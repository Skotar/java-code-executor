package pl.beone.javacodeexecutor.intellij.plugin.internal.component.task

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import pl.beone.javacodeexecutor.intellij.plugin.external.http.JavaCodeExecutorRunner
import pl.beone.javacodeexecutor.intellij.plugin.external.http.RunnerResult
import pl.beone.javacodeexecutor.intellij.plugin.internal.component.toolwindow.RunToolWindowTab

class JavaCodeExecutorTaskBackgroundable(private val project: Project,
                                         private val host: String,
                                         private val path: String,
                                         private val port: String,
                                         private val username: String,
                                         private val password: String) {

    fun run(fileName: String, code: String) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Java code executor") {

            private val javaCodeExecutorRunner = JavaCodeExecutorRunner(host, path, port, username, password)
            private lateinit var runnerResult: RunnerResult

            override fun run(progressIndicator: ProgressIndicator) {
                progressIndicator.text = "Executing $fileName..."

                runnerResult = javaCodeExecutorRunner
                        .execute(code)
            }

            override fun onSuccess() {
                RunToolWindowTab(project)
                        .createAndPrintMessages(runnerResult.messages, runnerResult.success, runnerResult.duration)
            }

            override fun onCancel() {
                javaCodeExecutorRunner.cancel()
            }
        })
    }
}