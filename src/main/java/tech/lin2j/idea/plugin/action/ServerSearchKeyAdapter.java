package tech.lin2j.idea.plugin.action;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.SearchTextField;
import org.apache.commons.collections.CollectionUtils;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.ssh.SshServer;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * keyboard listener
 *
 * @author linjinjia 2024/5/16 23:11
 */
public class ServerSearchKeyAdapter extends KeyAdapter {

    private final SearchTextField searchInput;
    private final Consumer<List<SshServer>> action;

    public ServerSearchKeyAdapter(SearchTextField searchInput, Consumer<List<SshServer>> action) {
        this.searchInput = searchInput;
        this.action = action;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        search(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        search(e);
    }


    private void search(KeyEvent e) {
        List<SshServer> searchResult = new ArrayList<>();
        List<SshServer> serverInConfig = ConfigHelper.sshServers();
        if (CollectionUtils.isEmpty(serverInConfig)) {
            return;
        }

        String keyword = searchInput.getText();
        if (StringUtil.isEmpty(keyword)) {
            action.accept(serverInConfig);
            return;
        }

        for (SshServer server : serverInConfig) {
            if (server.getIp().contains(keyword)) {
                searchResult.add(server);
                continue;
            }
            if (server.getUsername().contains(keyword)) {
                searchResult.add(server);
                continue;
            }
            String desc = server.getDescription();
            if (StringUtil.isNotEmpty(desc) && desc.contains(keyword)) {
                searchResult.add(server);
            }
        }

        action.accept(searchResult);
    }
}
