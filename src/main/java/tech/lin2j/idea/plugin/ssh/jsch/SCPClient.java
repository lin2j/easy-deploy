package tech.lin2j.idea.plugin.ssh.jsch;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author linjinjia
 * @date 2023/12/25 22:35
 */
public class SCPClient {

    private final Session session;

    public SCPClient(Session session) {
        this.session = session;
    }

    public void put(String localFile, String remoteDir) throws JSchException, IOException {
        put(localFile, remoteDir, "0644");
    }

    public void put(String localFile, String remoteDir, String mode) throws JSchException, IOException {
        put(new String[]{localFile}, remoteDir, mode);
    }

    /**
     * Copy a set of files to a remote directory, uses specify mode
     * when creating the files on the remote side.
     *
     * @param localFiles Paths and names of the local files.
     * @param remoteDir  Remote target directory, use empty string
     *                   to specify the default directory
     * @param mode       a four digit string (e.g, 0644, see "man chmod", "man open")
     * @throws IOException IOException
     */
    public void put(String[] localFiles, String remoteDir, String mode) throws JSchException, IOException {
        if (localFiles == null || localFiles.length == 0 || remoteDir == null) {
            throw new IllegalArgumentException("Null Argument.");
        }

        if (mode == null || mode.length() != 4) {
            throw new IllegalArgumentException("Invalid mode.");
        }

        ChannelExec scp = openScpSendMode(remoteDir);
        InputStream response = new BufferedInputStream(scp.getInputStream(), 512);
        OutputStream request = new BufferedOutputStream(scp.getOutputStream(), 2048);

        for (String localFile : localFiles) {
            File file = new File(localFile);
            if (!file.exists()) {
                continue;
            }

            long contentLength = file.length();
            if (contentLength <= 0) {
                continue;
            }

            String cline = "C" + mode + " " + contentLength + " " + file.getName() + "\n";
            request.write(cline.getBytes(StandardCharsets.UTF_8));
            request.flush();
            checkAck(response);

            try (InputStream ins = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                long remain = contentLength;
                while (remain > 0) {
                    int trans;

                    if (remain > buffer.length) {
                        trans = buffer.length;
                    } else {
                        trans = (int) remain;
                    }

                    if (ins.read(buffer, 0, trans) != trans) {
                        throw new IOException("Cannot read enough from local file " + file.getName());
                    }

                    request.write(buffer, 0, trans);

                    remain -= trans;
                }

            }
            request.write(0);
            request.flush();

            checkAck(response);
        }

        request.write("E\n".getBytes());
        request.flush();

        scp.disconnect();
    }

    /**
     * When used in conjunction with SCP, this flag `-f`
     * indicates that the remote server is receiving a file
     * sent by the client (local side). It is not meant
     * to be used directly from the command line.
     *
     * @param remoteDir Represents the path on the remote server
     *                  where the file will be received
     * @return The channel used to send command or file content to
     * the remote server
     * @throws JSchException Exception
     */
    private ChannelExec openScpSendMode(String remoteDir) throws JSchException, IOException {
        String command = "scp -t -d " + remoteDir;
        ChannelExec scpChannel = (ChannelExec) session.openChannel("exec");
        scpChannel.setCommand(command);

        // get I/O streams for remote scp
        InputStream response = scpChannel.getInputStream();

        scpChannel.connect();
        checkAck(response);
        return scpChannel;
    }

    /**
     * Reads the response stream and checks whether
     * the ack flag is success.
     * <p>
     * The first byte of response stream is the flag
     * which indicates whether the command is handled
     * correct.These response codes indicate the status
     * of the acknowledgment for the SCP protocol.
     * <ul>
     *     <li>0 for success</li>
     *     <li>1 for error</li>
     *     <li>2 for fatal error</li>
     *     <li>-1 for error or unknown</li>
     * </ul>
     *
     * @param in response stream
     * @throws IOException if an I/O error occurs when reading
     */
    private void checkAck(InputStream in) throws IOException {
        int b = in.read();

        if (b == 0) return;

        if (b == -1) {
            throw new IOException("Remote scp terminated unexpectedly.");
        }

        StringBuilder errMsg = new StringBuilder();
        int c;
        do {
            c = in.read();
            errMsg.append((char)c);
        } while (c != '\n');

        throw new IOException("Remote scp terminated with error, code: " + b + ", errMsg: " + errMsg);
    }

}