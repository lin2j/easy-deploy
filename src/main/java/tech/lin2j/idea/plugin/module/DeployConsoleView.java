package tech.lin2j.idea.plugin.module;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.JBSplitter;
import tech.lin2j.idea.plugin.ui.DashboardView;

/**
 * @author linjinjia
 * @date 2022/4/24 17:01
 */
public class DeployConsoleView extends SimpleToolWindowPanel {

    private final Project project;
    private final DashboardView consoleUi;

    public DeployConsoleView(Project project) {
        super(false, true);
        this.project = project;
        this.consoleUi = new DashboardView(project);

        JBSplitter splitter = new JBSplitter(false);
        splitter.setSplitterProportionKey("main.splitter.key");
        splitter.setFirstComponent(consoleUi);
        splitter.setProportion(0.3f);
        setContent(splitter);
    }

    public Project getProject() {
        return project;
    }

    public DashboardView getConsoleUi() {
        return consoleUi;
    }
}