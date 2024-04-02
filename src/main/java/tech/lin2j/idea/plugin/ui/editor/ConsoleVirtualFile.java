package tech.lin2j.idea.plugin.ui.editor;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.ui.ftp.FTPConsoleUi;

import javax.swing.JPanel;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author linjinjia
 * @date 2024/3/31 02:29
 */
public class ConsoleVirtualFile extends VirtualFile {
    private final String name;
    private final Project project;
    private final JPanel consolePanel;

    @Override
    public @NotNull FileType getFileType() {
        return new ConsoleFileType();
    }

    public ConsoleVirtualFile(String name, Project project, FTPConsoleUi ftpConsoleUi) {
        this.project = project;
        this.name = name;
        this.consolePanel = ftpConsoleUi.getMainPanel();
    }

    public Project getProject() {
        return project;
    }

    @Override
    public @NotNull
    String getName() {
        return name;
    }

    @Override
    public @NotNull
    VirtualFileSystem getFileSystem() {
        return ConsoleFileSystem.getInstance(project);
    }

    @Override
    public @NonNls
    @NotNull String getPath() {
        return name;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public VirtualFile getParent() {
        return null;
    }

    @Override
    public VirtualFile[] getChildren() {
        return new VirtualFile[0];
    }

    @Override
    public @NotNull
    OutputStream getOutputStream(Object o, long l, long l1) throws IOException {
        throw new UnsupportedOperationException("Unsupported Operation");
    }

    @Override
    public byte @NotNull [] contentsToByteArray() throws IOException {
        return new byte[0];
    }

    @Override
    public long getTimeStamp() {
        return 0;
    }

    @Override
    public long getLength() {
        return 0;
    }

    @Override
    public void refresh(boolean b, boolean b1, @Nullable Runnable runnable) {

    }

    @Override
    public @NotNull InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public long getModificationStamp() {
        return 0;
    }

    public JPanel getConsolePanel() {
        return this.consolePanel;
    }
}