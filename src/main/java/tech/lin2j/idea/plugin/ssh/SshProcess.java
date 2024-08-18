package tech.lin2j.idea.plugin.ssh;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author linjinjia
 * @date 2022/6/25 00:36
 */
public class SshProcess extends Process {

    private final CustomTtyConnector ttyConnector;

    public SshProcess(CustomTtyConnector customTtyConnector) {
        this.ttyConnector = customTtyConnector;
    }

    @Override
    public OutputStream getOutputStream() {
        return ttyConnector.getOutputStream();
    }

    @Override
    public InputStream getInputStream() {
        return ttyConnector.getInputStream();
    }

    @Override
    public InputStream getErrorStream() {
        return ttyConnector.getInputStream();
    }

    @Override
    public int waitFor() throws InterruptedException {
        while(isAlive()) {
            this.wait();
        }
        return exitValue();
    }

    @Override
    public int exitValue() {
        if (ttyConnector.isConnected()) {
            throw new IllegalThreadStateException();
        }
        return 0;
    }

    @Override
    public void destroy() {
        try {
            ttyConnector.close();
        } catch (Exception e) {
            //
        }
    }
}