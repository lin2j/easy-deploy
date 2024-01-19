package tech.lin2j.idea.plugin.ssh.sshj;

import com.jediterm.terminal.Questioner;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.SessionChannel;
import net.schmizz.sshj.transport.TransportException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.lin2j.idea.plugin.ssh.CustomTtyConnector;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author linjinjia
 * @date 2024/1/7 22:01
 */
public class SshjTtyConnector implements CustomTtyConnector {
    public static final Logger log = LoggerFactory.getLogger(SshjTtyConnector.class);

    private String title;
    private final SSHClient sshClient;
    private Session.Shell shell;
    private InputStream inputStream;
    private InputStreamReader inputStreamReader;
    private OutputStream outputStream;
    private Dimension pendingTermSize;
    private Dimension pendingPixelSize;
    private final AtomicBoolean isInitiated = new AtomicBoolean(false);

    public SshjTtyConnector(SSHClient sshClient) {
        this.sshClient = sshClient;
        this.init(null);
    }

    @Override
    public boolean init(Questioner questioner) {
        if (!sshClient.isConnected()) {
            isInitiated.set(true);
            return false;
        }
        try {
            Session session = sshClient.startSession();
            session.allocatePTY("xterm", 80, 24, 0, 0, Collections.emptyMap());
            shell = session.startShell();
            inputStream = shell.getInputStream();
            outputStream = shell.getOutputStream();
            inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            resizeImmediately();
            return true;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            isInitiated.set(true);
        }
        return false;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public void resizeImmediately() {
        if (pendingTermSize != null && pendingPixelSize != null) {
            try {
                this.shell.changeWindowDimensions(
                        pendingTermSize.width, pendingTermSize.height,
                        pendingPixelSize.width, pendingPixelSize.height);
            } catch (TransportException e) {
                throw new RuntimeException(e);
            }

            pendingTermSize = null;
            pendingPixelSize = null;
        }

    }

    public void resize(@NotNull Dimension termSize) {
        resize(termSize, new Dimension(0, 0));
    }

    @Override
    public void resize(Dimension termSize, Dimension pixelSize) {
        pendingTermSize = termSize;
        pendingPixelSize = pixelSize;
        if (shell != null) {
            resizeImmediately();
        }

    }

    @Override
    public void close() {
        if (shell != null) {
            try {
                shell.close();
            } catch (Exception ignored) {

            }
            shell = null;
            inputStream = null;
            outputStream = null;
        }
    }

    @Override
    public void setName(String title) {
        this.title = title;
    }


    @Override
    public String getName() {
        return this.title;
    }

    @Override
    public int read(char[] buffer, int offset, int len) throws IOException {
        return inputStreamReader.read(buffer, offset, len);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        if (outputStream != null) {
            outputStream.write(bytes);
            outputStream.flush();
        }
    }

    @Override
    public boolean isConnected() {
        return shell != null && shell.isOpen();
    }

    @Override
    public void write(String s) throws IOException {
        write(s.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public int waitFor() throws InterruptedException {
        while (!isInitiated.get() || isRunning(shell)) {
            Thread.sleep(100L);
        }

        return 0;
    }

    private static boolean isRunning(Session.Shell shell) {
        return shell != null && shell.isOpen();
    }

    public boolean ready() throws IOException {
        return inputStreamReader.ready();
    }

}