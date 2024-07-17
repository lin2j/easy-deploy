package tech.lin2j.idea.plugin.ui.render;

import com.intellij.ui.ColoredListCellRenderer;
import icons.MyIcons;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.model.UploadProfile;

import javax.swing.JList;

/**
 * @author linjinjia
 * @date 2024/4/27 11:08
 */
public class UploadProfileColoredListCellRenderer extends ColoredListCellRenderer<UploadProfile> {

    @Override
    protected void customizeCellRenderer(@NotNull JList<? extends UploadProfile> list, UploadProfile value,
                                         int index, boolean selected, boolean hasFocus) {
        setIcon(MyIcons.UploadProfile);
        append(value.getName());
    }
}