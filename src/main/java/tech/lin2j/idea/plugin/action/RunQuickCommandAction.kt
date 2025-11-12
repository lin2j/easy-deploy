package tech.lin2j.idea.plugin.action

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import tech.lin2j.idea.plugin.model.Command
import java.util.function.Supplier

class RunQuickCommandAction(val provider: Supplier<Command?>) :
    NewUpdateThreadAction("Run", "Run command", AllIcons.Actions.RunAll) {

    override fun actionPerformed(e: AnActionEvent) {
        this.provider.get()?.run {
            // 执行命令
        }

    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

}