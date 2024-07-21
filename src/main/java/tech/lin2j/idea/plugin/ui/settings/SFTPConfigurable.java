package tech.lin2j.idea.plugin.ui.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.ColorPanel;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.border.IdeaTitledBorder;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.PluginSetting;
import tech.lin2j.idea.plugin.enums.SFTPAction;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Objects;

import static tech.lin2j.idea.plugin.enums.Constant.DEFAULT_TOP_INSET;

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
        return MessagesBundle.getText("setting.item.sftp");
    }

    @Override
    public @Nullable JComponent createComponent() {
        JPanel panel = FormBuilder.createFormBuilder()
                .addComponent(mouseControl())
                .addComponent(transferControl(), DEFAULT_TOP_INSET)
                .getPanel();
        JPanel result = new JPanel(new BorderLayout());
        result.add(panel, BorderLayout.NORTH);
        return result;
    }

    @Override
    public void reset() {
        doubleClickAction.setSelectedItem(setting.getDoubleClickAction());
        uploadColorPicker.setSelectedColor(setting.uploadProgressColor());
        downloadColorPicker.setSelectedColor(setting.downloadProgressColor());
    }

    @Override
    public boolean isModified() {
        return !Objects.equals(uploadColorPicker.getSelectedColor(), setting.uploadProgressColor())
                || !Objects.equals(downloadColorPicker.getSelectedColor(), setting.downloadProgressColor())
                || !Objects.equals(doubleClickAction.getSelectedItem(), setting.getDoubleClickAction());
    }

    @Override
    public void apply() {
        setting.setDoubleClickAction((SFTPAction) doubleClickAction.getSelectedItem());

        Color upColor = uploadColorPicker.getSelectedColor();
        if (upColor != null) {
            setting.setUploadProgressColor(ColorUtil.toHex(upColor));
        }
        Color downColor = downloadColorPicker.getSelectedColor();
        if (downColor != null) {
            setting.setDownloadProgressColor(ColorUtil.toHex(downColor));
        }
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
        uploadColorPicker.setSelectedColor(setting.uploadProgressColor());
        JPanel up = new JPanel(new BorderLayout());
        up.add(uploadColorPicker, BorderLayout.WEST);

        downloadColorPicker = new ColorPanel();
        downloadColorPicker.setSelectedColor(setting.downloadProgressColor());
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