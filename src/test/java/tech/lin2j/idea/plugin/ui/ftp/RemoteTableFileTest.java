package tech.lin2j.idea.plugin.ui.ftp;

import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import org.junit.Test;
import tech.lin2j.idea.plugin.file.RemoteTableFile;

import java.util.List;

/**
 * @author linjinjia
 * @date 2024/4/1 23:03
 */
public class RemoteTableFileTest {

    @Test
    public void test() throws Exception{
        SSHClient sshClient = new SSHClient(new DefaultConfig());
        sshClient.connect("192.168.0.104", 22);
        sshClient.authPassword("root", "123");
        List<RemoteResourceInfo> infos = sshClient.newSFTPClient().ls("/root");
        infos.stream().map(RemoteTableFile::new).forEach(System.out::println);
    }
}