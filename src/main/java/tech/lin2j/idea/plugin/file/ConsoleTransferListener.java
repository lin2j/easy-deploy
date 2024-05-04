package tech.lin2j.idea.plugin.file;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.util.text.StringUtil;
import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.xfer.TransferListener;

import java.util.stream.Stream;

/**
 * @author linjinjia
 * @date 2024/4/13 16:09
 */
public class ConsoleTransferListener implements TransferListener {
    private final String relPath;
    private final ConsoleView consoleView;

    public ConsoleTransferListener(String relPath, ConsoleView consoleView) {
        if (!relPath.endsWith("/")) {
            relPath += "/";
        }
        this.relPath = relPath;
        this.consoleView = consoleView;
    }

    @Override
    public TransferListener directory(String name) {
        return new ConsoleTransferListener(relPath + name + "/", consoleView);
    }

    @Override
    public StreamCopier.Listener file(final String name, final long size) {
        final String path = relPath + name;
        print("Transfer file: " + path + ", Size: " + StringUtil.formatFileSize(size) + "\n");
        return transferred -> {
            double fileProgress = 0;
            if (size > 0) {
                fileProgress = transferred / (double) size;
            }
            boolean fileCompleted = Math.abs(1 - fileProgress) < 1e-6;
            printProgress((int) (fileProgress * 100), fileCompleted);

        };
    }

    private void printProgress(int complete, boolean completed) {
        StringBuilder sb = new StringBuilder("[");
        Stream.generate(() -> '#').limit(complete).forEach(sb::append);
        Stream.generate(() -> '_').limit(100 - complete).forEach(sb::append);
        sb.append("] ");
        if (completed) {
            sb.append("complete\n");
        } else {
            sb.append(complete).append("%");
        }
        print("\r");
        print(sb.toString());
    }

    private void print(String msg) {
        consoleView.print(msg, ConsoleViewContentType.NORMAL_OUTPUT);
    }
}