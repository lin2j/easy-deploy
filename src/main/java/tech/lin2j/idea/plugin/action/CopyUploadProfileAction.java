package tech.lin2j.idea.plugin.action;

import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.util.ui.TextTransferable;
import icons.MyIcons;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.model.UploadProfile;

import java.util.function.Supplier;

/**
 * @author linjinjia
 * @date 2024/6/9 16:09
 */
public class CopyUploadProfileAction extends AnAction {

    private final Supplier<UploadProfile> provider;

    public CopyUploadProfileAction(UploadProfile profile) {
        this(() -> profile);
    }

    public CopyUploadProfileAction(Supplier<UploadProfile> provider) {
        super("Copy Profile", "Copy profile", MyIcons.Actions.Copy);
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