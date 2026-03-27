package io.github.yueryou.easydev.plugin.ui.render;

import io.github.yueryou.easydev.plugin.model.LocalCommandStep;
import io.github.yueryou.easydev.plugin.model.PipelineStep;
import io.github.yueryou.easydev.plugin.model.RemoteCommandStep;
import io.github.yueryou.easydev.plugin.model.StepType;
import io.github.yueryou.easydev.plugin.model.UploadStep;

import javax.swing.*;
import java.awt.*;

/**
 * 流水线步骤列表单元格渲染器
 */
public class PipelineStepListCellRenderer extends JLabel implements ListCellRenderer<PipelineStep> {

    public PipelineStepListCellRenderer() {
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends PipelineStep> list,
            PipelineStep step,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        if (step == null) {
            setText("");
            return this;
        }

        StringBuilder text = new StringBuilder();
        text.append(index + 1).append(". ");
        text.append(step.getName() != null ? step.getName() : "未命名");
        text.append(" (");

        switch (step.getType()) {
            case LOCAL_COMMAND:
                text.append("本地命令");
                if (step instanceof LocalCommandStep) {
                    String cmd = ((LocalCommandStep) step).getCommand();
                    if (cmd != null && cmd.length() > 30) {
                        cmd = cmd.substring(0, 30) + "...";
                    }
                    text.append(": ").append(cmd);
                }
                break;
            case UPLOAD:
                text.append("上传文件");
                if (step instanceof UploadStep) {
                    text.append(": profile=").append(((UploadStep) step).getUploadProfileId());
                }
                break;
            case REMOTE_COMMAND:
                text.append("远程命令");
                if (step instanceof RemoteCommandStep) {
                    String cmd = ((RemoteCommandStep) step).getCommand();
                    if (cmd != null && cmd.length() > 30) {
                        cmd = cmd.substring(0, 30) + "...";
                    }
                    text.append(": ").append(cmd);
                }
                break;
        }
        text.append(")");

        setText(text.toString());
        setIcon(getIconForStepType(step.getType()));

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        return this;
    }

    private Icon getIconForStepType(StepType type) {
        switch (type) {
            case LOCAL_COMMAND:
                return com.intellij.icons.AllIcons.General.ArrowRight;
            case UPLOAD:
                return com.intellij.icons.AllIcons.Actions.Upload;
            case REMOTE_COMMAND:
                return com.intellij.icons.AllIcons.Actions.Execute;
            default:
                return com.intellij.icons.AllIcons.General.Information;
        }
    }
}
