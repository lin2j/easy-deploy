package tech.lin2j.idea.plugin.terminal;

import com.jediterm.terminal.ProcessTtyConnector;
import com.jediterm.terminal.Questioner;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ssh.CustomTtyConnector;
import tech.lin2j.idea.plugin.ssh.SshProcess;

import java.awt.Dimension;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author linjinjia
 * @date 2022/6/25 17:28
 */
public class ProcessTtyConnectorAdapter extends ProcessTtyConnector {
    private final CustomTtyConnector customTtyConnector;

    public ProcessTtyConnectorAdapter(SshProcess sshProcess,
                                      CustomTtyConnector customTtyConnector,
                                      Charset charset) {
        super(sshProcess, charset);
        this.customTtyConnector = customTtyConnector;
    }

    @Override
    public boolean init(Questioner q) {
        return this.customTtyConnector.init(q);
    }

    @Override
    public void close() {
        this.customTtyConnector.close();
    }

    public void resize(@NotNull Dimension termWinSize) {
        this.customTtyConnector.resize(termWinSize);
    }

    @Override
    public void resize(Dimension termSize, Dimension pixelSize) {
        this.customTtyConnector.resize(termSize, pixelSize);
    }

    @Override
    protected void resizeImmediately() {
        this.customTtyConnector.resizeImmediately();
    }

    @Override
    public String getName() {
        return this.customTtyConnector.getName();
    }

    @Override
    public boolean isConnected() {
        return this.customTtyConnector.isConnected();
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        return this.customTtyConnector.read(buf, offset, length);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        this.customTtyConnector.write(bytes);
    }

    @Override
    public void write(String string) throws IOException {
        this.customTtyConnector.write(string);
    }
}