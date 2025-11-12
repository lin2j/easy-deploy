package tech.lin2j.idea.plugin.model;

/**
 * 复选框列表项数据模型
 */
public class CheckboxListItem {
    private String text;
    private boolean selected;
    
    public CheckboxListItem(String text) {
        this.text = text;
        this.selected = false;
    }
    
    public CheckboxListItem(String text, boolean selected) {
        this.text = text;
        this.selected = selected;
    }
    
    // Getters and Setters
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CheckboxListItem that = (CheckboxListItem) obj;
        return text.equals(that.text);
    }
    
    @Override
    public int hashCode() {
        return text.hashCode();
    }
    
    @Override
    public String toString() {
        return text;
    }
}