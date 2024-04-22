package tech.lin2j.idea.plugin.ui.settings;

import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.domain.model.ConfigHelper;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.List;

/**
 * @author linjinjia
 * @date 2024/4/21 20:44
 */
public class ServerTagConfigurable implements SearchableConfigurable, Configurable.NoScroll {

    private final JBList<String> tagList;
    private final List<String> tagData;

    public ServerTagConfigurable() {
        tagData = ConfigHelper.getServerTags();
        tagList = new JBList<>(new CollectionListModel<>(tagData));
        tagList.setCellRenderer(new DefaultListCellRenderer() {

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
                .setAddAction(new AnActionButtonRunnable() {
                    @Override
                    public void run(AnActionButton button) {
                        stopEditing();
                        String tag = Messages.showInputDialog("new tag", "New Tag", null);
                        if (StringUtil.isEmpty(tag)) {
                            return;
                        }
                        if (!isUnique(tag)) {
                            Messages.showErrorDialog("Duplicate tag definition", "Tag");
                            return;
                        }
                        tagData.add(tag);
                        tagList.setModel(new CollectionListModel<>(tagData));
                    }
                })
                .setEditAction(new AnActionButtonRunnable() {
                    @Override
                    public void run(AnActionButton button) {
                        editSelectedTag();
                    }
                })
                .setRemoveAction(new AnActionButtonRunnable() {
                    @Override
                    public void run(AnActionButton button) {
                        stopEditing();
                        int selectedIndex = tagList.getSelectedIndex();
                        if (selectedIndex < 0 || selectedIndex >= tagData.size()) {
                            return;
                        }
                        tagData.remove(selectedIndex);
                    }
                })
                .disableUpDownActions().createPanel(), BorderLayout.CENTER);
        return FormBuilder.createFormBuilder()
                .addComponentFillVertically(patternsPanel, 0)
                .getPanel();
    }

    protected void stopEditing() {

    }

    protected void editSelectedTag() {
        int selected = tagList.getSelectedIndex();
        String tag = tagData.get(selected);
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }

    private boolean isUnique(String tag) {
        return tagData.contains(tag);
    }
}