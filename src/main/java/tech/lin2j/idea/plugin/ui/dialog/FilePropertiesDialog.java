package tech.lin2j.idea.plugin.ui.dialog;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.file.LocalTableFile;
import tech.lin2j.idea.plugin.file.RemoteTableFile;
import tech.lin2j.idea.plugin.file.TableFile;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author linjinjia 
 * @date 2024/5/24 21:42
 */
public class FilePropertiesDialog extends DialogWrapper {

    private final TableFile file;
    private final JPanel root;

    private JPanel header;
    private JPanel body;

    public FilePropertiesDialog(@NotNull TableFile file) {
        super(true);
        this.file = file;

        initHeader();
        initBody();

        root = FormBuilder.createFormBuilder()
                .addComponent(header)
                .addComponent(body)
                .getPanel();

        setSize(400, 0);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return root;
    }

    private void initHeader() {
        JBLabel nameLabel = new JBLabel(file.getName());
        nameLabel.setFont(UIUtil.getLabelFont().deriveFont(Font.PLAIN, 16));

        JBLabel iconLabel = new JBLabel();
        Icon icon = IconUtil.scale(file.getIcon(), iconLabel,3f);
        iconLabel.setIcon(icon);

        header = FormBuilder.createFormBuilder()
                .setHorizontalGap(52)
                .addLabeledComponent(iconLabel, nameLabel)
                .getPanel();
    }

    private void initBody() {
        FormBuilder formBuilder  = FormBuilder.createFormBuilder()
                .setHorizontalGap(50)
                .setVerticalGap(15)
                .addSeparator()
                .addLabeledComponent("File type", new JBLabel(file.getType()))
                .addLabeledComponent("Path", new JBLabel(file.getFilePath()))
                .addLabeledComponent("Size", new JBLabel(file.getSize()))
                .addLabeledComponent("Modified", new JBLabel(file.getModified()));

        if (file instanceof RemoteTableFile) {
            formBuilder.addSeparator();
            formBuilder.addLabeledComponent("Owner", new JBLabel(file.getOwner()));
            formBuilder.addLabeledComponent("Group", new JBLabel(file.getGroup()));
            formBuilder.addLabeledComponent("Permission", new JBLabel(file.getAccess()));
        }

        if (file instanceof LocalTableFile) {
            formBuilder.addSeparator();
            formBuilder.addLabeledComponent("Permission", localPermission());
        }

        // BLANK
        formBuilder.addComponent(new JBLabel());

        body = formBuilder.getPanel();
    }

    private JPanel localPermission() {
        JPanel panel = new JPanel(new GridLayout(1, 2));

        JBCheckBox readOnly = new JBCheckBox("Read only");
        readOnly.setSelected(file.readOnly());

        JBCheckBox hidden = new JBCheckBox("Hidden");
        hidden.setSelected(file.isHidden());

        panel.add(readOnly);
        panel.add(hidden);

        return panel;
    }
}
