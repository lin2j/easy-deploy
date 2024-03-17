package tech.lin2j.idea.plugin.module;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.JBSplitter;
import tech.lin2j.idea.plugin.ui.CommandLogViewer;

/**
 * @author linjinjia
 * @date 2022/4/29 09:59
 */
public class CommandExecuteView extends SimpleToolWindowPanel {

    private final CommandLogViewer commandLogViewer;

    private final Project project;

    public CommandExecuteView(Project project) {
        super(false, true);

        this.project = project;
        this.commandLogViewer = new CommandLogViewer(project);

        JBSplitter splitter = new JBSplitter(false);
        splitter.setSplitterProportionKey("main.splitter.key");
        splitter.setFirstComponent(commandLogViewer.getMainPanel());
        splitter.setProportion(0.3f);
        setContent(splitter);
    }

    public CommandLogViewer getCommandLogViewer() {
        return commandLogViewer;
    }

    public Project getProject() {
        return project;
    }
}