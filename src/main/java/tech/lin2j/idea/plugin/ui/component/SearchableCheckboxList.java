package tech.lin2j.idea.plugin.ui.component;

import tech.lin2j.idea.plugin.model.CheckboxListItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 可搜索的带复选框下拉列表组件（选中项以内容块显示）
 */
public class SearchableCheckboxList extends JPanel {
    private JPanel contentPanel; // 替换原来的搜索框，用于显示内容块
    private JTextField searchField; // 用于输入搜索文本
    private JButton toggleButton;
    private JPopupMenu popupMenu;
    private JPanel listPanel;
    private List<CheckboxListItem> items;
    private List<CheckboxListItem> filteredItems;
    
    // 全选相关组件
    private JCheckBox selectAllCheckBox;
    private JPanel selectAllPanel;
    
    // 样式配置
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(200, 200, 200);
    private static final Color HOVER_COLOR = new Color(240, 240, 240);
    private static final Color SELECTED_COLOR = new Color(220, 240, 255);
    private static final Color CHIP_BACKGROUND = new Color(225, 245, 254);
    private static final Color CHIP_BORDER = new Color(179, 229, 252);
    private static final Font DEFAULT_FONT = new Font("Microsoft YaHei", Font.PLAIN, 12);
    private static final Font CHIP_FONT = new Font("Microsoft YaHei", Font.PLAIN, 11);
    
    // 配置选项
    private boolean showSelectAllOption = true;
    private int maxDisplayItems = 10;
    
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
    
    public SearchableCheckboxList(List<String> itemNames, boolean showSelectAll) {
        this();
        this.showSelectAllOption = showSelectAll;
        setItems(itemNames);
    }
    
    private void initComponents() {
        // 内容面板 - 用于显示选中的内容块和搜索框
        contentPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 5, 5));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        // 搜索框
        searchField = new JTextField();
        searchField.setFont(DEFAULT_FONT);
        searchField.setBorder(BorderFactory.createEmptyBorder());
        searchField.setOpaque(false);
        searchField.setPreferredSize(new Dimension(150, 25));
        searchField.getDocument().addDocumentListener(new SearchDocumentListener());
        
        // 添加搜索框到内容面板
        contentPanel.add(searchField);
        
        // 添加点击事件打开下拉框
        contentPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!popupMenu.isVisible()) {
                    showPopup();
                }
            }
        });
        
        // 下拉按钮
        toggleButton = new JButton("▼");
        toggleButton.setPreferredSize(new Dimension(30, 30));
        toggleButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
        toggleButton.addActionListener(e -> togglePopup());
        
        // 弹出菜单
        popupMenu = new JPopupMenu();
        popupMenu.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        // 列表面板
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(BACKGROUND_COLOR);
        
        // 全选面板
        if (showSelectAllOption) {
            selectAllPanel = new JPanel(new BorderLayout());
            selectAllPanel.setBackground(BACKGROUND_COLOR);
            selectAllPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(5, 5, 5, 5)
            ));
            
            selectAllCheckBox = new JCheckBox("全选");
            selectAllCheckBox.setFont(DEFAULT_FONT);
            selectAllCheckBox.setBackground(BACKGROUND_COLOR);
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
        
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        JPanel popupContent = new JPanel(new BorderLayout());
        if (showSelectAllOption) {
            popupContent.add(selectAllPanel, BorderLayout.NORTH);
        }
        popupContent.add(scrollPane, BorderLayout.CENTER);
        
        popupMenu.add(popupContent);
        
        // 添加全局点击监听器来关闭弹出菜单
        setupGlobalClickListener();
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(contentPanel, BorderLayout.CENTER);
        headerPanel.add(toggleButton, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.CENTER);
        setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
    }
    
    private void setupGlobalClickListener() {
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                Timer timer = new Timer(200, evt -> {
                    if (!isFocusOwner() && !isPopupChildFocused()) {
                        popupMenu.setVisible(false);
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }
        });
        
        // ESC键关闭
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .addKeyEventDispatcher(e -> {
                if (e.getID() == KeyEvent.KEY_PRESSED && 
                    e.getKeyCode() == KeyEvent.VK_ESCAPE && 
                    popupMenu.isVisible()) {
                    popupMenu.setVisible(false);
                    searchField.requestFocus();
                    return true;
                }
                return false;
            });
    }
    
    private boolean isPopupChildFocused() {
        Window[] windows = Window.getWindows();
        for (Window window : windows) {
            if (window instanceof JWindow && 
                ((JWindow) window).getContentPane().getComponentCount() > 0 &&
                ((JWindow) window).getContentPane().getComponent(0) == popupMenu) {
                return window.isFocused();
            }
        }
        return false;
    }
    
    private void togglePopup() {
        if (popupMenu.isVisible()) {
            popupMenu.setVisible(false);
        } else {
            showPopup();
        }
    }
    
    private void showPopup() {
        if (items.isEmpty()) return;
        
        Point location = getLocationOnScreen();
        popupMenu.show(this, 0, getHeight());
        popupMenu.setLocation(location.x, location.y + getHeight());
        
        int itemCount = Math.min(filteredItems.size(), maxDisplayItems);
        int itemHeight = 30;
        int totalHeight = itemCount * itemHeight + (showSelectAllOption ? 40 : 0);
        popupMenu.setPopupSize(300, Math.min(totalHeight, 300));
        
        searchField.requestFocus();
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
            
            if (showSelectAllOption && selectAllPanel != null) {
                selectAllPanel.setVisible(searchText.isEmpty());
            }
        }
    }
    
    private void refreshListPanel() {
        listPanel.removeAll();
        
        for (CheckboxListItem item : filteredItems) {
            JPanel itemPanel = createItemPanel(item);
            listPanel.add(itemPanel);
        }
        
        if (showSelectAllOption && selectAllCheckBox != null) {
            List<CheckboxListItem> targetItems = searchField.getText().isEmpty() ? items : filteredItems;
            if (targetItems.isEmpty()) {
                selectAllCheckBox.setSelected(false);
            } else {
                long selectedCount = targetItems.stream().filter(CheckboxListItem::isSelected).count();
                if (selectedCount == 0) {
                    selectAllCheckBox.setSelected(false);
                } else if (selectedCount == targetItems.size()) {
                    selectAllCheckBox.setSelected(true);
                } else {
                    selectAllCheckBox.setSelected(false);
                }
            }
        }
        
        listPanel.revalidate();
        listPanel.repaint();
    }
    
    private JPanel createItemPanel(CheckboxListItem item) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(item.isSelected() ? SELECTED_COLOR : BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        JCheckBox checkBox = new JCheckBox(item.getText());
        checkBox.setSelected(item.isSelected());
        checkBox.setFont(DEFAULT_FONT);
        checkBox.setBackground(panel.getBackground());
        checkBox.setOpaque(false);
        
        checkBox.addActionListener(e -> {
            item.setSelected(checkBox.isSelected());
            updateContentPanel();
            refreshListPanel();
        });
        
        panel.add(checkBox, BorderLayout.CENTER);
        
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!item.isSelected()) {
                    panel.setBackground(HOVER_COLOR);
                    checkBox.setBackground(HOVER_COLOR);
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(item.isSelected() ? SELECTED_COLOR : BACKGROUND_COLOR);
                checkBox.setBackground(panel.getBackground());
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
    
    /**
     * 更新显示选中的内容块
     */
    private void updateContentPanel() {
        // 移除所有内容块，保留搜索框
        contentPanel.removeAll();
        
        // 添加选中的内容块
        List<String> selectedItems = getSelectedItems();
        for (String itemText : selectedItems) {
            JPanel chipPanel = createChipPanel(itemText);
            contentPanel.add(chipPanel);
        }
        
        // 重新添加搜索框
        contentPanel.add(searchField);
        
        // 更新布局
        contentPanel.revalidate();
        contentPanel.repaint();
        
        // 设置工具提示
        if (selectedItems.isEmpty()) {
            setToolTipText("未选择任何项目");
        } else {
            setToolTipText("已选择: " + String.join(", ", selectedItems));
        }
    }
    
    /**
     * 创建内容块面板
     */
    private JPanel createChipPanel(String text) {
        JPanel chipPanel = new JPanel(new BorderLayout(5, 0));
        chipPanel.setBackground(CHIP_BACKGROUND);
        chipPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CHIP_BORDER, 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 2)
        ));
        chipPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // 文本标签
        JLabel label = new JLabel(text);
        label.setFont(CHIP_FONT);
        label.setForeground(Color.DARK_GRAY);
        
        // 删除按钮
        JLabel deleteButton = new JLabel("×");
        deleteButton.setFont(new Font("Arial", Font.BOLD, 14));
        deleteButton.setForeground(Color.GRAY);
        deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // 添加鼠标悬停效果
        MouseAdapter chipMouseAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                chipPanel.setBackground(new Color(200, 230, 255));
                deleteButton.setForeground(Color.RED);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                chipPanel.setBackground(CHIP_BACKGROUND);
                deleteButton.setForeground(Color.GRAY);
            }
        };
        
        chipPanel.addMouseListener(chipMouseAdapter);
        deleteButton.addMouseListener(chipMouseAdapter);
        
        // 删除按钮点击事件
        deleteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 取消选中对应的项目
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
    
    // ========== 公共方法 ==========
    
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
        items.forEach(item -> 
            item.setSelected(selectedNames.contains(item.getText())));
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
        if (selectAllPanel != null) {
            selectAllPanel.setVisible(show);
        }
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
}

/**
 * 自动换行的布局管理器
 */
class WrapLayout extends FlowLayout {
    public WrapLayout() {
        super();
    }
    
    public WrapLayout(int align) {
        super(align);
    }
    
    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }
    
    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }
    
    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension minimum = layoutSize(target, false);
        minimum.width -= (getHgap() + 1);
        return minimum;
    }
    
    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getSize().width;
            
            if (targetWidth == 0) {
                targetWidth = Integer.MAX_VALUE;
            }
            
            int hgap = getHgap();
            int vgap = getVgap();
            Insets insets = target.getInsets();
            int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);
            
            int x = 0;
            int y = insets.top + vgap;
            int rowHeight = 0;
            
            for (int i = 0; i < target.getComponentCount(); i++) {
                Component comp = target.getComponent(i);
                if (comp.isVisible()) {
                    Dimension dim = preferred ? comp.getPreferredSize() : comp.getMinimumSize();
                    
                    if (x == 0 || x + dim.width <= maxWidth) {
                        if (x > 0) {
                            x += hgap;
                        }
                        x += dim.width;
                        rowHeight = Math.max(rowHeight, dim.height);
                    } else {
                        x = dim.width;
                        y += vgap + rowHeight;
                        rowHeight = dim.height;
                    }
                }
            }
            
            y += rowHeight;
            y += insets.bottom;
            
            return new Dimension(targetWidth, y);
        }
    }
}