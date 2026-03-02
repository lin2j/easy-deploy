package tech.lin2j.idea.plugin.ssh;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Session logger implementation of CommandLog interface.
 * Logs SSH console commands to files for later review.
 *
 * @author linjinjia
 * @date 2024/7/28 12:00
 */
public class SessionLogger implements CommandLog {

    private static final Logger log = LoggerFactory.getLogger(SessionLogger.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    private final ConsoleView console;
    private final String serverInfo;
    private final File logFile;
    private final AtomicInteger taskCount = new AtomicInteger(0);
    private PrintWriter fileWriter;

    public SessionLogger(Project project, SshServer server, String logDirectory) {
        this.console = null;
        this.serverInfo = server.getUsername() + "@" + server.getIp() + ":" + server.getPort();
        this.logFile = createLogFile(project, logDirectory, serverInfo);
        initializeFileWriter();
    }

    public SessionLogger(ConsoleView console, String sessionId) {
        this.console = console;
        this.serverInfo = sessionId;
        this.logFile = null;
    }

    private void initializeFileWriter() {
        if (logFile != null) {
            try {
                FileUtil.createParentDirs(logFile);
                fileWriter = new PrintWriter(new FileWriter(logFile, true), true);
                fileWriter.println("=== Session started at " + new Date() + " ===");
            } catch (IOException e) {
                log.error("Failed to initialize session log file: {}", e.getMessage());
            }
        }
    }

    private File createLogFile(Project project, String logDirectory, String serverInfo) {
        String expandedDir = FileUtil.expandUserHome(logDirectory);
        File dir = new File(expandedDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = DATE_FORMAT.format(new Date()) + "_" + serverInfo.replace("[@:,]", "_") + ".log";
        return new File(dir, fileName);
    }

    @Override
    public ConsoleView getConsole() {
        return console;
    }

    @Override
    public void print(String msg, ConsoleViewContentType contentType) {
        if (console != null) {
            console.print(msg, contentType);
        }
        writeToFile(msg);
    }

    @Override
    public void addTask(FutureTask<?> task) {
        taskCount.incrementAndGet();
    }

    @Override
    public void deleteTask(FutureTask<?> task) {
        taskCount.decrementAndGet();
    }

    @Override
    public void stopAllTasks() {
        taskCount.set(0);
    }

    @Override
    public int taskNum() {
        return taskCount.get();
    }

    private void writeToFile(String msg) {
        if (fileWriter != null) {
            fileWriter.print(msg);
            fileWriter.flush();
        }
    }

    public void close() {
        if (fileWriter != null) {
            fileWriter.println("=== Session ended at " + new Date() + " ===");
            fileWriter.println();
            fileWriter.close();
            fileWriter = null;
        }
    }

    public File getLogFile() {
        return logFile;
    }
}