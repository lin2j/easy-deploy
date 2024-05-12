package tech.lin2j.idea.plugin.module;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.JBSplitter;
import tech.lin2j.idea.plugin.ui.log.ConsoleLogViewer;

/**
 * @author linjinjia
 * @date 2022/4/29 09:59
 */
public class CommandExecuteView extends SimpleToolWindowPanel {

    private final ConsoleLogViewer consoleLogViewer;

    private final Project project;

    public CommandExecuteView(Project project) {
        super(false, true);

        this.project = project;
        this.consoleLogViewer = new ConsoleLogViewer(project);

        JBSplitter splitter = new JBSplitter(false);
        splitter.setSplitterProportionKey("main.splitter.key");
        splitter.setFirstComponent(consoleLogViewer.getRoot());
        splitter.setProportion(0.3f);
        setContent(splitter);
    }

    public ConsoleLogViewer getCommandLogViewer() {
        return consoleLogViewer;
    }

    public Project getProject() {
        return project;
    }
}