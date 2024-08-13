package tech.lin2j.idea.plugin.ui.render;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.model.Command;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.SeparatorCommand;
import tech.lin2j.idea.plugin.ssh.SshServer;

import javax.swing.JList;
import java.util.Objects;

/**
 * @author linjinjia
 * @date 2024/4/27 11:08
 */
public class CommandColoredListCellRenderer extends ColoredListCellRenderer<Command> {

    public static final String TEXT_PADDING = "    ";

    private final Integer sshId;

    public CommandColoredListCellRenderer(Integer sshId) {
        this.sshId = sshId;
    }

    @Override
    protected void customizeCellRenderer(@NotNull JList<? extends Command> list, Command value,
                                         int index, boolean selected, boolean hasFocus) {
        if (value instanceof SeparatorCommand) {
            append(value.getTitle(), SimpleTextAttributes.GRAY_ATTRIBUTES);
            return;
        }
        setIcon(AllIcons.Debugger.Console);
        if (StringUtil.isNotEmpty(value.getTitle())) {
            append(value.getTitle());
            append(TEXT_PADDING);
        }
        // server info
        if (!Objects.equals(sshId, value.getSshId()) && value.getSharable()) {
            SshServer server = ConfigHelper.getSshServerById(value.getSshId());
            if (server != null) {
                append("Shared by [" + server.getIp() + "]", SimpleTextAttributes.GRAY_ATTRIBUTES);
                append(TEXT_PADDING);
            }
        }
        append(Objects.toString(value), SimpleTextAttributes.GRAY_ATTRIBUTES);
    }
}