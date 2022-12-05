package tech.lin2j.idea.plugin.module;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.JBSplitter;
import tech.lin2j.idea.plugin.ui.ConsoleUi;

/**
 * @author linjinjia
 * @date 2022/4/24 17:01
 */
public class DeployConsoleView extends SimpleToolWindowPanel {

    private final Project project;
    private final ConsoleUi consoleUi;

    public DeployConsoleView(Project project) {
        super(false, true);
        this.project = project;
        this.consoleUi = new ConsoleUi(project);

        JBSplitter splitter = new JBSplitter(false);
        splitter.setSplitterProportionKey("main.splitter.key");
        splitter.setFirstComponent(consoleUi.getMainPanel());
        splitter.setProportion(0.3f);
        setContent(splitter);
    }

    public Project getProject() {
        return project;
    }

    public ConsoleUi getConsoleUi() {
        return consoleUi;
    }
}