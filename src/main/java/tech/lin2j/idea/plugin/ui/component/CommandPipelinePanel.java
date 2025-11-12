package tech.lin2j.idea.plugin.ui.component;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import tech.lin2j.idea.plugin.model.Command;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;
import tech.lin2j.idea.plugin.uitl.UiUtil;

import javax.swing.*;
import java.awt.*;

/**
 * 自定义任务流水线。
 * 1. 创建工作流水线。
 * 2. 基层基于上传文件和执行脚本实现。
 * 3. 可一键部署开发环境（一键部署mysql、redis、某产品）
 * 4. 一键巡检，检查环境依赖，检查服务可用性、一键拉取日志、快速下载指定文件
 * 5. 核心是要支持多任务编排，控制任务执行顺序，任务上下文。
 */
public class CommandPipelinePanel extends JPanel {

    private final Project project;
    private final JPanel root;
    private JBLabel label;
    public CommandPipelinePanel(Project project) {
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
