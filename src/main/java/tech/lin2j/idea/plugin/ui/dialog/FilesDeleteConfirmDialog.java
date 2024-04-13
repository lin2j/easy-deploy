package tech.lin2j.idea.plugin.ui.dialog;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.file.RemoteTableFile;
import tech.lin2j.idea.plugin.file.TableFile;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;

/**
 * @author linjinjia
 * @date 2024/4/13 11:05
 */
public class FilesDeleteConfirmDialog extends DialogWrapper {

    private JPanel root;
    private JBList<TableFile> fileList;

    public FilesDeleteConfirmDialog(List<TableFile> files) {
        super(null);

        fileList.setCellRenderer(new ListCellRenderer());
        fileList.setModel(new CollectionListModel<>(files));
        setTitle("Delete Files And Directories");
        init();

        Toolkit tk = Toolkit.getDefaultToolkit();
        root.setMinimumSize(new Dimension(tk.getScreenSize().width / 3, 0));
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return root;
    }


    /**
     * Local JBList cell renderer
     */
    private static class ListCellRenderer extends DefaultListCellRenderer {
        @NotNull
        @Override
        public Component getListCellRendererComponent(@NotNull JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            TableFile vFile = (TableFile) value;
            setText(vFile.getFilePath());
            setIcon(vFile.getIcon());
            return this;
        }
    }

}