package tech.lin2j.idea.plugin.ui.editor;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author linjinjia
 * @date 2024/4/22 21:56
 */
public class SFTPEditorProvider implements FileEditorProvider, DumbAware {
    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return virtualFile instanceof SFTPVirtualFile;
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return new SFTPEditor(virtualFile);
    }

    @Override
    public void disposeEditor(@NotNull FileEditor editor) {
        FileEditorProvider.super.disposeEditor(editor);
    }

    @Override
    public @NotNull FileEditorState readState(@NotNull Element sourceElement, @NotNull Project project, @NotNull VirtualFile file) {
        return FileEditorProvider.super.readState(sourceElement, project, file);
    }

    @Override
    public void writeState(@NotNull FileEditorState state, @NotNull Project project, @NotNull Element targetElement) {
        FileEditorProvider.super.writeState(state, project, targetElement);
    }

    @Override
    public @NotNull
    @NonNls String getEditorTypeId() {
        return "Redis Console";
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }

}
