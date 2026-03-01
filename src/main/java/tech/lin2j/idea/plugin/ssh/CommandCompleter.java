package tech.lin2j.idea.plugin.ssh;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author lin2j
 * @date 2026-03-01 13:33
 */
public class CommandCompleter {
    private final CommandHistoryManager historyManager;
    private final Set<String> commandSuggestions;
    private final Set<String> fileSuggestions;
    private final Set<String> directorySuggestions;

    public CommandCompleter(CommandHistoryManager historyManager) {
        this.historyManager = historyManager;
        this.commandSuggestions = new TreeSet<>();
        this.fileSuggestions = new TreeSet<>();
        this.directorySuggestions = new TreeSet<>();
        initializeCommonCommands();
    }

    /**
     * Initialize common Linux/Unix commands
     */
    private void initializeCommonCommands() {
        // Basic commands
        commandSuggestions.add("ls");
        commandSuggestions.add("cd");
        commandSuggestions.add("pwd");
        commandSuggestions.add("mkdir");
        commandSuggestions.add("rm");
        commandSuggestions.add("cp");
        commandSuggestions.add("mv");
        commandSuggestions.add("cat");
        commandSuggestions.add("echo");
        commandSuggestions.add("touch");
        commandSuggestions.add("chmod");
        commandSuggestions.add("chown");
        commandSuggestions.add("grep");
        commandSuggestions.add("find");
        commandSuggestions.add("locate");
        commandSuggestions.add("which");
        commandSuggestions.add("whereis");
        commandSuggestions.add("man");
        commandSuggestions.add("info");
        commandSuggestions.add("help");
        
        // File operations
        commandSuggestions.add("head");
        commandSuggestions.add("tail");
        commandSuggestions.add("less");
        commandSuggestions.add("more");
        commandSuggestions.add("nano");
        commandSuggestions.add("vim");
        commandSuggestions.add("vi");
        commandSuggestions.add("emacs");
        
        // System commands
        commandSuggestions.add("ps");
        commandSuggestions.add("top");
        commandSuggestions.add("htop");
        commandSuggestions.add("df");
        commandSuggestions.add("du");
        commandSuggestions.add("free");
        commandSuggestions.add("uname");
        commandSuggestions.add("uptime");
        commandSuggestions.add("w");
        commandSuggestions.add("who");
        commandSuggestions.add("last");
        
        // Network commands
        commandSuggestions.add("ping");
        commandSuggestions.add("traceroute");
        commandSuggestions.add("netstat");
        commandSuggestions.add("ss");
        commandSuggestions.add("ip");
        commandSuggestions.add("ifconfig");
        commandSuggestions.add("route");
        commandSuggestions.add("curl");
        commandSuggestions.add("wget");
        commandSuggestions.add("ssh");
        commandSuggestions.add("scp");
        commandSuggestions.add("rsync");
        
        // Package management
        commandSuggestions.add("apt");
        commandSuggestions.add("apt-get");
        commandSuggestions.add("yum");
        commandSuggestions.add("dnf");
        commandSuggestions.add("pacman");
        commandSuggestions.add("zypper");
        
        // Process management
        commandSuggestions.add("kill");
        commandSuggestions.add("killall");
        commandSuggestions.add("pkill");
        commandSuggestions.add("nice");
        commandSuggestions.add("renice");
        
        // Archive commands
        commandSuggestions.add("tar");
        commandSuggestions.add("gzip");
        commandSuggestions.add("gunzip");
        commandSuggestions.add("zip");
        commandSuggestions.add("unzip");
        commandSuggestions.add("bzip2");
        commandSuggestions.add("bunzip2");
        
        // System administration
        commandSuggestions.add("sudo");
        commandSuggestions.add("su");
        commandSuggestions.add("passwd");
        commandSuggestions.add("useradd");
        commandSuggestions.add("usermod");
        commandSuggestions.add("userdel");
        commandSuggestions.add("groupadd");
        commandSuggestions.add("groupmod");
        commandSuggestions.add("groupdel");
        
        // File system
        commandSuggestions.add("mount");
        commandSuggestions.add("umount");
        commandSuggestions.add("df");
        commandSuggestions.add("fsck");
        commandSuggestions.add("mkfs");
        
        // Compression
        commandSuggestions.add("compress");
        commandSuggestions.add("uncompress");
        commandSuggestions.add("zcat");
        commandSuggestions.add("bzcat");
        
        // Text processing
        commandSuggestions.add("awk");
        commandSuggestions.add("sed");
        commandSuggestions.add("sort");
        commandSuggestions.add("uniq");
        commandSuggestions.add("cut");
        commandSuggestions.add("paste");
        commandSuggestions.add("join");
        commandSuggestions.add("tr");
        commandSuggestions.add("wc");
        
        // Shell built-ins
        commandSuggestions.add("export");
        commandSuggestions.add("alias");
        commandSuggestions.add("unalias");
        commandSuggestions.add("source");
        commandSuggestions.add(".");
        commandSuggestions.add("exit");
        commandSuggestions.add("logout");
        commandSuggestions.add("history");
        commandSuggestions.add("fc");
        
        // Common flags
        commandSuggestions.add("-h");
        commandSuggestions.add("--help");
        commandSuggestions.add("-v");
        commandSuggestions.add("--version");
        commandSuggestions.add("-a");
        commandSuggestions.add("--all");
        commandSuggestions.add("-l");
        commandSuggestions.add("--long");
        commandSuggestions.add("-r");
        commandSuggestions.add("--recursive");
        commandSuggestions.add("-f");
        commandSuggestions.add("--force");
        commandSuggestions.add("-i");
        commandSuggestions.add("--interactive");
        commandSuggestions.add("-n");
        commandSuggestions.add("--dry-run");
        commandSuggestions.add("-p");
        commandSuggestions.add("--preserve");
        commandSuggestions.add("-t");
        commandSuggestions.add("--test");
        commandSuggestions.add("-d");
        commandSuggestions.add("--debug");
        commandSuggestions.add("-q");
        commandSuggestions.add("--quiet");
        commandSuggestions.add("-s");
        commandSuggestions.add("--silent");
        commandSuggestions.add("-c");
        commandSuggestions.add("--config");
        commandSuggestions.add("-o");
        commandSuggestions.add("--output");
        commandSuggestions.add("-e");
        commandSuggestions.add("--error");
    }

    /**
     * Get suggestions for the current input
     * @param input the current input text
     * @return list of suggestions
     */
    public List<String> getSuggestions(String input) {
        List<String> suggestions = new ArrayList<>();
        
        if (input == null || input.trim().isEmpty()) {
            return suggestions;
        }
        
        // Check if input is a command or part of a command
        String[] parts = input.split("\\s+");
        String lastPart = parts[parts.length - 1];
        
        if (parts.length == 1) {
            // Suggest commands
            for (String command : commandSuggestions) {
                if (command.startsWith(lastPart)) {
                    suggestions.add(command);
                }
            }
        } else {
            // Suggest flags or parameters
            if (lastPart.startsWith("-")) {
                // Suggest flags
                for (String command : commandSuggestions) {
                    if (command.startsWith(lastPart)) {
                        suggestions.add(command);
                    }
                }
            } else {
                // Suggest files/directories (simplified)
                for (String file : fileSuggestions) {
                    if (file.startsWith(lastPart)) {
                        suggestions.add(file);
                    }
                }
                for (String dir : directorySuggestions) {
                    if (dir.startsWith(lastPart)) {
                        suggestions.add(dir);
                    }
                }
            }
        }
        
        return suggestions;
    }

    /**
     * Get suggestions based on command history
     * @param input the current input text
     * @return list of suggestions from history
     */
    public List<String> getHistorySuggestions(String input) {
        List<String> suggestions = new ArrayList<>();
        
        if (input == null || input.trim().isEmpty()) {
            return suggestions;
        }
        
        List<String> history = historyManager.getHistory();
        for (String command : history) {
            if (command.startsWith(input)) {
                suggestions.add(command);
            }
        }
        
        return suggestions;
    }

    /**
     * Add a custom command suggestion
     * @param command the command to add
     */
    public void addCommandSuggestion(String command) {
        if (command != null && !command.trim().isEmpty()) {
            commandSuggestions.add(command.trim());
        }
    }

    /**
     * Add a file suggestion
     * @param file the file name to add
     */
    public void addFileSuggestion(String file) {
        if (file != null && !file.trim().isEmpty()) {
            fileSuggestions.add(file.trim());
        }
    }

    /**
     * Add a directory suggestion
     * @param directory the directory name to add
     */
    public void addDirectorySuggestion(String directory) {
        if (directory != null && !directory.trim().isEmpty()) {
            directorySuggestions.add(directory.trim());
        }
    }

    /**
     * Clear all suggestions
     */
    public void clearSuggestions() {
        commandSuggestions.clear();
        fileSuggestions.clear();
        directorySuggestions.clear();
    }

    /**
     * Get all command suggestions
     * @return set of command suggestions
     */
    public Set<String> getCommandSuggestions() {
        return new TreeSet<>(commandSuggestions);
    }

    /**
     * Get all file suggestions
     * @return set of file suggestions
     */
    public Set<String> getFileSuggestions() {
        return new TreeSet<>(fileSuggestions);
    }

    /**
     * Get all directory suggestions
     * @return set of directory suggestions
     */
    public Set<String> getDirectorySuggestions() {
        return new TreeSet<>(directorySuggestions);
    }
}