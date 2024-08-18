package tech.lin2j.idea.plugin.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.util.NlsActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.function.Supplier;

/**
 * @author linjinjia
 * @date 2024/8/18 18:21
 */
public abstract class NewUpdateThreadAction extends AnAction {

    public NewUpdateThreadAction() {
    }

    public NewUpdateThreadAction(@Nullable Icon icon) {
        super(icon);
    }

    public NewUpdateThreadAction(@Nullable @NlsActions.ActionText String text) {
        super(text);
    }

    public NewUpdateThreadAction(@NotNull Supplier<@NlsActions.ActionText String> dynamicText) {
        super(dynamicText);
    }

    public NewUpdateThreadAction(@Nullable @NlsActions.ActionText String text, @Nullable @NlsActions.ActionDescription String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    public NewUpdateThreadAction(@NotNull @NlsActions.ActionText Supplier<String> text, @Nullable @NlsActions.ActionDescription Supplier<String> description, @Nullable Supplier<? extends @Nullable Icon> icon) {
        super(text, description, icon);
    }

    public NewUpdateThreadAction(@NotNull @NlsActions.ActionText Supplier<String> text, @NotNull @NlsActions.ActionDescription Supplier<String> description) {
        super(text, description);
    }

    public NewUpdateThreadAction(@NotNull Supplier<@NlsActions.ActionText String> dynamicText, @Nullable Icon icon) {
        super(dynamicText, icon);
    }

    public NewUpdateThreadAction(@NotNull Supplier<@NlsActions.ActionText String> dynamicText, @NotNull Supplier<@NlsActions.ActionDescription String> dynamicDescription, @Nullable Icon icon) {
        super(dynamicText, dynamicDescription, icon);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}