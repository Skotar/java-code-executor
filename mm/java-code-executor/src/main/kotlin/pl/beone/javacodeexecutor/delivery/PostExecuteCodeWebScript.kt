package pl.beone.javacodeexecutor.delivery

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.extensions.webscripts.*
import pl.beone.javacodeexecutor.applicationmodel.ExecutionResult
import pl.beone.javacodeexecutor.contract.JvmExecutor
import java.io.PrintWriter
import java.io.StringWriter
import java.net.HttpURLConnection

class PostExecuteCodeWebScript(private val jvmExecutor: JvmExecutor) : AbstractWebScript() {

    companion object {
        private val logger = LoggerFactory.getLogger(PostExecuteCodeWebScript::class.java)

        private val mapper = ObjectMapper()
    }

    override fun execute(request: WebScriptRequest, response: WebScriptResponse) {
        val code = request.getCode()

        try {
            response.apply(jvmExecutor.execute(code))
        } catch (e: Exception) {
            logger.error("Error occurred during executing sent code", e)

            response.apply(ExecutionResult(e.convertStackTraceToMessages()), Status.STATUS_INTERNAL_SERVER_ERROR)
        }
    }

    private fun WebScriptRequest.getCode(): ByteArray {
        if (this.content.inputStream.available() == 0) {
            throw WebScriptException(HttpURLConnection.HTTP_BAD_REQUEST, "You have to use <binary> request type")
        }

        return this.content.inputStream.readBytes()
    }

    private fun WebScriptResponse.apply(executionResult: ExecutionResult, status: Int = Status.STATUS_OK) {
        this.writer.use {
            it.write(mapper.writeValueAsString(executionResult))
        }
        this.setStatus(status)
    }

    private fun Exception.convertStackTraceToMessages(): List<String> {
        val sw = StringWriter()
        this.printStackTrace(PrintWriter(sw))

        return sw.toString().split("\n")
                .dropLast(1)
    }
}