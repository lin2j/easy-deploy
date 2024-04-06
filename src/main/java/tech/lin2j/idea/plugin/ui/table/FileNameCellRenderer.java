package tech.lin2j.idea.plugin.ui.table;

import com.intellij.util.ui.table.IconTableCellRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.file.TableFile;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import java.awt.Component;

/**
 * @author linjinjia
 * @date 2024/4/6 13:51
 */
public class FileNameCellRenderer extends IconTableCellRenderer<TableFile> {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, selected, focus, row, column);
        ((JLabel) component).setText(((TableFile)value).getName());
        return component;
    }

    @Nullable
    @Override
    protected Icon getIcon(@NotNull TableFile value, JTable table, int row) {
        return value.getIcon();
    }
}