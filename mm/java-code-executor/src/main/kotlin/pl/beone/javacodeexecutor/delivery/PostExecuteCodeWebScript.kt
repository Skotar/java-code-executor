package pl.beone.javacodeexecutor.delivery

import org.slf4j.LoggerFactory
import org.springframework.extensions.webscripts.*
import pl.beone.javacodeexecutor.contract.JvmExecutor
import java.io.PrintWriter
import java.io.StringWriter
import java.net.HttpURLConnection

class PostExecuteCodeWebScript(private val jvmExecutor: JvmExecutor) : DeclarativeWebScript() {

    companion object {
        private val logger = LoggerFactory.getLogger(PostExecuteCodeWebScript::class.java)
    }

    override fun executeImpl(req: WebScriptRequest, status: Status, cache: Cache): MutableMap<String, Any> {
        val code = req.getCode()

        val messages = try {
            jvmExecutor.execute(code).messages
        } catch (e: Exception) {
            logger.error("Error occurred during executing sent code", e)

            status.code = Status.STATUS_INTERNAL_SERVER_ERROR
            convertStackTraceToMessages(e)
        }

        return mutableMapOf("messages" to messages)
    }

    private fun WebScriptRequest.getCode(): ByteArray {
        if (this.content.inputStream.available() == 0) {
            throw WebScriptException(HttpURLConnection.HTTP_BAD_REQUEST, "You have to use <binary> request type")
        }

        return this.content.inputStream.readBytes()
    }

    private fun convertStackTraceToMessages(e: Exception): List<String> {
        val sw = StringWriter()
        e.printStackTrace(PrintWriter(sw))

        return sw.toString().split("\n")
                .dropLast(1)
    }
}