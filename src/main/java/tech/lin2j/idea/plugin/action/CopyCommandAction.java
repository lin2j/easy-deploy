package tech.lin2j.idea.plugin.action;

import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.util.ui.TextTransferable;
import icons.MyIcons;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.model.Command;

import java.util.function.Supplier;

/**
 * @author linjinjia
 * @date 2024/6/9 16:09
 */
public class CopyCommandAction extends NewUpdateThreadAction {

    private final Supplier<Command> provider;

    public CopyCommandAction(Command command) {
        this(() -> command);
    }

    public CopyCommandAction(Supplier<Command> provider) {
        super("Copy Command", "Copy command", MyIcons.Actions.Copy);
        this.provider = provider;

    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (provider.get() == null) {
            return;
        }
        Gson gson = new Gson();
        String json = gson.toJson(provider.get());
        CopyPasteManager.getInstance().setContents(new TextTransferable(json));
    }
}