package tech.lin2j.idea.plugin.service;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import tech.lin2j.idea.plugin.enums.AuthType;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.SshStatus;
import tech.lin2j.idea.plugin.uitl.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author linjinjia
 * @date 2022/4/25 20:50
 */
public class SshService {

    /**
     * service instance
     */
    private static final SshService INSTANCE = new SshService();

    private static final Logger log = Logger.getInstance(SshService.class);

    public static SshService getInstance() {
        return INSTANCE;
    }

    public Connection getConnection(SshServer sshServer) throws Exception {
        Connection connection = new Connection(sshServer.getIp(), sshServer.getPort());
        connection.connect();

        if (AuthType.needPassword(sshServer.getAuthType())) {
            authWithPassword(connection, sshServer);
        } else {
            authWithPemPrivateKey(connection, sshServer);
        }
        return connection;
    }

    /**
     * get file from remote server
     *
     * @param sshServer  server information
     * @param remoteFile remote file absolute path
     * @param localFile  local file absolute path
     * @return download result
     */
    public SshStatus scpGet(SshServer sshServer, String remoteFile, String localFile) {
        Connection conn = null;
        String msg = "";
        try {
            conn = getConnection(sshServer);
            SCPClient scpClient = new SCPClient(conn);
            scpClient.get(remoteFile, localFile);
            return new SshStatus(true, "success");
        } catch (Exception e) {
            log.warn(e);
            msg = e.getMessage();
        } finally {
            close(conn);
        }
        return new SshStatus(false, msg);
    }

    /**
     * upload file to remote server
     *
     * @param sshServer  server information
     * @param localFile  local file absolute path
     * @param remoteFile remote file absolute path
     * @return upload result
     */
    public SshStatus scpPut(SshServer sshServer, String localFile, String remoteFile) {
        Connection conn = null;
        String msg = "";
        try {
            conn = getConnection(sshServer);
            SCPClient scpClient = new SCPClient(conn);
            scpClient.put(localFile, remoteFile);
            return new SshStatus(true, "success");
        } catch (Exception e) {
            log.warn(e);
            msg = e.getCause().getMessage();
        } finally {
            close(conn);
        }

        return new SshStatus(false, msg);
    }

    /**
     * block if the command is not finished <br>
     * so do not execute command like "tail -f" <br>
     * because it will block the thread
     */
    public SshStatus execute(SshServer sshServer, String command) {
        Connection conn = null;
        String msg = "";
        Session session = null;
        try {
            conn = getConnection(sshServer);
            session = conn.openSession();
            session.execCommand(command);
            msg = resolveInputStream(session.getStdout());
            if (StringUtil.isEmpty(msg)) {
                msg = resolveInputStream(session.getStderr());
            }
            return new SshStatus(true, msg);
        } catch (Exception e) {
            log.warn(e);
            msg = e.getMessage();
        } finally {
            close(conn);
            close(session);
        }
        return new SshStatus(false, msg);
    }

    /**
     * test the server information is correct
     */
    public SshStatus isValid(SshServer sshServer) {
        Connection conn = null;
        String msg = "";
        try {
            conn = getConnection(sshServer);
            if (conn != null) {
                return new SshStatus(true, "success");
            } else {
                return new SshStatus(false, "failed");
            }
        } catch (Exception e) {
            log.error(e);
            msg = e.getMessage();
        } finally {
            close(conn);
        }
        return new SshStatus(false, msg);
    }

    private void close(Connection conn) {
        if (conn != null) {
            conn.close();
        }
    }

    private void close(Session session) {
        if (session != null) {
            session.close();
        }
    }

    private String resolveInputStream(InputStream in) {
        StringBuilder sb = new StringBuilder();
        byte[] buf = new byte[1024];
        int len = 0;
        try {
            while ((len = in.read(buf)) > 0) {
                sb.append(new String(buf, 0, len, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            log.error(e);
        }
        return sb.toString();
    }

    private void authWithPassword(Connection connection, SshServer sshServer) throws IOException {
        boolean isAuthenticated = false;
        String userName = sshServer.getUsername();
        String password = sshServer.getPassword();

        isAuthenticated = connection.authenticateWithPassword(userName, password);
        if (!isAuthenticated) {
            String authErr = "Authentication failed, please check the user name and password";
            throw new RuntimeException(authErr);
        }
    }

    private void authWithPemPrivateKey(Connection connection, SshServer sshServer) {
        boolean isAuthenticated = false;
        String userName = sshServer.getUsername();
        String pemPrivateKey = sshServer.getPemPrivateKey();
        pemPrivateKey = FileUtil.replaceHomeSymbol(pemPrivateKey);
        File file = new File(pemPrivateKey);
        if (!file.exists()) {
            String err = "Authentication failed, pem private key file not exist: " + pemPrivateKey;
            throw new RuntimeException(err);
        }

        String authErr = "";
        try {
            isAuthenticated = connection.authenticateWithPublicKey(userName, file, null);
            // https://blog.csdn.net/chezong/article/details/122403709
            authErr = "Authentication failed, please check your ssh private key file";
        } catch (IOException e) {
            authErr = e.getMessage();
            if (e.getCause().getMessage().startsWith("Invalid PEM structure")) {
                authErr = "please check your pem file structure";
            }
        }
        if (!isAuthenticated) {
            throw new RuntimeException(authErr);
        }
    }
}