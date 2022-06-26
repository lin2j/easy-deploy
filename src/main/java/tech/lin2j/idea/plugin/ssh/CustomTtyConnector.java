package tech.lin2j.idea.plugin.ssh;

import com.jediterm.terminal.TtyConnector;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author linjinjia
 * @date 2022/6/24 21:41
 */
public interface CustomTtyConnector extends TtyConnector {

    /**
     * JSch, implementation of SSH2
     */
    String JSCH = "JSch";

    InputStream getInputStream();

    OutputStream getOutputStream();

    void setName(String title);
}