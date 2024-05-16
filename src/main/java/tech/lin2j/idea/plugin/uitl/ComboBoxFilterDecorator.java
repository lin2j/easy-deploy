package tech.lin2j.idea.plugin.uitl;


import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.JBColor;
import tech.lin2j.idea.plugin.ui.editor.FilterEditor;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author linjinjia
 * @date 2024/5/14 23:24
 */
public class ComboBoxFilterDecorator<T> {
    private final JComboBox<T> comboBox;
    private final BiPredicate<T, String> userFilter;
    private final Function<T, String> comboDisplayTextMapper;
    private List<T> originalItems;
    private Object selectedItem;
    private FilterEditor<T> filterEditor;

    public ComboBoxFilterDecorator(JComboBox<T> comboBox,
                                   BiPredicate<T, String> userFilter,
                                   Function<T, String> comboDisplayTextMapper) {
        this.comboBox = comboBox;
        this.userFilter = userFilter;
        this.comboDisplayTextMapper = comboDisplayTextMapper;
    }

    public static <T> ComboBoxFilterDecorator<T> decorate(JComboBox<T> comboBox,
                                                          Function<T, String> comboDisplayTextMapper,
                                                          BiPredicate<T, String> userFilter) {
        ComboBoxFilterDecorator<T> decorator =
                new ComboBoxFilterDecorator<>(comboBox, userFilter, comboDisplayTextMapper);
        decorator.init();
        return decorator;
    }

    private void init() {
        prepareComboFiltering();
        initComboPopupListener();
        initComboKeyListener();
    }

    private void prepareComboFiltering() {
        CollectionComboBoxModel<T> model = (CollectionComboBoxModel<T>) comboBox.getModel();
        this.originalItems = new ArrayList<>();
        for (int i = 0; i < model.getSize(); i++) {
            this.originalItems.add(model.getElementAt(i));
        }


        filterEditor = new FilterEditor<>(comboDisplayTextMapper, new Consumer<Boolean>() {
            //editing mode (commit/cancel) change listener
            @Override
            public void accept(Boolean aBoolean) {
                if (aBoolean) {//commit
                    selectedItem = comboBox.getSelectedItem();
                } else {//rollback to the last one
                    comboBox.setSelectedItem(selectedItem);
                    filterEditor.setItem(selectedItem);
                }
            }
        });

        JLabel filterLabel = filterEditor.getFilterLabel();
        filterLabel.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                filterLabel.setBorder(BorderFactory.createLoweredBevelBorder());
            }

            @Override
            public void focusLost(FocusEvent e) {
                filterLabel.setBorder(UIManager.getBorder("TextField.border"));
                resetFilterComponent();
            }
        });
        comboBox.setEditor(filterEditor);
        comboBox.setEditable(true);
    }

    private void initComboKeyListener() {
        filterEditor.getFilterLabel().addKeyListener(
                new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        char keyChar = e.getKeyChar();
                        if (!Character.isDefined(keyChar)) {
                            return;
                        }
                        int keyCode = e.getKeyCode();
                        switch (keyCode) {
                            case KeyEvent.VK_DELETE:
                                return;
                            case KeyEvent.VK_ENTER:
                                selectedItem = comboBox.getSelectedItem();
                                resetFilterComponent();
                                return;
                            case KeyEvent.VK_ESCAPE:
                                resetFilterComponent();
                                return;
                            case KeyEvent.VK_BACK_SPACE:
                                filterEditor.removeCharAtEnd();
                                break;
                            default:
                                filterEditor.addChar(keyChar);
                        }
                        if (!comboBox.isPopupVisible()) {
                            comboBox.showPopup();
                        }
                        if (filterEditor.isEditing() && filterEditor.getText().length() > 0) {
                            applyFilter();
                        } else {
                            comboBox.hidePopup();
                            resetFilterComponent();
                        }
                    }
                }
        );
    }

    public Supplier<String> getFilterTextSupplier() {
        return () -> {
            if (filterEditor.isEditing()) {
                return filterEditor.getFilterLabel().getText();
            }
            return "";
        };
    }

    private void initComboPopupListener() {
        comboBox.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                resetFilterComponent();
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                resetFilterComponent();
            }
        });
    }

    private void resetFilterComponent() {
        if (!filterEditor.isEditing()) {
            return;
        }
        //restore original order
        CollectionComboBoxModel<T> model = (CollectionComboBoxModel<T>) comboBox.getModel();
        model.removeAll();
        for (T item : originalItems) {
            model.add(item);
        }
        filterEditor.reset();
    }

    private void applyFilter() {
        CollectionComboBoxModel<T> model = (CollectionComboBoxModel<T>) comboBox.getModel();
        model.removeAll();
        List<T> filteredItems = new ArrayList<>();
        //add matched items at top
        for (T item : originalItems) {
            if (userFilter.test(item, filterEditor.getFilterLabel().getText())) {
                model.add(item);
            } else {
                filteredItems.add(item);
            }
        }

        //red color when no match
        filterEditor.getFilterLabel()
                .setForeground(model.getSize() == 0 ?
                        JBColor.RED : UIManager.getColor("Label.foreground"));
        //add unmatched items
        filteredItems.forEach(model::add);
    }
}