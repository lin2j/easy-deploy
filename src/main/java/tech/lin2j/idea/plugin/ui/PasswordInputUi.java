package tech.lin2j.idea.plugin.ui;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import java.awt.BorderLayout;

/**
 * @author linjinjia
 * @date 2022/7/1 21:51
 */
public class PasswordInputUi extends DialogWrapper {
    private JPasswordField passwordInput;
    private JPanel mainPanel;
    private JButton okButton;

    private final String[] password;

    public PasswordInputUi(String[] password) {
        super(true);
        if (password == null) {
            throw new NullPointerException();
        }
        if (password.length < 1) {
            throw new IllegalArgumentException("length less than 1: " + password.length);
        }
        this.password = password;
        setTitle("Password");
        uiInit();
        init();
    }

    private void uiInit() {
        okButton.addActionListener(e -> {
            password[0] = new String(passwordInput.getPassword());
            close(OK_EXIT_CODE);
        });
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel dialog = new JPanel(new BorderLayout());
        mainPanel.setVisible(true);
        dialog.add(mainPanel, BorderLayout.CENTER);
        return dialog;
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[]{};
    }
}