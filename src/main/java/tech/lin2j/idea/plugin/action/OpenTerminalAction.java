package tech.lin2j.idea.plugin.action;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.TerminalTabState;
import org.jetbrains.plugins.terminal.TerminalView;
import org.jetbrains.plugins.terminal.cloud.CloudTerminalRunner;
import tech.lin2j.idea.plugin.enums.AuthType;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.SshStatus;
import tech.lin2j.idea.plugin.ssh.exception.RemoteSdkException;
import tech.lin2j.idea.plugin.uitl.TerminalRunnerUtil;
import tech.lin2j.idea.plugin.uitl.UiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * @author linjinjia
 * @date 2024/5/5 12:25
 */
public class OpenTerminalAction implements ActionListener {

    private final int sshId;
    private final Project project;
    private final String workingDirectory;

    public OpenTerminalAction(int sshId, Project project, @Nullable String workingDirectory) {
        this.sshId = sshId;
        this.project = project;
        this.workingDirectory = workingDirectory;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SshServer tmp = ConfigHelper.getSshServerById(sshId);
        // 获取 IP 列表，支持多 IP 配置
        List<String> ipList = tmp.getIpList();
        if (ipList.isEmpty()) {
            Messages.showErrorDialog("IP address is required", "Error");
            return;
        }

        // 当有多个 IP 时，让用户选择连接方式
        if (ipList.size() == 1) {
            // 单个 IP，直接打开终端
            openTerminalForIp(ipList.get(0), tmp, project, workingDirectory);
        } else {
            // 多个 IP，弹出对话框让用户选择
            showMultiIpDialog(ipList, tmp);
        }
    }

    /**
     * 显示多 IP 选择对话框
     */
    private void showMultiIpDialog(List<String> ipList, SshServer server) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 提示信息
        JLabel label = new JLabel("<html>Multiple servers available. Choose an option:</html>");
        panel.add(label, BorderLayout.NORTH);

        // 选项面板
        JPanel optionsPanel = new JPanel(new GridLayout(0, 1, 0, 5));

        // 选项 1: 选择单个 IP（默认选中第一个）
        JComboBox<String> ipComboBox = new JComboBox<>();
        for (String ip : ipList) {
            ipComboBox.addItem(ip + ":" + server.getPort());
        }
        JPanel singleIpPanel = new JPanel(new BorderLayout(5, 0));
        singleIpPanel.add(new JLabel("Connect to one server:"), BorderLayout.WEST);
        singleIpPanel.add(ipComboBox, BorderLayout.CENTER);

        // 选项 2: 连接所有 IP
        JCheckBox connectAllCheck = new JCheckBox("Connect to all servers (open multiple terminals)");

        optionsPanel.add(singleIpPanel);
        optionsPanel.add(connectAllCheck);
        panel.add(optionsPanel, BorderLayout.CENTER);

        // 使用 JOptionPane 显示自定义对话框
        Object[] options = {"OK", "Cancel"};
        int result = JOptionPane.showOptionDialog(
                null,
                panel,
                "Select Server(s)",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (result == JOptionPane.OK_OPTION) { // 用户点击 OK
            if (connectAllCheck.isSelected()) {
                // 连接所有 IP
                openTerminalsForAllIps(ipList, server);
            } else {
                // 连接选中的单个 IP
                Object selectedItem = ipComboBox.getSelectedItem();
                if (selectedItem != null) {
                    String selectedIp = selectedItem.toString().split(":")[0];
                    openTerminalForIp(selectedIp, server, project, workingDirectory);
                }
            }
        }
    }

    /**
     * 为单个 IP 打开终端
     */
    private void openTerminalForIp(String ip, SshServer originalServer, Project project, String workingDirectory) {
        // 创建用于终端连接的服务器副本，使用单个 IP
        SshServer serverForTerminal = originalServer.clone();
        serverForTerminal.setIp(ip);
        serverForTerminal.setIps(null); // 清除多 IP 设置，避免后续处理混淆

        SshStatus status = new SshStatus(false, null);
        String title = String.format("Opening terminal %s:%s", ip, originalServer.getPort());

        ProgressManager.getInstance().run(new Task.Backgroundable(project, title) {
            CloudTerminalRunner runner = null;

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                boolean needPassword = AuthType.needPassword(serverForTerminal.getAuthType());
                if (!needPassword || StringUtil.isNotEmpty(serverForTerminal.getPassword())) {
                    SshServer server = serverForTerminal.clone();
                    coreProcess(indicator, server);
                } else {
                    UiUtil.getUserInputAsync().thenAccept(password -> {
                        SshServer server = serverForTerminal.clone();
                        server.setPassword(password);
                        if (StringUtil.isEmpty(server.getPassword())) {
                            return;
                        }
                        coreProcess(indicator, server);
                    }).exceptionally(throwable -> {
                        return null;
                    });
                }
            }

            private void coreProcess(@NotNull ProgressIndicator indicator, SshServer server) {
                indicator.setIndeterminate(false);
                try {
                    runner = TerminalRunnerUtil.createCloudTerminalRunner(project, server, workingDirectory);
                    status.setSuccess(true);
                } catch (RemoteSdkException ex) {
                    status.setMessage("Error connecting server: " + ex.getMessage());
                } finally {
                    indicator.setFraction(1);
                }
            }

            @Override
            public void onFinished() {
                if (!status.isSuccess()) {
                    Messages.showErrorDialog(status.getMessage(), "Error");
                    return;
                }
                TerminalView terminalView = TerminalView.getInstance(project);
                TerminalTabState tabState = new TerminalTabState();
                tabState.myTabName = ip + ":" + originalServer.getPort();
                terminalView.createNewSession(runner, tabState);
            }
        });
    }

    /**
     * 为所有 IP 打开多个终端窗口
     */
    private void openTerminalsForAllIps(List<String> ipList, SshServer originalServer) {
        for (String ip : ipList) {
            // 每个 IP 独立打开一个终端
            openTerminalForIp(ip, originalServer, project, workingDirectory);
        }
    }
}