package pl.beone.javacodeexecutor.contract

import pl.beone.javacodeexecutor.applicationmodel.ExecutionResult

interface JvmExecutor {

    fun execute(code: ByteArray): ExecutionResult
}