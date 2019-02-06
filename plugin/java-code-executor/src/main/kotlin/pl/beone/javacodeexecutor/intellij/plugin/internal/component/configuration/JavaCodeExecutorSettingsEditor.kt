package pl.beone.javacodeexecutor.intellij.plugin.internal.component.configuration

import com.intellij.openapi.options.SettingsEditor
import javax.swing.JComponent

class JavaCodeExecutorSettingsEditor : SettingsEditor<JavaCodeExecutorRunConfiguration>() {

    private val form = JavaCodeExecutorSettingsEditorForm()

    override fun resetEditorFrom(configuration: JavaCodeExecutorRunConfiguration) {
        form.hostTextField.text = configuration.host
        form.pathTextField.text = configuration.path
        form.portTextField.text = configuration.port
        form.userNameTextField.text = configuration.username
        form.passwordField.text = configuration.password
    }

    override fun applyEditorTo(configuration: JavaCodeExecutorRunConfiguration) {
        configuration.host = form.hostTextField.text
        configuration.path = form.pathTextField.text
        configuration.port = form.portTextField.text
        configuration.username = form.userNameTextField.text
        configuration.password = String(form.passwordField.password)
    }

    override fun createEditor(): JComponent = form.panel
}