package tech.lin2j.idea.plugin.ssh;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.util.Key;

import java.util.concurrent.FutureTask;

/**
 *
 * @author linjinjia
 * @date 2024/11/28 22:14
 */
public interface CommandLog {
    Key<CommandLog> COMMAND_LOG_KEY = Key.create("ProjectCommandLog");

    ConsoleView getConsole();

    void print(String msg, ConsoleViewContentType contentType);

    void addTask(FutureTask<?> task);

    void deleteTask(FutureTask<?> task);

    void stopAllTasks();

    int taskNum();

    default void print(String msg) {
        print(msg, ConsoleViewContentType.NORMAL_OUTPUT);
    }

    default void info(String msg) {
        print("[INFO] ", ConsoleViewContentType.LOG_INFO_OUTPUT);
        if (msg != null && !msg.endsWith("\n")) {
            msg += "\n";
        }
        print(msg, ConsoleViewContentType.NORMAL_OUTPUT);
    }

    default void println(String msg) {
        print(msg + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
    }

    default void error(String msg) {
        print("[ERROR] ", ConsoleViewContentType.LOG_ERROR_OUTPUT);
        if (msg != null && !msg.endsWith("\n")) {
            msg += "\n";
        }
        print(msg, ConsoleViewContentType.ERROR_OUTPUT);
    }
}
