package pl.beone.javacodeexecutor.intellij.plugin.internal.component.task

import com.intellij.execution.CantRunException
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project

class SettingsEditorValidator(private val project: Project,
                              private val host: String,
                              private val port: String,
                              private val username: String,
                              private val password: String) {

    fun validate() {
        validateCurrentOpenFile()
        validateHost()
        validatePort()
        validateUsername()
        validatePassword()
    }

    private fun validateCurrentOpenFile() {
        if (FileEditorManager.getInstance(project).selectedTextEditor == null) {
            throw CantRunException("No file is open")
        }
    }

    private fun validateHost() {
        if (host.isBlank()) {
            throw CantRunException("Host must be specified")
        }
    }

    private fun validatePort() {
        if (port.isBlank()) {
            throw CantRunException("Port must be specified")
        }
        port.toIntOrNull() ?: throw CantRunException("Port <$port> must be a number")
    }

    private fun validateUsername() {
        if (username.isBlank()) {
            throw CantRunException("Username must be specified")
        }
    }

    private fun validatePassword() {
        if (password.isBlank()) {
            throw CantRunException("Password must be specified")
        }
    }
}