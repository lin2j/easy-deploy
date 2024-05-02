package tech.lin2j.idea.plugin.ui.editor;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * @author linjinjia
 * @date 2024/4/22 21:56
 */
public class SFTPFileSystem extends VirtualFileSystem {

    public static SFTPFileSystem getInstance(Project project) {
        return project.getService(SFTPFileSystem.class);
    }

    public void openEditor(SFTPVirtualFile SFTPVirtualFile) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(SFTPVirtualFile.getProject());
        fileEditorManager.openFile(SFTPVirtualFile, true);
    }

    @Override
    public @NonNls
    @NotNull String getProtocol() {
        return "SFTP";
    }

    @Override
    public @Nullable VirtualFile findFileByPath(@NotNull @NonNls String s) {
        return null;
    }

    @Override
    public void refresh(boolean b) {

    }

    @Override
    public @Nullable VirtualFile refreshAndFindFileByPath(@NotNull String s) {
        return null;
    }

    @Override
    public void addVirtualFileListener(@NotNull VirtualFileListener virtualFileListener) {

    }

    @Override
    public void removeVirtualFileListener(@NotNull VirtualFileListener virtualFileListener) {

    }

    @Override
    protected void deleteFile(Object o, @NotNull VirtualFile virtualFile) throws IOException {

    }

    @Override
    protected void moveFile(Object o, @NotNull VirtualFile virtualFile, @NotNull VirtualFile virtualFile1) throws IOException {

    }

    @Override
    protected void renameFile(Object o, @NotNull VirtualFile virtualFile, @NotNull String s) throws IOException {

    }

    @Override
    protected @NotNull VirtualFile createChildFile(Object o, @NotNull VirtualFile virtualFile, @NotNull String s) throws IOException {
        return null;
    }

    @Override
    protected @NotNull VirtualFile createChildDirectory(Object o, @NotNull VirtualFile virtualFile, @NotNull String s) throws IOException {
        return null;
    }

    @Override
    protected @NotNull VirtualFile copyFile(Object o, @NotNull VirtualFile virtualFile, @NotNull VirtualFile virtualFile1, @NotNull String s) throws IOException {
        return null;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }
}
