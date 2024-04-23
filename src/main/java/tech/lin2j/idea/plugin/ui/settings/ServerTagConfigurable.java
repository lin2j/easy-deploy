package tech.lin2j.idea.plugin.ui.settings;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import icons.MyIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;
import tech.lin2j.idea.plugin.domain.model.event.TableRefreshEvent;
import tech.lin2j.idea.plugin.event.ApplicationContext;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;
import java.util.Objects;

/**
 * @author linjinjia
 * @date 2024/4/21 20:44
 */
public class ServerTagConfigurable implements SearchableConfigurable, Configurable.NoScroll {

    private static final Icon TAG = MyIcons.TAG;

    private final JBList<String> tagList;
    private final List<String> tagData;

    public ServerTagConfigurable() {
        tagData = ConfigHelper.getServerTags();
        tagList = new JBList<>(new CollectionListModel<>(tagData));
        tagList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setIcon(TAG);
                setText(Objects.toString(value));
                return this;
            }
        });
    }

    @NotNull
    @Override
    public String getId() {
        return "Server Tag";
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Server Tag";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        JPanel patternsPanel = new JPanel(new BorderLayout());
        patternsPanel.setBorder(IdeBorderFactory.createTitledBorder("Server Tag:", false, JBUI.insetsTop(8)).setShowLine(false));
        patternsPanel.add(ToolbarDecorator.createDecorator(tagList)
                .setToolbarPosition(ActionToolbarPosition.TOP)
                .setAddAction(button -> addTag())
                .setEditAction(button -> editSelectedTag())
                .setRemoveAction(button -> removeTag())
                .disableUpDownActions().createPanel(), BorderLayout.CENTER);
        return FormBuilder.createFormBuilder()
                .addComponentFillVertically(patternsPanel, 0)
                .getPanel();
    }

    protected void stopEditing() {

    }

    protected void addTag() {
        String tag = Messages.showInputDialog("new tag", "New Tag", TAG);
        if (StringUtil.isEmpty(tag)) {
            return;
        }
        if (isRepeat(tag)) {
            Messages.showErrorDialog("Duplicate tag definition", "Tag");
            return;
        }
        tagData.add(tag);
        tagList.setModel(new CollectionListModel<>(tagData));
        sendTagRefreshEvent();
    }

    protected void removeTag() {
        int selectedIndex = tagList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= tagData.size()) {
            return;
        }
        tagData.remove(selectedIndex);
        tagList.setModel(new CollectionListModel<>(tagData));
        sendTagRefreshEvent();
    }

    protected void editSelectedTag() {
        int selected = tagList.getSelectedIndex();
        String tag = tagData.get(selected);
        String newTag = Messages.showInputDialog("edit tag", "Edit Tag", TAG, tag,  null);
        if (Objects.equals(tag, newTag)) {
            return;
        }
        tagData.set(selected, newTag);
        tagList.setModel(new CollectionListModel<>(tagData));
        sendTagRefreshEvent();
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }

    private boolean isRepeat(String tag) {
        return tagData.contains(tag);
    }

    public void sendTagRefreshEvent() {
        TableRefreshEvent event = new TableRefreshEvent();
        event.setTagRefresh(true);
        ApplicationContext.getApplicationContext().publishEvent(event);
    }
}