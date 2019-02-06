package pl.beone.javacodeexecutor.intellij.plugin.internal.component.configuration

import com.intellij.openapi.options.SettingsEditor
import javax.swing.JComponent

class JavaCodeExecutorSettingsEditor : SettingsEditor<JavaCodeExecutorRunConfiguration>() {

    private val form = JavaCodeExecutorSettingsEditorForm()

    override fun resetEditorFrom(configuration: JavaCodeExecutorRunConfiguration) {
        form.hostTextField.text = configuration.model.host
        form.pathTextField.text = configuration.model.path
        form.portTextField.text = configuration.model.port
        form.userNameTextField.text = configuration.model.username
        form.passwordField.text = configuration.model.password
    }

    override fun applyEditorTo(configuration: JavaCodeExecutorRunConfiguration) {
        configuration.model.host = form.hostTextField.text
        configuration.model.path = form.pathTextField.text
        configuration.model.port = form.portTextField.text
        configuration.model.username = form.userNameTextField.text
        configuration.model.password = String(form.passwordField.password)
    }

    override fun createEditor(): JComponent = form.panel
}