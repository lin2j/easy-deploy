package tech.lin2j.idea.plugin.action;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.ui.Messages;
import icons.MyIcons;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.model.UploadProfile;

import javax.swing.SwingUtilities;
import java.awt.datatransfer.DataFlavor;
import java.util.function.Consumer;

/**
 * @author linjinjia
 * @date 2024/6/9 16:12
 */
public class PasteUploadProfileAction extends NewUpdateThreadAction {

    private final Consumer<UploadProfile> consumer;

    public PasteUploadProfileAction(Consumer<UploadProfile> consumer) {
        super("Paste Profile", "Paste profile", MyIcons.Actions.Paste);
        this.consumer = consumer;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            String json = CopyPasteManager.getInstance().getContents(DataFlavor.stringFlavor);
            if (StringUtils.isBlank(json)) {
                return;
            }
            Gson gson = new Gson();
            UploadProfile up = gson.fromJson(json, UploadProfile.class);
            if (up == null) {
                return;
            }
            up.setCommandId(null);
            consumer.accept(up);
        } catch (JsonSyntaxException err) {
            SwingUtilities.invokeLater(() -> {
                Messages.showErrorDialog("Json parse exception", "Parse Error");
            });
        }

    }
}