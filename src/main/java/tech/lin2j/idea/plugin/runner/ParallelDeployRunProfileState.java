package tech.lin2j.idea.plugin.runner;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.content.ContentManagerAdapter;
import com.intellij.ui.content.ContentManagerEvent;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.model.DeployProfile;
import tech.lin2j.idea.plugin.runner.process.ListExecutionResult;
import tech.lin2j.idea.plugin.runner.process.ListProcessHandler;
import tech.lin2j.idea.plugin.runner.process.UploadProcessHandler;
import tech.lin2j.idea.plugin.ssh.SshUploadTask;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author linjinjia
 * @date 2024/5/2 16:17
 */
public class ParallelDeployRunProfileState extends CommandLineState {

    private final List<String> deployProfiles;
    private final Executor executor;
    private final ExecutionEnvironment environment;
    private final Project project;

    protected ParallelDeployRunProfileState(Executor executor,
                                            ExecutionEnvironment environment,
                                            List<String> deployProfiles) {
        super(environment);
        this.deployProfiles = deployProfiles;
        this.executor = executor;
        this.environment = environment;
        this.project = environment.getProject();
    }


    @NotNull
    @Override
    public ExecutionResult execute(@NotNull Executor executor,
                                   @NotNull ProgramRunner runner) throws ExecutionException {
        ListProcessHandler processHandler = startProcess();
        if (processHandler.getProcessHandlers().isEmpty()) {
            UploadProcessHandler nopHandler = createNopProcessHandler();
            return new DefaultExecutionResult(nopHandler.getConsole(), processHandler);
        }

        processHandler.getProcessHandlers().forEach(h -> {
            ExecutionResult er = new DefaultExecutionResult(h.getConsole(), h, new AnAction[0]);
            h.setExecutionResult(er);
        });
//        setFirstRunTabTitle(processHandler);

        return new ListExecutionResult(processHandler, environment);
    }


    @NotNull
    @Override
    protected ListProcessHandler startProcess() throws ExecutionException {
        String configurationName = this.environment.getRunnerAndConfigurationSettings().getName();
        ListProcessHandler listProcessHandler = new ListProcessHandler(new ArrayList<>());
        Iterator<String> iterator = deployProfiles.listIterator();
        while (iterator.hasNext()) {
            String profile = iterator.next();
            if (StringUtil.isEmpty(profile)) {
                iterator.remove();
                continue;
            }
            // resolve profile
            DeployProfile deployProfile = new DeployProfile(profile);
            if (!deployProfile.isActive()) {
                continue;
            }
            UploadProcessHandler processHandler = startProcess(deployProfile, configurationName);
            listProcessHandler.getProcessHandlers().add(processHandler);
        }
        return listProcessHandler;
    }

    private UploadProcessHandler startProcess(DeployProfile deployProfile,
                                              String configurationName) throws ExecutionException {
        // create console
        ConsoleView console = createConsole(executor);
        if (console == null) {
            throw new ExecutionException("create console view failed");
        }

        // create process handler
        SshUploadTask uploadTask = new SshUploadTask(console, deployProfile);
        UploadProcessHandler processHandler = new UploadProcessHandler();
        processHandler.setName(configurationName + " [" +uploadTask.getTaskName() + "]");

        // execute
        Task.Backgroundable task = new BackgroundUploadTask(uploadTask, console, processHandler);
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, new ProgressIndicatorBase());

        // attach
        console.attachToProcess(processHandler);
        processHandler.setConsole(console);
        return processHandler;
    }

    private UploadProcessHandler createNopProcessHandler() throws ExecutionException {
        ConsoleView console = this.createConsole(executor);
        if (console == null) {
            throw new ExecutionException("create console view failed");
        }
        UploadProcessHandler processHandler = new UploadProcessHandler();
        console.attachToProcess(processHandler);
        processHandler.setConsole(console);

        processHandler.notifyProcessTerminated(0);
        console.print("[INFO] ", ConsoleViewContentType.LOG_INFO_OUTPUT);
        console.print("Finished at: " + LocalDateTime.now(), ConsoleViewContentType.NORMAL_OUTPUT);

        return processHandler;
    }

    private void setFirstRunTabTitle(ListProcessHandler processHandler) {
        ToolWindowManager windowManager = ToolWindowManager.getInstance(this.environment.getProject());
        ToolWindow runWindow = windowManager.getToolWindow("Run");
        if (runWindow != null) {
            UploadProcessHandler first = processHandler.getProcessHandlers().get(0);
            ContentManager contentManager = runWindow.getContentManager();
            if (this.environment.getRunnerAndConfigurationSettings() != null) {
                String name = this.environment.getRunnerAndConfigurationSettings().getName();
                contentManager.addContentManagerListener(new RunTabTitleListener(first, name, contentManager));
            }
        }
    }

    private class BackgroundUploadTask extends Task.Backgroundable {

        private final SshUploadTask uploadTask;
        private final ConsoleView console;
        private final UploadProcessHandler processHandler;

        public BackgroundUploadTask(SshUploadTask uploadTask,
                                    ConsoleView console,
                                    UploadProcessHandler processHandler) {
            super(project, "Upload to Host", false);
            this.uploadTask = uploadTask;
            this.console = console;
            this.processHandler = processHandler;
        }

        @Override
        public void run(@NotNull ProgressIndicator indicator) {
            try {
                uploadTask.run();

                console.print("[INFO] ", ConsoleViewContentType.LOG_INFO_OUTPUT);
                console.print("Finished at: " + LocalDateTime.now(), ConsoleViewContentType.NORMAL_OUTPUT);
            } catch (Exception e) {
                console.print(e.getMessage() + "\n", ConsoleViewContentType.ERROR_OUTPUT);
            } finally {
                processHandler.notifyProcessTerminated(0);
            }
        }
    }

    /**
     * listen for the configuration name and replace it with the name of the process handler
     */
    private static class RunTabTitleListener extends ContentManagerAdapter {
        private final UploadProcessHandler processHandler;
        private final String configurationName;
        private final ContentManager contentManager;

        public RunTabTitleListener(UploadProcessHandler processHandler,
                                   String configurationName,
                                   ContentManager contentManager) {
            this.processHandler = processHandler;
            this.configurationName = configurationName;
            this.contentManager = contentManager;
        }

        @Override
        public void selectionChanged(@NotNull ContentManagerEvent event) {
            setTabTitle(event);
        }

        @Override
        public void contentAdded(@NotNull ContentManagerEvent event) {
            setTabTitle(event);
        }

        private void setTabTitle(ContentManagerEvent event) {
            if (event.getContent().getDisplayName().equals(configurationName)) {
                event.getContent().setDisplayName(processHandler.getName());
                contentManager.removeContentManagerListener(this);
            }
        }
    }
}