package tech.lin2j.idea.plugin.file;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import org.junit.Test;
import tech.lin2j.idea.plugin.file.filter.ExtExcludeFilter;
import tech.lin2j.idea.plugin.ssh.CommandLog;

import java.util.concurrent.FutureTask;

/**
 * @author linjinjia
 * @date 2022/12/11 00:00
 */
public class FilterTest {

    @Test
    public void testExtensionFilter() {
        String extensions = "*.iml;.log;****.bat";
        ExtExcludeFilter fileFilter = new ExtExcludeFilter(extensions, new TestCommandLog());
        String[] suffix = {"bat", "log", "iml"};
        for (String s : suffix) {
            assert fileFilter.getExtensionSet().contains(s);
        }
        assert !fileFilter.accept("abc.iml");
        assert fileFilter.accept("abc.txt");
        assert fileFilter.accept("abc.");
    }

    private class TestCommandLog implements CommandLog {

        @Override
        public ConsoleView getConsole() {
            return null;
        }

        @Override
        public void print(String msg, ConsoleViewContentType contentType) {

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
}