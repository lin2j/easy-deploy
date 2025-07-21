package tech.lin2j.idea.plugin.ui.component

import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.ClearableLazyValue
import com.intellij.openapi.util.Disposer
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.bindIntText
import com.intellij.ui.dsl.builder.panel
import tech.lin2j.idea.plugin.ssh.SshServer
import tech.lin2j.idea.plugin.uitl.MessagesBundle

/**
 * @author lin2j
 * @date 2025-07-21 21:48
 */
internal class HostOtherPanel() {
    private var disposable: Disposable? = null
    private val panel = object : ClearableLazyValue<DialogPanel>() {
        override fun compute(): DialogPanel {
            if (disposable == null) {
                disposable = Disposer.newDisposable()
            }
            val panel = createPanel()
            panel.registerValidators(disposable!!)
            return panel
        }
    }

    private var contentProvider: SshServer? = null;

    constructor(server: SshServer?) : this() {
        contentProvider = server?.clone() ?: SshServer()
    }

    fun createPanel(): DialogPanel {
        return panel {
            row(MessagesBundle.getText("dialog.panel.host.other.exit-code")) {
                intTextField()
                    .bindIntText(
                        getter = { contentProvider?.successCommandExitCode ?: 0 },
                        setter = { contentProvider?.successCommandExitCode = it })
                    .comment(MessagesBundle.getText("dialog.panel.host.other.exit-code.comment"))
            }.topGap(TopGap.SMALL)
        }
    }

    fun createUI(): DialogPanel {
        return panel.value
    }

    fun setOtherSettings(server: SshServer) {
        panel.value.apply()
        server.successCommandExitCode = contentProvider?.successCommandExitCode ?: 0
    }

}