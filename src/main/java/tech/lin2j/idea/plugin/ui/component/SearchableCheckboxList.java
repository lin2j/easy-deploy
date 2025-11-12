package tech.lin2j.idea.plugin.ui.component;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.OnePixelDivider;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * IDEA风格的可搜索复选框下拉列表组件（修复宽度和高度问题）
 */
public class SearchableCheckboxList extends JPanel {
    private JPanel contentPanel;
    private SearchTextField searchField;
    private JButton toggleButton;
    private JBPopup popup;
    private JPanel listPanel;
    private List<CheckboxListItem> items;
    private List<CheckboxListItem> filteredItems;

    private JCheckBox selectAllCheckBox;
    private JPanel selectAllPanel;

    private boolean showSelectAllOption = true;
    private int maxDisplayItems = 8;

    // 固定高度和宽度控制
    private static final int ITEM_HEIGHT = 28;
    private static final int POPUP_MIN_WIDTH = 200;
    private static final int POPUP_MAX_HEIGHT = 300;

    // IDEA样式常量
    private static final Color BACKGROUND_COLOR = UIUtil.getPanelBackground();
    private static final Color BORDER_COLOR = OnePixelDivider.BACKGROUND;
    private static final Color HOVER_COLOR = UIUtil.getListSelectionBackground(true);
    private static final Color SELECTED_COLOR = UIUtil.getListSelectionBackground(false);
    private static final Color CHIP_BACKGROUND = UIUtil.getToolTipBackground();
    private static final Color CHIP_BORDER = JBColor.border();

    public SearchableCheckboxList() {
        this.items = new ArrayList<>();
        this.filteredItems = new ArrayList<>();
        initComponents();
        setupLayout();
    }

    public SearchableCheckboxList(List<String> itemNames) {
        this();
        setItems(itemNames);
    }

    private void initComponents() {
        setOpaque(true);
        setBackground(UIUtil.getPanelBackground());

        // 内容面板
        contentPanel = new JPanel(new WrappedFlowLayout(FlowLayout.LEFT, 4, 4));
        contentPanel.setOpaque(true);
        contentPanel.setBackground(UIUtil.getTextFieldBackground());
        contentPanel.setBorder(JBUI.Borders.empty(4));

        // 搜索框
        searchField = new SearchTextField(false) {

            @Override
            public void addNotify() {
                super.addNotify();
                setOpaque(false);
                getTextEditor().setOpaque(false);
                getTextEditor().setBorder(JBUI.Borders.empty());
            }
        };

        searchField.getTextEditor().setBorder(JBUI.Borders.empty());
        searchField.getTextEditor().getDocument().addDocumentListener(new SearchDocumentListener());
        searchField.setPreferredSize(JBUI.size(120, 24));

        // 添加点击事件
        contentPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (popup == null || !popup.isVisible()) {
                    showPopup();
                }
            }
        });

        // 下拉按钮
        toggleButton = new JButton(AllIcons.General.ArrowDown);
        toggleButton.setBorder(JBUI.Borders.empty(4));
        toggleButton.setContentAreaFilled(false);
        toggleButton.setFocusable(false);
        toggleButton.addActionListener(e -> togglePopup());

        // 列表面板 - 使用固定高度的布局
        listPanel = new JPanel(new VerticalListLayout());
        listPanel.setOpaque(true);
        listPanel.setBackground(UIUtil.getListBackground());

        // 全选面板
        if (showSelectAllOption) {
            selectAllPanel = new JPanel(new BorderLayout());
            selectAllPanel.setOpaque(true);
            selectAllPanel.setBackground(UIUtil.getListBackground());
            selectAllPanel.setBorder(BorderFactory.createCompoundBorder(
                    new CustomLineBorder(JBUI.insetsBottom(1), OnePixelDivider.BACKGROUND),
                    JBUI.Borders.empty(6, 8)
            ));
            selectAllPanel.setPreferredSize(new Dimension(0, ITEM_HEIGHT));

            selectAllCheckBox = new JCheckBox("Select All");
            selectAllCheckBox.setOpaque(false);
            selectAllCheckBox.setFont(UIUtil.getLabelFont());
            selectAllCheckBox.addActionListener(e -> {
                boolean selected = selectAllCheckBox.isSelected();
                if (searchField.getText().isEmpty()) {
                    items.forEach(item -> item.setSelected(selected));
                } else {
                    filteredItems.forEach(item -> item.setSelected(selected));
                }
                refreshListPanel();
                updateContentPanel();
            });

            selectAllPanel.add(selectAllCheckBox, BorderLayout.WEST);
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // 主内容区域
        contentPanel.add(searchField.getTextEditor());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(true);
        mainPanel.setBackground(UIUtil.getTextFieldBackground());
        mainPanel.setBorder(JBUI.Borders.customLine(BORDER_COLOR));

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(toggleButton, BorderLayout.EAST);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void togglePopup() {
        if (popup != null && popup.isVisible()) {
            popup.cancel();
        } else {
            showPopup();
        }
    }

    private void showPopup() {
        if (items.isEmpty()) return;

        refreshListPanel();

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(true);
        content.setBackground(UIUtil.getListBackground());

        if (showSelectAllOption && selectAllPanel != null && searchField.getText().isEmpty()) {
            content.add(selectAllPanel, BorderLayout.NORTH);
        }

        // 设置列表面板的固定高度
        int itemCount = Math.min(filteredItems.size(), maxDisplayItems);
        int listHeight = itemCount * ITEM_HEIGHT;
        listPanel.setPreferredSize(new Dimension(0, listHeight));

        JBScrollPane scrollPane = new JBScrollPane(listPanel);
        scrollPane.setBorder(JBUI.Borders.empty());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        content.add(scrollPane, BorderLayout.CENTER);

        // 计算弹出框的合适尺寸
        int popupWidth = Math.max(getWidth(), POPUP_MIN_WIDTH);
        int popupHeight = calculatePopupHeight(itemCount);

        content.setPreferredSize(new Dimension(popupWidth, popupHeight));

        popup = JBPopupFactory.getInstance().createComponentPopupBuilder(content, searchField.getTextEditor())
                .setRequestFocus(true)
                .setResizable(false) // 禁止调整大小，保持宽度一致
                .setMovable(true)
                .setFocusable(true)
                .setCancelOnClickOutside(true)
                .setCancelOnWindowDeactivation(true)
                .setCancelKeyEnabled(true)
                .createPopup();

        popup.showUnderneathOf(this);
    }

    private int calculatePopupHeight(int itemCount) {
        int baseHeight = itemCount * ITEM_HEIGHT;
        if (showSelectAllOption && searchField.getText().isEmpty()) {
            baseHeight += ITEM_HEIGHT; // 加上全选面板的高度
        }
        return Math.min(baseHeight + 10, POPUP_MAX_HEIGHT); // 加上一些边距，但不超过最大高度
    }

    private class SearchDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            filterItems();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            filterItems();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            filterItems();
        }

        private void filterItems() {
            String searchText = searchField.getText().toLowerCase().trim();
            filteredItems.clear();

            if (searchText.isEmpty()) {
                filteredItems.addAll(items);
            } else {
                filteredItems.addAll(items.stream()
                        .filter(item -> item.getText().toLowerCase().contains(searchText))
                        .collect(Collectors.toList()));
            }

            refreshListPanel();

            // 如果弹出框可见，更新其大小
            if (popup != null && popup.isVisible()) {
                SwingUtilities.invokeLater(() -> {
                    Component content = popup.getContent();
                    if (content != null) {
                        int itemCount = Math.min(filteredItems.size(), maxDisplayItems);
                        int newHeight = calculatePopupHeight(itemCount);
                        content.setPreferredSize(new Dimension(getWidth(), newHeight));
                        popup.pack(false, true); // 只调整高度，不调整宽度
                    }
                });
            }
        }
    }

    private void refreshListPanel() {
        listPanel.removeAll();

        for (CheckboxListItem item : filteredItems) {
            JPanel itemPanel = createListItemPanel(item);
            listPanel.add(itemPanel);
        }

        // 更新全选状态
        if (showSelectAllOption && selectAllCheckBox != null) {
            List<CheckboxListItem> targetItems = searchField.getText().isEmpty() ? items : filteredItems;
            if (targetItems.isEmpty()) {
                selectAllCheckBox.setSelected(false);
            } else {
                long selectedCount = targetItems.stream().filter(CheckboxListItem::isSelected).count();
                selectAllCheckBox.setSelected(selectedCount == targetItems.size());
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createListItemPanel(CheckboxListItem item) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(true);
        panel.setBackground(item.isSelected() ? SELECTED_COLOR : UIUtil.getListBackground());
        panel.setBorder(JBUI.Borders.empty(4, 8));
        panel.setPreferredSize(new Dimension(0, ITEM_HEIGHT)); // 固定高度
        panel.setMinimumSize(new Dimension(0, ITEM_HEIGHT));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, ITEM_HEIGHT));

        JCheckBox checkBox = new JCheckBox(item.getText());
        checkBox.setSelected(item.isSelected());
        checkBox.setOpaque(false);
        checkBox.setFont(UIUtil.getListFont());
        checkBox.setFocusable(false);

        checkBox.addActionListener(e -> {
            item.setSelected(checkBox.isSelected());
            updateContentPanel();
            refreshListPanel();
        });

        panel.add(checkBox, BorderLayout.WEST);

        // 悬停效果
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(item.isSelected() ? SELECTED_COLOR : UIUtil.getListBackground());
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                checkBox.setSelected(!checkBox.isSelected());
                item.setSelected(checkBox.isSelected());
                updateContentPanel();
                refreshListPanel();
            }
        });

        return panel;
    }

    private void updateContentPanel() {
        contentPanel.removeAll();

        // 添加选中的内容块
        for (String itemText : getSelectedItems()) {
            JPanel chipPanel = createChipPanel(itemText);
            contentPanel.add(chipPanel);
        }

        // 添加搜索框
        contentPanel.add(searchField.getTextEditor());

        contentPanel.revalidate();
        contentPanel.repaint();

        // 更新工具提示
        List<String> selectedItems = getSelectedItems();
        if (selectedItems.isEmpty()) {
            setToolTipText("No items selected");
        } else {
            setToolTipText("Selected: " + String.join(", ", selectedItems));
        }
    }

    private JPanel createChipPanel(String text) {
        JPanel chipPanel = new JPanel(new BorderLayout(4, 0));
        chipPanel.setOpaque(true);
        chipPanel.setBackground(CHIP_BACKGROUND);
        chipPanel.setBorder(BorderFactory.createCompoundBorder(
                JBUI.Borders.customLine(CHIP_BORDER, 1),
                JBUI.Borders.empty(2, 6, 2, 4)
        ));

        JBLabel label = new JBLabel(text);
        label.setFont(UIUtil.getLabelFont().deriveFont(UIUtil.getFontSize(UIUtil.FontSize.SMALL)));

        JLabel deleteButton = new JLabel(AllIcons.Actions.Close);
        deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // 悬停效果
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                chipPanel.setBackground(UIUtil.getListSelectionBackground(true));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                chipPanel.setBackground(CHIP_BACKGROUND);
            }
        };

        chipPanel.addMouseListener(adapter);
        deleteButton.addMouseListener(adapter);

        // 删除事件
        deleteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                items.stream()
                        .filter(item -> item.getText().equals(text))
                        .findFirst()
                        .ifPresent(item -> item.setSelected(false));
                updateContentPanel();
                refreshListPanel();
            }
        });

        chipPanel.add(label, BorderLayout.CENTER);
        chipPanel.add(deleteButton, BorderLayout.EAST);

        return chipPanel;
    }

    // ========== 公共API ==========

    public void setItems(List<String> itemNames) {
        items.clear();
        for (String name : itemNames) {
            items.add(new CheckboxListItem(name));
        }
        filteredItems.clear();
        filteredItems.addAll(items);
        refreshListPanel();
        updateContentPanel();
    }

    public void addItem(String itemName) {
        CheckboxListItem item = new CheckboxListItem(itemName);
        items.add(item);
        if (searchField.getText().isEmpty() ||
                itemName.toLowerCase().contains(searchField.getText().toLowerCase())) {
            filteredItems.add(item);
        }
        refreshListPanel();
    }

    public void removeItem(String itemName) {
        items.removeIf(item -> item.getText().equals(itemName));
        filteredItems.removeIf(item -> item.getText().equals(itemName));
        refreshListPanel();
        updateContentPanel();
    }

    public List<String> getSelectedItems() {
        return items.stream()
                .filter(CheckboxListItem::isSelected)
                .map(CheckboxListItem::getText)
                .collect(Collectors.toList());
    }

    public void setSelectedItems(List<String> selectedNames) {
        items.forEach(item -> item.setSelected(selectedNames.contains(item.getText())));
        refreshListPanel();
        updateContentPanel();
    }

    public void selectAll() {
        items.forEach(item -> item.setSelected(true));
        refreshListPanel();
        updateContentPanel();
    }

    public void clearSelection() {
        items.forEach(item -> item.setSelected(false));
        refreshListPanel();
        updateContentPanel();
    }

    public int getItemCount() {
        return items.size();
    }

    public int getSelectedCount() {
        return (int) items.stream().filter(CheckboxListItem::isSelected).count();
    }

    public boolean containsItem(String itemName) {
        return items.stream().anyMatch(item -> item.getText().equals(itemName));
    }

    public void setShowSelectAllOption(boolean show) {
        this.showSelectAllOption = show;
    }

    public void setMaxDisplayItems(int maxDisplayItems) {
        this.maxDisplayItems = maxDisplayItems;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        contentPanel.setEnabled(enabled);
        searchField.setEnabled(enabled);
        toggleButton.setEnabled(enabled);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension preferred = super.getPreferredSize();
        return new Dimension(Math.max(preferred.width, POPUP_MIN_WIDTH), preferred.height);
    }
}

/**
 * 垂直列表布局，固定每个项目的高度
 */
class VerticalListLayout implements LayoutManager {
    @Override
    public void addLayoutComponent(String name, Component comp) {}

    @Override
    public void removeLayoutComponent(Component comp) {}

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int width = 0;
            int height = 0;

            for (int i = 0; i < parent.getComponentCount(); i++) {
                Component comp = parent.getComponent(i);
                if (comp.isVisible()) {
                    Dimension dim = comp.getPreferredSize();
                    width = Math.max(width, dim.width);
                    height += dim.height;
                }
            }

            return new Dimension(
                    width + insets.left + insets.right,
                    height + insets.top + insets.bottom
            );
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int x = insets.left;
            int y = insets.top;
            int width = parent.getWidth() - insets.left - insets.right;

            for (int i = 0; i < parent.getComponentCount(); i++) {
                Component comp = parent.getComponent(i);
                if (comp.isVisible()) {
                    Dimension pref = comp.getPreferredSize();
                    comp.setBounds(x, y, width, pref.height);
                    y += pref.height;
                }
            }
        }
    }
}

// 其他辅助类保持不变（CheckboxListItem, WrappedFlowLayout, CustomLineBorder）
class CheckboxListItem {
    private String text;
    private boolean selected;

    public CheckboxListItem(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CheckboxListItem && text.equals(((CheckboxListItem) obj).text);
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }
}

/**
 * 自动换行布局
 */
class WrappedFlowLayout extends FlowLayout {
    public WrappedFlowLayout() {
        super();
    }
    
    public WrappedFlowLayout(int align) {
        super(align);
    }
    
    public WrappedFlowLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }
    
    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }
    
    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getSize().width;
            if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;
            
            int hgap = getHgap();
            int vgap = getVgap();
            Insets insets = target.getInsets();
            int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);
            
            int x = 0, y = insets.top + vgap, rowHeight = 0;
            
            for (int i = 0; i < target.getComponentCount(); i++) {
                Component comp = target.getComponent(i);
                if (comp.isVisible()) {
                    Dimension dim = preferred ? comp.getPreferredSize() : comp.getMinimumSize();
                    
                    if (x == 0 || x + dim.width <= maxWidth) {
                        if (x > 0) x += hgap;
                        x += dim.width;
                        rowHeight = Math.max(rowHeight, dim.height);
                    } else {
                        x = dim.width;
                        y += vgap + rowHeight;
                        rowHeight = dim.height;
                    }
                }
            }
            
            y += rowHeight + insets.bottom;
            return new Dimension(targetWidth, y);
        }
    }
}

/**
 * 自定义边框用于分隔线
 */
class CustomLineBorder implements Border {
    private final Insets insets;
    private final Color color;
    
    public CustomLineBorder(Insets insets, Color color) {
        this.insets = insets;
        this.color = color;
    }
    
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.setColor(color);
        if (insets.top > 0) g.fillRect(x, y, width, insets.top);
        if (insets.left > 0) g.fillRect(x, y, insets.left, height);
        if (insets.bottom > 0) g.fillRect(x, y + height - insets.bottom, width, insets.bottom);
        if (insets.right > 0) g.fillRect(x + width - insets.right, y, insets.right, height);
    }
    
    @Override
    public Insets getBorderInsets(Component c) {
        return insets;
    }
    
    @Override
    public boolean isBorderOpaque() {
        return true;
    }
}