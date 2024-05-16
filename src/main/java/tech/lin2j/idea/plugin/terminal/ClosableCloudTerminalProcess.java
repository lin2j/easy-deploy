package tech.lin2j.idea.plugin.terminal;

import org.jetbrains.plugins.terminal.cloud.CloudTerminalProcess;
import tech.lin2j.idea.plugin.ssh.sshj.SshjConnection;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author linjinjia
 * @date 2024/5/16 23:40
 */
public class ClosableCloudTerminalProcess extends CloudTerminalProcess {

    public ClosableCloudTerminalProcess(OutputStream terminalInput, InputStream terminalOutput) {
        super(terminalInput, terminalOutput);
    }
}