package tech.lin2j.idea.plugin.file;

import com.intellij.openapi.progress.util.ColorProgressBar;
import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.xfer.TransferListener;

/**
 * @author linjinjia
 * @date 2024/4/13 16:09
 */
public class ProgressTransferListener implements TransferListener {

    private final ColorProgressBar progress;

    private final String relPath;

    public ProgressTransferListener(ColorProgressBar progress) {
        this("", progress);
    }

    private ProgressTransferListener(String relPath, ColorProgressBar progress) {
        this.relPath = relPath;
        this.progress = progress;
    }

    @Override
    public TransferListener directory(String name) {
        return new ProgressTransferListener(relPath + name + "/", progress);
    }

    @Override
    public StreamCopier.Listener file(final String name, final long size) {
        final String path = relPath + name;
        return transferred -> {
            double percent = 0;
            if (size > 0) {
                percent = transferred / (double) size;
            }
            progress.setFraction(percent);
        };
    }

    ;
}