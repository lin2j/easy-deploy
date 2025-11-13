package io.github.yueryou.easydev.plugin.ui.component;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;
import tech.lin2j.idea.plugin.uitl.UiUtil;

import javax.swing.*;
import java.awt.*;

/**
 * 命令设置
 * 1. 设置每个session最多保存的历史命令个数
 * 2. 历史命令是否自动去重处理
 * 3. 命令编排
 */
public class CommandSettingPanel extends JPanel {
    private final Project project;
    private final JPanel root;
    private JBLabel label;
    public CommandSettingPanel(Project project) {
        this.project = project;
        initInput();
        root = new JPanel(new GridBagLayout());
        root.add(label);
        root.setPreferredSize(new Dimension(UiUtil.screenWidth() / 2, 600));

    }
    private void initInput() {
        label = new JBLabel(MessagesBundle.getText("dialog.feature.expect"));
    }

    public JPanel createUI() {
        return root;
    }
}
