package tech.lin2j.idea.plugin.ui.component;

import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.ui.border.IdeaTitledBorder;
import com.intellij.util.ui.JBUI;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

/**
 *
 *
 * @author linjinjia 2024/5/19 00:40
 */
public class HostDependencyPanel extends JPanel {

    private List<HostChainItem> hostList;
    private boolean hasCycle;

    public HostDependencyPanel() {
        String title = MessagesBundle.getText("dialog.panel.host.proxy.border-title");
        setLayout(new GridBagLayout());
        setBorder(new IdeaTitledBorder(title, 0, JBUI.emptyInsets()));
    }

    private void initUI() {
        if (hostList == null) {
            return;
        }
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        if (hasCycle) {
            JLabel tipLabel = new JLabel(MessagesBundle.getText("dialog.panel.host.proxy.cycle"));
            tipLabel.setForeground(JBColor.RED);
            gbc.gridwidth = getWidth();
            add(tipLabel, gbc);
            gbc.gridwidth = 1;
            gbc.gridy++;
        }

        int hostsPerRow = 3;
        boolean leftToRight = true;
        int size = hostList.size();
        for (int i = 1; i <= size; i++) {
            HostChainItem item = hostList.get(i - 1);
            String host = item.getServer().getIp();
            JLabel hostLabel = new JLabel(host);
            if (item.isCycle()) {
                hostLabel.setIcon(AllIcons.Actions.Refresh);
                hostLabel.setForeground(JBColor.RED);
            }
            add(hostLabel, gbc);

            if (size == 1 || i == size) {
                break;
            }

            if (i > 0 && i % hostsPerRow == 0) {
                leftToRight = !leftToRight;
                gbc.gridy++;
                ArrowLabel curveArrowLabel = new ArrowLabel(true, leftToRight);
                gbc.gridx = leftToRight ? 0 : gbc.gridx;
                add(curveArrowLabel, gbc);
                gbc.gridy++;
                continue;
            }

            ArrowLabel arrowLabel = new ArrowLabel(false, leftToRight);
            gbc.gridx = leftToRight ? gbc.gridx + 1 : gbc.gridx - 1;
            add(arrowLabel, gbc);
            gbc.gridx = leftToRight ? gbc.gridx + 1 : gbc.gridx - 1;
        }
    }

    public void setHostList(List<HostChainItem> hostList, boolean hasCycle) {
        this.hasCycle = hasCycle;
        this.hostList = hostList;
        removeAll();
        initUI();
        revalidate();
        repaint();
    }
}
