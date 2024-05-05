package tech.lin2j.idea.plugin.ui.dialog;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * @author linjinjia
 * @date 2024/5/5 01:11
 */
public class PasswordInputDialog extends DialogWrapper {

    private final JPanel root;

    protected PasswordInputDialog(StringBuilder password) {
        super(true);


        root = FormBuilder.createFormBuilder()
                .getPanel();

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return root;
    }
}