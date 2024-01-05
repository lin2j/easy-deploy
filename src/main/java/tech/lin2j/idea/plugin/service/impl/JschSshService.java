package tech.lin2j.idea.plugin.service.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import tech.lin2j.idea.plugin.file.ExtensionFilter;
import tech.lin2j.idea.plugin.file.FileFilter;
import tech.lin2j.idea.plugin.file.FileFilterAdapter;
import tech.lin2j.idea.plugin.service.ISshService;
import tech.lin2j.idea.plugin.ssh.SshConnectionManager;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.SshStatus;
import tech.lin2j.idea.plugin.ssh.jsch.SCPClient;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.util.concurrent.TimeUnit;

/**
 * @author linjinjia
 * @date 2023/12/24 14:42
 */
public class JschSshService implements ISshService {

    private static final Logger log = Logger.getInstance(JschSshService.class);

    @Override
    public SshStatus isValid(SshServer sshServer) {
        boolean status = false;
        String msg = "failed";
        Session session = null;
        try {
            session = getSession(sshServer);
            status = true;
            msg = "success";
        } catch (JSchException e) {
            log.error(e);
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
        return new SshStatus(status, msg);
    }

    @Override
    public SshStatus execute(SshServer sshServer, String command) {
        boolean status = false;
        StringBuilder msg = new StringBuilder();

        Session session = null;
        ChannelExec channel = null;
        try {
            session = getSession(sshServer);
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            InputStream err = channel.getErrStream();
            InputStream resp = channel.getInputStream();
            channel.connect();
            waitForResponse(channel, err);

            msg.append(readStream(resp));

            status = true;
        } catch (JSchException | IOException e) {
            log.error(e);
            msg.append(e.getMessage());
        } finally {
            close(channel);
            close(session);
        }
        return new SshStatus(status, msg.toString());
    }

    @Override
    public SshStatus scpGet(SshServer sshServer, String remoteFile, String localFile) {
        return null;
    }

    @Override
    public SshStatus scpPut(Project project, SshServer sshServer, String localFile, String remoteDir, String exclude) {
        boolean status = false;
        String msg = "failed";

        Session session = null;

        try {
            session = getSession(sshServer);
            SCPClient scpClient = new SCPClient(session);

            if (!isFileExist(session, remoteDir).isSuccess()) {
                mkdir(session, remoteDir);
            }

            String cmd = "scp -r " + localFile + " " + remoteDir;
            if (new File(localFile).isDirectory()) {
                FileFilter filter = new FileFilterAdapter(project, new ExtensionFilter(exclude), sshServer, cmd);
                putDir(session, scpClient, filter, localFile, remoteDir);
            } else {
                FileFilter filter = new FileFilterAdapter(project, new ExtensionFilter(""), sshServer, cmd);
                putFile(scpClient, filter, localFile, remoteDir);
            }
            status = true;
            msg = "success";
        } catch (Exception e) {
            log.warn(e);
            msg = e.getMessage();
        } finally {
            close(session);
        }
        return new SshStatus(status, msg);
    }

    @Override
    public SshStatus isDirExist(SshServer server, String remoteTargetDir) {
        try {
            Session session = getSession(server);
            return isFileExist(session, remoteTargetDir);
        } catch (Exception e) {
            log.error(e);
        }
        return new SshStatus(false, "Exception");
    }

    /**
     * Check if the remote file or directory exists.
     *
     * @param session         SSH session
     * @param remoteTargetDir File or directory to be verified
     *                        for existence
     * @return true if the file or directory exists, false otherwise.
     */
    private SshStatus isFileExist(Session session, String remoteTargetDir) {
        boolean status = false;
        String msg = "No such file or directory: " + remoteTargetDir;

        try {
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand("stat " + remoteTargetDir);
            channelExec.connect();
            while (channelExec.getExitStatus() == -1) {
                TimeUnit.MILLISECONDS.sleep(200);
            }
            if (channelExec.getExitStatus() == 0) {
                status = true;
                msg = "Success";
            }
        } catch (Exception e) {
            log.error(e);
        }
        return new SshStatus(status, msg);
    }

    /**
     * Upload files or folders to the specified directory on the
     * remote server, during which filter will be called for file
     * filtering. When encountering a subdirectory, this method
     * will be recursively called.
     *
     * @param session      SSH session
     * @param scpClient    Scp client implementation based on JSCH
     * @param filter       File filters, only accepted ones will be sent
     * @param localFile    File or directory on local machine
     * @param remoteDstDir Target directory on the remote server
     * @throws Exception Exception
     */
    private void putDir(Session session, SCPClient scpClient, FileFilter filter,
                        String localFile, String remoteDstDir) throws Exception {
        File dir = new File(localFile);
        if (dir.isDirectory()) {
            String[] fileList = dir.list();
            if (fileList == null) {
                return;
            }
            for (String f : fileList) {
                String localFullFileName = localFile + "/" + f;
                if (new File(localFullFileName).isDirectory()) {
                    String remoteSubDir = remoteDstDir + "/" + f;
                    mkdir(session, remoteSubDir);
                    putDir(session, scpClient, filter, localFullFileName, remoteSubDir);
                } else {
                    putFile(scpClient, filter, localFullFileName, remoteDstDir);
                }
            }
        } else {
            putFile(scpClient, filter, localFile, remoteDstDir);
        }
    }

    private void putFile(SCPClient scpClient, FileFilter filter,
                         String localFile, String remoteTargetDir) throws Exception {
        filter.accept(localFile, (accept) -> {
            if (accept) {
                scpClient.put(localFile, remoteTargetDir);
            }
        });
    }

    /**
     * Create the specified directory on the remote server, and
     * automatically create it if the parent directory does not
     * exist. Actually, calling the "mkdir -p dir" command
     *
     * @param session   SSH session
     * @param remoteDir Remote server directory to be created
     * @throws Exception Exception
     */
    private void mkdir(Session session, String remoteDir) throws Exception {
        ChannelExec channel = null;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("mkdir -p " + remoteDir);
            InputStream err = channel.getErrStream();
            channel.connect();
            waitForResponse(channel, err);
        } finally {
            close(channel);
        }
    }

    private Session getSession(SshServer server) throws JSchException {
        return SshConnectionManager.makeJschSession(server);
    }

    /**
     * Reads the response stream from remote server
     *
     * @param ins input stream
     * @return Message from remote server, or empty string when
     * occurring exception
     */
    private StringBuilder readStream(InputStream ins) {
        StringBuilder result = new StringBuilder();
        InputStreamReader channelInsReader = null;
        BufferedReader fromServer = null;
        try {
            channelInsReader = new InputStreamReader(ins);
            fromServer = new BufferedReader(channelInsReader);

            String line;
            while ((line = fromServer.readLine()) != null) {
                result.append(line).append("\n");
            }
        } catch (IOException e) {
            log.error(e);
            close(channelInsReader);
            close(fromServer);
        }
        return result;
    }

    /**
     * Waiting for remote server to execute commands and respond
     *
     * @param channel command channel
     * @throws IOException IOException
     */
    private void waitForResponse(ChannelExec channel, InputStream e) throws IOException {
        while (channel.getExitStatus() == -1) {
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (Exception ignored) {

            }
        }
        int r = channel.getExitStatus();
        BufferedReader fromServer = new BufferedReader(new InputStreamReader(e));
        String errMsg = fromServer.readLine();
        if (StringUtil.isNotEmpty(errMsg)) {
            String err = String.format("Failed to execute command, code: %s, errMsg: %s", r, errMsg);
            throw new RuntimeException(err);
        }
    }

    private void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ignored) {
        }
    }

    private void close(Channel channel) {
        if (channel != null) {
            channel.disconnect();
        }
    }

    private void close(Session session) {
        if (session != null) {
            session.disconnect();
        }
    }

}