package tech.lin2j.idea.plugin.ssh;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lin2j
 * @date 2026-03-01 13:30
 */
public class CommandHistoryManager {
    private final List<String> commandHistory;
    private int currentIndex;

    public CommandHistoryManager() {
        this.commandHistory = new ArrayList<>();
        this.currentIndex = -1;
    }

    /**
     * Add a command to the history
     * @param command the command to add
     */
    public void addCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return;
        }
        
        // Remove duplicates
        commandHistory.remove(command);
        
        // Add to history
        commandHistory.add(command);
        currentIndex = commandHistory.size();
    }

    /**
     * Get the previous command in history
     * @return the previous command or null if no previous command
     */
    public String getPreviousCommand() {
        if (currentIndex > 0) {
            currentIndex--;
            return commandHistory.get(currentIndex);
        }
        return null;
    }

    /**
     * Get the next command in history
     * @return the next command or null if no next command
     */
    public String getNextCommand() {
        if (currentIndex < commandHistory.size() - 1) {
            currentIndex++;
            return commandHistory.get(currentIndex);
        }
        return null;
    }

    /**
     * Reset the history index to the end
     */
    public void resetIndex() {
        currentIndex = commandHistory.size();
    }

    /**
     * Get all commands in history
     * @return list of commands
     */
    public List<String> getHistory() {
        return new ArrayList<>(commandHistory);
    }

    /**
     * Clear the command history
     */
    public void clearHistory() {
        commandHistory.clear();
        currentIndex = -1;
    }

    /**
     * Get the current index
     * @return current index
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * Set the current index
     * @param index the index to set
     */
    public void setCurrentIndex(int index) {
        if (index >= 0 && index <= commandHistory.size()) {
            currentIndex = index;
        }
    }
}