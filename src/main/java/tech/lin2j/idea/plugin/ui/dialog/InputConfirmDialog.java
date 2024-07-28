package tech.lin2j.idea.plugin.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Objects;

import static com.intellij.ui.SimpleTextAttributes.STYLE_BOLD;
import static com.intellij.ui.SimpleTextAttributes.STYLE_PLAIN;

public class InputConfirmDialog extends DialogWrapper {

    private static final SimpleTextAttributes RED = new SimpleTextAttributes(STYLE_BOLD, JBColor.RED);
    private static final SimpleTextAttributes GRAY = SimpleTextAttributes.GRAY_ATTRIBUTES;

    private final String message;
    private final String confirm;

    private JTextField input;
    private SimpleColoredComponent matcher;

    public InputConfirmDialog(Project project,
                              String title, String message, String confirm, String okText) {
        super(project);

        Objects.requireNonNull(confirm);

        this.message = message;
        this.confirm = confirm;

        initInput();

        setTitle(title);
        if (StringUtils.isNotBlank(okText)) {
            setOKButtonText(okText);
        }
        setOKActionEnabled(false);
        setSize(600, 400);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {

        return FormBuilder.createFormBuilder()
                .addComponent(new JBLabel(message))
                .addComponent(matcher)
                .addComponent(input)
                .getPanel();
    }

    private void initInput() {
        matcher = new SimpleColoredComponent();
        matcher.append(confirm, GRAY);

        input = new JTextField();
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                check();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                check();
            }

            private void check() {
                int match = match();
                matcher.clear();
                if (match == 0) {
                    matcher.append(confirm, GRAY);
                } else {
                    String first = input.getText();
                    matcher.append(first, RED);
                    if (first.length() < confirm.length()) {
                        String second = confirm.substring(first.length());
                        matcher.append(second, GRAY);
                    }
                }

                setOKActionEnabled(Objects.equals(input.getText(), confirm));
            }
        });
    }

    private int match() {
        String ipt = input.getText();
        if (ipt == null) {
            return 0;
        }
        if (ipt.length() > confirm.length()) {
            return 0;
        }
        int len = ipt.length();
        for (int i = 0; i < len; i++) {
            if (ipt.charAt(i) != confirm.charAt(i)) {
                return 0;
            }
        }
        return ipt.length();
    }
}
