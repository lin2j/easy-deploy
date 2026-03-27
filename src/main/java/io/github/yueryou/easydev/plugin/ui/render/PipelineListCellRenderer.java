package io.github.yueryou.easydev.plugin.ui.render;

import io.github.yueryou.easydev.plugin.model.Pipeline;

import javax.swing.*;
import java.awt.*;

/**
 * 流水线列表单元格渲染器
 */
public class PipelineListCellRenderer extends JLabel implements ListCellRenderer<Pipeline> {

    public PipelineListCellRenderer() {
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends Pipeline> list,
            Pipeline pipeline,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        if (pipeline == null) {
            setText("");
            return this;
        }

        setText(pipeline.getName());
        setIcon(com.intellij.icons.AllIcons.Actions.Execute);

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        return this;
    }
}
