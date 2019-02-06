package pl.beone.javacodeexecutor.intellij.plugin.internal.component.configuration

import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import pl.beone.javacodeexecutor.intellij.plugin.internal.component.commons.getFileName
import pl.beone.javacodeexecutor.intellij.plugin.internal.component.task.JavaCodeExecutorTaskBackgroundable
import pl.beone.javacodeexecutor.intellij.plugin.internal.component.task.SettingsEditorValidator


class JavaCodeExecutorRunConfiguration(project: Project,
                                       factory: JavaCodeExecutorConfigurationFactory,
                                       name: String)
    : RunConfigurationBase<JavaCodeExecutorSettingsEditorForm>(project, factory, name) {

    internal var host: String = "localhost"
    internal var path: String = "/alfresco"
    internal var port: String = "8080"
    internal var username: String = "admin"
    internal var password: String = "admin"

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
            JavaCodeExecutorSettingsEditor()

    override fun getState(executor: Executor, executionEnvironment: ExecutionEnvironment): RunProfileState =
            RunProfileState { _, _ ->
                SettingsEditorValidator(project, host, port, username, password)
                        .validate()

                JavaCodeExecutorTaskBackgroundable(project, host, path, port, username, password)
                        .run(project.getFileName(), getCode())

                null
            }

    private fun getCode(): String =
            FileEditorManager.getInstance(project)
                    .selectedTextEditor!!
                    .document
                    .text
}
