package pl.beone.javacodeexecutor.intellij.plugin.internal.component.configuration

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import pl.beone.javacodeexecutor.intellij.plugin.internal.component.icon.Icons
import javax.swing.Icon

class JavaCodeExecutorRunConfigurationType : ConfigurationType {

    override fun getDisplayName(): String =
            "Java code executor"

    override fun getConfigurationTypeDescription(): String =
            "Executing code on running ACS (you have to install java-code-executor module)"

    override fun getIcon(): Icon =
            Icons.Alfresco

    override fun getId(): String =
            "JAVA_CODE_EXECUTOR_RUN_CONFIGURATION"

    override fun getConfigurationFactories(): Array<ConfigurationFactory> =
            arrayOf(JavaCodeExecutorConfigurationFactory(this))
}
