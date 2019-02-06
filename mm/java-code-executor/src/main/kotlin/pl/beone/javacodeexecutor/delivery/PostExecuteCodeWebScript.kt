package pl.beone.javacodeexecutor.delivery

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.LoggerFactory
import org.springframework.extensions.webscripts.AbstractWebScript
import org.springframework.extensions.webscripts.Status.*
import org.springframework.extensions.webscripts.WebScriptRequest
import org.springframework.extensions.webscripts.WebScriptResponse
import pl.beone.javacodeexecutor.applicationmodel.ExecutionResult
import pl.beone.javacodeexecutor.applicationmodel.exception.CompilerException
import pl.beone.javacodeexecutor.applicationmodel.exception.ExecutionException
import pl.beone.javacodeexecutor.contract.JvmExecutor

internal data class WebScriptException(val message: String?,
                                       val stackTrace: List<String>?)

class PostExecuteCodeWebScript(private val jvmExecutor: JvmExecutor) : AbstractWebScript() {

    companion object {
        private val logger = LoggerFactory.getLogger(PostExecuteCodeWebScript::class.java)

        private val mapper = ObjectMapper().apply {
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }

    override fun execute(request: WebScriptRequest, response: WebScriptResponse) {
        val code = request.getCode()

        try {
            response.applySuccess(jvmExecutor.execute(code))
        } catch (e: Exception) {
            logger.error("Error occurred during executing", e)

            when (e) {
                is IllegalStateException -> response.applyFailure(e, STATUS_BAD_REQUEST)
                else                     -> response.applyFailure(e, STATUS_INTERNAL_SERVER_ERROR)
            }
        }
    }

    private fun WebScriptRequest.getCode(): ByteArray {
        if (this.content.inputStream.available() == 0) {
            throw IllegalStateException("You have to use <binary> request type")
        }

        return this.content.inputStream.readBytes()
    }

    private fun WebScriptResponse.applySuccess(executionResult: ExecutionResult) {
        this.writer.use { it.write(mapper.writeValueAsString(executionResult)) }
        this.setStatus(STATUS_OK)
    }

    private fun WebScriptResponse.applyFailure(e: Exception, status: Int) {
        val convertedException = when (e) {
            is CompilerException  -> WebScriptException(e.message, null)
            is ExecutionException -> WebScriptException(null, e.stackTrace)
            else                  -> WebScriptException(e.message, ExceptionUtils.getStackFrames(e).toList())
        }

        this.writer.use { it.write(mapper.writeValueAsString(convertedException)) }
        this.setStatus(status)
    }
}