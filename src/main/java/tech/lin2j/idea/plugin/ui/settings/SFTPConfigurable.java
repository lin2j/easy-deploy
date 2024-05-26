package tech.lin2j.idea.plugin.ui.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.ColorPanel;
import com.intellij.ui.border.IdeaTitledBorder;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.PluginSetting;
import tech.lin2j.idea.plugin.enums.SFTPAction;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Objects;

/**
 * @author linjinjia
 * @date 2024/5/26 15:25
 */
public class SFTPConfigurable implements SearchableConfigurable, Configurable.NoScroll {
    private final PluginSetting setting = ConfigHelper.pluginSetting();

    private ComboBox<SFTPAction> doubleClickAction;
    private ColorPanel uploadColorPicker;
    private ColorPanel downloadColorPicker;

    @Override
    public @NotNull
    @NonNls String getId() {
        return "ED-SFTP";
    }

    @Override
    public String getDisplayName() {
        return "SFTP";
    }

    @Override
    public @Nullable JComponent createComponent() {
        JPanel panel = FormBuilder.createFormBuilder()
                .addComponent(mouseControl())
                .addComponent(transferControl(), 10)
                .getPanel();
        JPanel result = new JPanel(new BorderLayout());
        result.add(panel, BorderLayout.NORTH);
        return result;
    }

    @Override
    public void reset() {
        doubleClickAction.setSelectedItem(setting.getDoubleClickAction());
        uploadColorPicker.setSelectedColor(setting.getUploadProgressColor());
        downloadColorPicker.setSelectedColor(setting.getDownloadProgressColor());
    }

    @Override
    public boolean isModified() {
        return !Objects.equals(uploadColorPicker.getSelectedColor(), setting.getUploadProgressColor())
                || !Objects.equals(downloadColorPicker.getSelectedColor(), setting.getDownloadProgressColor())
                || !Objects.equals(doubleClickAction.getSelectedItem(), setting.getDoubleClickAction());
    }

    @Override
    public void apply() throws ConfigurationException {
        setting.setDoubleClickAction((SFTPAction) doubleClickAction.getSelectedItem());
        setting.setUploadProgressColor(uploadColorPicker.getSelectedColor());
        setting.setDownloadProgressColor(downloadColorPicker.getSelectedColor());
    }

    private JPanel mouseControl() {
        String title = MessagesBundle.getText("setting.sftp.mouse.title");
        String text = MessagesBundle.getText("setting.sftp.mouse.double-click");

        doubleClickAction = new ComboBox<>();
        doubleClickAction.setModel(new CollectionComboBoxModel<>(SFTPAction.asList()));

        JPanel panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(text, doubleClickAction)
                .getPanel();
        panel.setBorder(new IdeaTitledBorder(title, 0, JBUI.emptyInsets()));

        return panel;
    }

    private JPanel transferControl() {
        String title = MessagesBundle.getText("setting.sftp.transfer.title");
        String uploadText = MessagesBundle.getText("setting.sftp.transfer.upload-progress");
        String downloadText = MessagesBundle.getText("setting.sftp.transfer.download-progress");

        uploadColorPicker = new ColorPanel();
        uploadColorPicker.setSize(new Dimension(40, 0));
        uploadColorPicker.setSelectedColor(setting.getUploadProgressColor());
        JPanel up = new JPanel(new BorderLayout());
        up.add(uploadColorPicker, BorderLayout.WEST);

        downloadColorPicker = new ColorPanel();
        downloadColorPicker.setSelectedColor(setting.getDownloadProgressColor());
        JPanel down = new JPanel(new BorderLayout());
        down.add(downloadColorPicker, BorderLayout.WEST);

        JPanel panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(uploadText, up)
                .addLabeledComponent(downloadText, down)
                .getPanel();
        panel.setBorder(new IdeaTitledBorder(title, 0, JBUI.emptyInsets()));

        return panel;
    }
}