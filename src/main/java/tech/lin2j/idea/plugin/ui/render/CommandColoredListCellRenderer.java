package tech.lin2j.idea.plugin.ui.render;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.domain.model.Command;

import javax.swing.JList;
import java.util.Objects;

/**
 * @author linjinjia
 * @date 2024/4/27 11:08
 */
public class CommandColoredListCellRenderer extends ColoredListCellRenderer<Command> {

    public static final String TEXT_PADDING = "    ";

    @Override
    protected void customizeCellRenderer(@NotNull JList<? extends Command> list, Command value,
                                         int index, boolean selected, boolean hasFocus) {
        setIcon(AllIcons.Debugger.Console);
        if (StringUtil.isNotEmpty(value.getTitle())) {
            append(value.getTitle());
            append(TEXT_PADDING);
        }
        append(Objects.toString(value), SimpleTextAttributes.GRAY_ATTRIBUTES);
    }
}