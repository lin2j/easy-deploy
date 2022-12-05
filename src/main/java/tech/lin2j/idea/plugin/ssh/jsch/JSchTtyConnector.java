package tech.lin2j.idea.plugin.ssh.jsch;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jediterm.terminal.Questioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.lin2j.idea.plugin.ssh.CustomTtyConnector;
import tech.lin2j.idea.plugin.ssh.SshConnection;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * imitate ssh/src/com/jediterm/ssh/jsch/JSchTtyConnector.java
 * in JetBrains/jediterm project
 *
 * @author linjinjia
 * @date 2022/6/25 11:17
 */
public class JSchTtyConnector implements CustomTtyConnector {
    public static final Logger LOG = LoggerFactory.getLogger(JSchTtyConnector.class);

    private String title;
    private final JSchConnection sshConnection;
    private Session session;
    private ChannelShell channelShell;
    private InputStream inputStream;
    private InputStreamReader inputStreamReader;
    private OutputStream outputStream;
    private Dimension pendingTermSize;
    private Dimension pendingPixelSize;
    private final AtomicBoolean isInitiated = new AtomicBoolean(false);

    public JSchTtyConnector(SshConnection sshConnection) {
        this.sshConnection = (JSchConnection) sshConnection;
        init(null);
    }

    @Override
    public void resize(Dimension termSize, Dimension pixelSize) {
        this.pendingTermSize = termSize;
        this.pendingPixelSize = pixelSize;
        if (this.channelShell != null) {
            this.resizeImmediately();
        }

    }

    private void setPtySize(ChannelShell channel, int col, int row, int wp, int hp) {
        channel.setPtySize(col, row, wp, hp);
    }

    @Override
    public void resizeImmediately() {
        if (this.pendingTermSize != null && this.pendingPixelSize != null) {
            this.setPtySize(
                    this.channelShell,
                    this.pendingTermSize.width, this.pendingTermSize.height,
                    this.pendingPixelSize.width, this.pendingPixelSize.height);
            this.pendingTermSize = null;
            this.pendingPixelSize = null;
        }
    }

    @Override
    public InputStream getInputStream() {
        return this.inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    @Override
    public void setName(String title) {
        this.title = title;
    }

    @Override
    public boolean init(Questioner questioner) {
        try {
            session = sshConnection.getSession();
            channelShell = (ChannelShell) session.openChannel("shell");
            inputStream = channelShell.getInputStream();
            outputStream = channelShell.getOutputStream();
            inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            channelShell.connect();
            return true;
        } catch (JSchException | IOException e) {
            questioner.showMessage(e.getMessage());
            LOG.error("Error opening channel", e);
            return false;
        } finally {
            isInitiated.set(true);
        }
    }

    @Override
    public void close() {
        if(session != null) {
            session.disconnect();
            session = null;
            inputStream = null;
            outputStream = null;
        }
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        return inputStreamReader.read(buf, offset, length);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        if(outputStream == null) {
            return ;
        }
        outputStream.write(bytes);
        outputStream.flush();
    }

    @Override
    public boolean isConnected() {
        return channelShell != null && channelShell.isConnected();
    }

    @Override
    public void write(String s) throws IOException {
        write(s.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public int waitFor() throws InterruptedException {
        while(!isInitiated.get() && isRunning(channelShell)) {
            // busy waiting
            Thread.sleep(100);
        }
        return channelShell.getExitStatus();
    }

//    @Override
    public boolean ready() throws IOException {
        return inputStreamReader.ready();
    }

    private boolean isRunning(ChannelShell channelShell) {
        return channelShell != null
                && channelShell.getExitStatus() < 0
                && channelShell.isConnected();
    }
}