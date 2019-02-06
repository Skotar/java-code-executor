package pl.beone.javacodeexecutor.intellij.plugin.internal.component.configuration

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project

class JavaCodeExecutorConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {

    override fun createTemplateConfiguration(project: Project): RunConfiguration =
            JavaCodeExecutorRunConfiguration(project,
                                             this,
                                             "Java code executor")

}
