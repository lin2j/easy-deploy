package tech.lin2j.idea.plugin.ui.render;

import com.intellij.ui.ColoredListCellRenderer;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ssh.SshServer;

import javax.swing.JList;
import java.util.function.Supplier;

/**
 * @author linjinjia
 * @date 2024/5/14 23:35
 */
public class HostComboBoxRenderer extends ColoredListCellRenderer<SshServer> {
    private final Supplier<String> highlightTextSupplier;

    public HostComboBoxRenderer(Supplier<String> highlightTextSupplier) {
        this.highlightTextSupplier = highlightTextSupplier;
    }

    @Override
    protected void customizeCellRenderer(@NotNull JList<? extends SshServer> list,
                                         SshServer value, int index, boolean selected, boolean hasFocus) {
        append(value.toString());
    }
}