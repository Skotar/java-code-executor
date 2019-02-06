package pl.beone.javacodeexecutor.intellij.plugin.internal.component.toolwindow

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.Content
import pl.beone.javacodeexecutor.intellij.plugin.internal.component.commons.getFileName
import pl.beone.javacodeexecutor.intellij.plugin.internal.component.icon.Icons
import java.time.Duration

class RunToolWindowTab(private val project: Project) {

    fun createAndPrintMessages(messages: List<String>, success: Boolean, duration: Duration) {
        val toolWindow = getToolWindow()
        val consoleView = createConsoleView()

        val content = consoleView.createContent(toolWindow, project.getFileName())

        setIcon(content)
        show(toolWindow, content)

        consoleView.printMessages(success, messages, duration)
    }

    private fun ConsoleView.printMessages(success: Boolean, messages: List<String>,
                                          duration: Duration) {
        val contentType = if (success) {
            ConsoleViewContentType.NORMAL_OUTPUT
        } else {
            ConsoleViewContentType.ERROR_OUTPUT
        }

        messages.forEach { this.print(it + "\n", contentType) }

        if (success) {
            this.print("\n", ConsoleViewContentType.NORMAL_OUTPUT)
            this.print("Time: ${duration.prettyPrint()}", ConsoleViewContentType.SYSTEM_OUTPUT)
        }
    }

    private fun getToolWindow(): ToolWindow =
            ToolWindowManager.getInstance(project)
                    .getToolWindow(ToolWindowId.RUN)

    private fun createConsoleView(): ConsoleView =
            TextConsoleBuilderFactory.getInstance()
                    .createBuilder(project)
                    .console

    private fun ConsoleView.createContent(toolWindow: ToolWindow, fileName: String): Content =
            toolWindow.contentManager.factory.createContent(this.component, fileName, true)

    private fun setIcon(content: Content) {
        content.icon = Icons.Alfresco
        content.putUserData(ToolWindow.SHOW_CONTENT_ICON, true)
    }

    private fun show(toolWindow: ToolWindow, content: Content) {
        toolWindow.contentManager.addContent(content)

        toolWindow.activate {
            toolWindow.contentManager.setSelectedContent(content, true)
        }
    }

    private fun Duration.prettyPrint(): String =
            this.toString()
                    .substring(2)
                    .replace("(\\d[HMS])(?!$)", "$1 ")
                    .toLowerCase()
}