package pl.beone.javacodeexecutor.internal

import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.log4j.Level
import org.apache.log4j.PatternLayout
import org.apache.log4j.WriterAppender
import org.joor.CompileOptions
import org.joor.CustomCompile
import org.joor.Reflect.on
import org.slf4j.Logger
import org.slf4j.impl.Log4jLoggerAdapterFactory
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import pl.beone.javacodeexecutor.applicationmodel.ExecutionResult
import pl.beone.javacodeexecutor.applicationmodel.exception.CompilerException
import pl.beone.javacodeexecutor.applicationmodel.exception.ExecutionException
import pl.beone.javacodeexecutor.contract.JvmExecutor
import java.io.StringWriter
import java.security.Permission


class ClassLoaderJvmExecutor(private val autowireCapableBeanFactory: AutowireCapableBeanFactory) : JvmExecutor {

    companion object {
        private val packageRegex = "package (.*);\$".toRegex()
        private val classNameRegex = "(public)?[ ]*(final)?[ ]*class[ ]*([a-zA-Z0-9]+)[ ]*[{]?".toRegex()
    }

    override fun execute(code: ByteArray): ExecutionResult {
        return grantPermissionsAndRun {
            val qualifiedClass = getQualifiedClass(code)

            val classInstance = compile(qualifiedClass, code)

            checkIfExecuteMethodExists(classInstance)
            autowireCapableBeanFactory.autowireBean(classInstance)
            val messages = runExecuteFunctionAndCollectMessagesFromLogger(qualifiedClass, classInstance)

            ExecutionResult(messages)
        }
    }

    // potential security issues
    private fun <T> grantPermissionsAndRun(block: () -> T): T {
        val defaultSecurityManager = System.getSecurityManager()
        System.setSecurityManager(object : SecurityManager() {
            override fun checkPermission(perm: Permission?) {
                return
            }
        })

        try {
            return block()
        } finally {
            System.setSecurityManager(defaultSecurityManager)
        }
    }

    private fun getQualifiedClass(code: ByteArray): String = getPackage(code) + "." + getClassName(code)

    private fun getPackage(code: ByteArray): String =
            String(code).lineSequence()
                    .firstOrNull { packageRegex.find(it) != null }
                    ?.let { packageRegex.find(it)!!.groupValues[1] }
                    ?: throw IllegalStateException("The file must have a package name")

    private fun getClassName(code: ByteArray): String =
            String(code).lineSequence()
                    .firstOrNull { classNameRegex.find(it) != null }
                    ?.let { classNameRegex.find(it)!!.groupValues[3] }
                    ?: throw IllegalStateException("The file must contain a class")

    private fun compile(qualifiedClass: String, code: ByteArray): Any =
            try {
                on(CustomCompile.compileAndLoadToCustomClassLoader(qualifiedClass, String(code), CompileOptions()))
                        .create()
                        .get()
            } catch (e: Exception) {
                throw CompilerException(e.message!!)
            }

    private fun checkIfExecuteMethodExists(classInstance: Any) {
        try {
            classInstance::class.java.getMethod("execute")
        } catch (e: Exception) {
            throw IllegalStateException("The class must contain the following method: public void execute()")
        }
    }

    private fun runExecuteFunctionAndCollectMessagesFromLogger(qualifiedName: String, classInstance: Any): List<String> {
        val (logger, stringWriter) = createLoggerWithRedirectedOutput(qualifiedName)
        injectLoggerIfConditionsAreMet(classInstance, logger)

        try {
            on(classInstance).call("execute")
        } catch (e: Exception) {
            throw createExecutionExceptionWithLimitedStackTrace(e)
        }

        return stringWriter.toString()
                .split("\n")
                .dropLast(1)
                .map { it.trim() }
    }

    private fun createLoggerWithRedirectedOutput(qualifiedName: String): Pair<Logger, StringWriter> {
        val logger = org.apache.log4j.Logger.getLogger("$qualifiedName-${getUniqueId()}")
        logger.level = Level.ALL

        val stringWriter = StringWriter()
        val writerAppender = WriterAppender(PatternLayout("%d{ISO8601} %x %-5p [%c{3}] [%t] %m%n "), stringWriter)
        logger.addAppender(writerAppender)

        return Pair(Log4jLoggerAdapterFactory.getLogger(logger), stringWriter)
    }

    private fun getUniqueId(): Long =
            Thread.currentThread().id

    private fun injectLoggerIfConditionsAreMet(classInstance: Any, logger: Logger) {
        val loggerFields = classInstance::class.java.declaredFields
                .filter { it.type == Logger::class.java }

        for (field in loggerFields) {
            val isAccessible = field.isAccessible
            field.isAccessible = true

            if (field.get(classInstance) == null) {
                field.set(classInstance, logger)
            }

            field.isAccessible = isAccessible
        }
    }

    private fun createExecutionExceptionWithLimitedStackTrace(e: Exception): ExecutionException {
        val stackFrames = ExceptionUtils.getStackFrames(e).toList()
        val stackTraces = stackFrames.subList(stackFrames.indexOfLast { it.startsWith("Caused by:") }, stackFrames.lastIndex)
        return ExecutionException(stackTraces)
    }
}