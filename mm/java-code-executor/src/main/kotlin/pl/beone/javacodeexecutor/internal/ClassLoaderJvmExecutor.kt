package pl.beone.javacodeexecutor.internal

import org.joor.CompileOptions
import org.joor.CustomCompile
import org.joor.Reflect.on
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import pl.beone.javacodeexecutor.applicationmodel.ExecutionResult
import pl.beone.javacodeexecutor.contract.JvmExecutor
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.security.Permission

class ClassLoaderJvmExecutor(private val autowireCapableBeanFactory: AutowireCapableBeanFactory) : JvmExecutor {

    companion object {
        private val packageRegex = "package (.*);\$".toRegex()
        private val classNameRegex = "(public)?[ ]*(final)?[ ]*class[ ]*([a-zA-Z0-9]+)[ ]*[{]?".toRegex()
    }


    override fun execute(code: ByteArray): ExecutionResult {
        return grantPermissionsAndRun {
            val classInstance = on(CustomCompile.compileWithoutLoadingToClassLoader(getFullClassName(code),
                    String(code),
                    CompileOptions()))
                    .create()
                    .get<Any>()

            autowireCapableBeanFactory.autowireBean(classInstance)

            val messages = runExecuteFunctionAndCollectMessagesFromSystemOut(classInstance)

            ExecutionResult(messages)
        }
    }

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

    private fun getFullClassName(code: ByteArray): String = getPackage(code) + "." + getClassName(code)

    private fun getPackage(code: ByteArray): String =
            String(code).lineSequence()
                    .firstOrNull { packageRegex.find(it) != null }
                    ?.let { packageRegex.find(it)!!.groupValues[1] }
                    ?: throw Exception("No package")

    private fun getClassName(code: ByteArray): String =
            String(code).lineSequence()
                    .firstOrNull { classNameRegex.find(it) != null }
                    ?.let { classNameRegex.find(it)!!.groupValues[3] }
                    ?: throw Exception("No class name")

    private fun runExecuteFunctionAndCollectMessagesFromSystemOut(classInstance: Any): List<String> {
        val originalStdout = System.out

        val byteArray = ByteArrayOutputStream()
        PrintStream(byteArray, true, "UTF-8").use {
            System.setOut(it)

            on(classInstance).call("execute")
        }

        System.setOut(originalStdout)

        return byteArray.toString(Charsets.UTF_8.name())
                .split("\n")
                .dropLast(1)
    }
}