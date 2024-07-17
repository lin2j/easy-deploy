package tech.lin2j.idea.plugin.action;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.ui.Messages;
import icons.MyIcons;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.model.Command;

import javax.swing.SwingUtilities;
import java.awt.datatransfer.DataFlavor;
import java.util.function.Consumer;

/**
 * @author linjinjia
 * @date 2024/6/9 16:12
 */
public class PasteCommandAction extends AnAction {

    private final Consumer<Command> consumer;

    public PasteCommandAction(Consumer<Command> consumer) {
        super("Paste Command", "Paste command", MyIcons.Actions.Paste);
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
            Command cmd = gson.fromJson(json, Command.class);
            if (cmd == null) {
                return;
            }
            consumer.accept(cmd);
        } catch (JsonSyntaxException err) {
            SwingUtilities.invokeLater(() -> Messages.showErrorDialog("Json parse exception", "Parse Error"));
        }

    }
}