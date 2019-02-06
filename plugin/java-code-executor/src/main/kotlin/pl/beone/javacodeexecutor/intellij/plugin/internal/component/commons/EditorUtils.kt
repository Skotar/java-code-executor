package pl.beone.javacodeexecutor.intellij.plugin.internal.component.commons

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project

internal fun Project.getFileName(): String {
    val document = FileEditorManager.getInstance(this).selectedTextEditor!!.document
    val file = FileDocumentManager.getInstance().getFile(document)
    return file!!.name
}