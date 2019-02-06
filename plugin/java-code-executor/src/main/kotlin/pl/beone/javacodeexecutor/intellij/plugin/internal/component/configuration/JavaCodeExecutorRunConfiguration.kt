package pl.beone.javacodeexecutor.intellij.plugin.internal.component.configuration

import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializer
import org.jdom.Element
import pl.beone.javacodeexecutor.intellij.plugin.internal.component.commons.getFileName
import pl.beone.javacodeexecutor.intellij.plugin.internal.component.task.JavaCodeExecutorTaskBackgroundable
import pl.beone.javacodeexecutor.intellij.plugin.internal.component.task.SettingsEditorValidator


class JavaCodeExecutorRunConfiguration(project: Project,
                                       factory: JavaCodeExecutorConfigurationFactory,
                                       name: String)
    : RunConfigurationBase<JavaCodeExecutorSettingsEditorForm>(project, factory, name) {

    internal var model = Model()

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
            JavaCodeExecutorSettingsEditor()

    override fun getState(executor: Executor, executionEnvironment: ExecutionEnvironment): RunProfileState =
            RunProfileState { _, _ ->
                SettingsEditorValidator(project, model.host, model.port, model.username, model.password)
                        .validate()

                JavaCodeExecutorTaskBackgroundable(project, model.host, model.path, model.port, model.username, model.password)
                        .run(project.getFileName(), getCode())

                null
            }

    private fun getCode(): String =
            FileEditorManager.getInstance(project)
                    .selectedTextEditor!!
                    .document
                    .text

    override fun readExternal(element: Element) {
        XmlSerializer.deserializeInto(this, element)
    }

    override fun writeExternal(element: Element) {
        XmlSerializer.serializeInto(this, element)
    }
}
