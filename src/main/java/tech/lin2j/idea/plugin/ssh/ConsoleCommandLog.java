package tech.lin2j.idea.plugin.ssh;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;

import java.util.concurrent.FutureTask;

/**
 * It provides methods to print messages to the console but does not implement task management methods.
 *
 * @author linjinjia
 * @date 2024/11/28 22:14
 */
public class ConsoleCommandLog implements CommandLog {
    private final ConsoleView console;

    public ConsoleCommandLog(ConsoleView console) {
        this.console = console;
    }

    @Override
    public ConsoleView getConsole() {
        return console;
    }

    @Override
    public void print(String msg, ConsoleViewContentType contentType) {
        console.print(msg, contentType);
    }

    @Override
    public void addTask(FutureTask<?> task) {

    }

    @Override
    public void deleteTask(FutureTask<?> task) {

    }

    @Override
    public void stopAllTasks() {

    }

    @Override
    public int taskNum() {
        return 0;
    }
}
