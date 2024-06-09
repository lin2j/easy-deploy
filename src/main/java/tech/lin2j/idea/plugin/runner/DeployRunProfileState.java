package tech.lin2j.idea.plugin.runner;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.process.NopProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.domain.model.DeployProfile;
import tech.lin2j.idea.plugin.ssh.SshUploadTask;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

/**
 * @author linjinjia
 * @date 2024/5/2 16:17
 */
public class DeployRunProfileState extends CommandLineState {

    private final List<String> deployProfiles;
    private final Executor executor;
    private ConsoleView console;

    protected DeployRunProfileState(Executor executor,
                                    ExecutionEnvironment environment,
                                    List<String> deployProfiles) {
        super(environment);
        this.deployProfiles = deployProfiles;
        this.executor = executor;
    }

    @NotNull
    @Override
    public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
        final ProcessHandler processHandler = startProcess();
        if (console != null) {
            console.attachToProcess(processHandler);
        }
        return new DefaultExecutionResult(console, processHandler, createActions(console, processHandler, executor));
    }

    @NotNull
    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        NopProcessHandler process = new NopProcessHandler();
        Project project = getEnvironment().getProject();
        console = createConsole(executor);
        Task.Backgroundable task = new Task.Backgroundable(project, "Upload to Host", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                Iterator<String> iterator = deployProfiles.listIterator();
                while (iterator.hasNext()) {
                    String profile = iterator.next();
                    if (StringUtil.isEmpty(profile)) {
                        iterator.remove();
                        continue;
                    }
                    try {
                        DeployProfile deployProfile = new DeployProfile(profile);
                        if (deployProfile.isActive()) {
                            int sshId = deployProfile.getSshId();
                            int profileId = deployProfile.getProfileId();
                            new SshUploadTask(console, sshId, profileId).run();
                        }
                    } catch (Exception e) {
                        console.print(e.getMessage() + "\n", ConsoleViewContentType.ERROR_OUTPUT);
                    }
                }

                console.print("[INFO] ", ConsoleViewContentType.LOG_INFO_OUTPUT);
                console.print("Finished at: " + LocalDateTime.now(), ConsoleViewContentType.NORMAL_OUTPUT);

                process.destroyProcess();
            }
        };
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, new ProgressIndicatorBase());
        return process;
    }
}