package tech.lin2j.idea.plugin.ui.dialog;

import com.intellij.json.JsonFileType;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.AdditionalPageAtBottomEditorCustomization;
import com.intellij.ui.EditorCustomization;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.EditorTextFieldProvider;
import com.intellij.ui.SoftWrapsEditorCustomization;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import java.awt.Dimension;
import java.util.HashSet;
import java.util.Set;

/**
 * @author linjinjia
 * @date 2024/7/19 23:32
 */
public class ImportConfigPreviewDialog extends DialogWrapper {
    private final Project project;
    private final String content;
    private EditorTextField editor;

    public ImportConfigPreviewDialog(@Nullable Project project, String content) {
        super(project);
        this.project = project;
        this.content = content;

        initEditor();

        setTitle("Import Configuration Preview");
        setOKButtonText("Import");
        init();
    }

    @Nullable
    @Override
    public JComponent createCenterPanel() {
        return editor;
    }

    private void initEditor() {
        Set<EditorCustomization> features = new HashSet<>();
        features.add(SoftWrapsEditorCustomization.ENABLED);
        features.add(AdditionalPageAtBottomEditorCustomization.DISABLED);
        features.add(editor -> editor.setBackgroundColor(null));
        features.add((editor -> editor.getSettings().setLineNumbersShown(true)));

        Language json = JsonFileType.INSTANCE.getLanguage();
        editor = EditorTextFieldProvider.getInstance().getEditorField(json, project, features);

        // Global editor color scheme is set by EditorTextField logic.
        // We also need to use font from it and not from the current LaF.
        editor.setFontInheritedFromLAF(false);
        editor.setPreferredSize(new Dimension(650, 750));
        editor.setText(content);
    }
}