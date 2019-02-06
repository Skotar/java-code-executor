package pl.beone.javacodeexecutor.intellij.plugin.external.http

import com.google.common.base.Stopwatch
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.http.HttpResponse
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClientBuilder
import java.io.IOException
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_OK
import java.net.URI
import java.nio.charset.Charset
import java.time.Duration

data class RunnerResult(val messages: List<String>,
                        val success: Boolean,
                        val duration: Duration)

class JavaCodeExecutorRunner(private val host: String,
                             private val path: String,
                             private val port: String,
                             private val username: String,
                             private val password: String) {


    private val httpClient = HttpClientBuilder.create()
            .setDefaultCredentialsProvider(createProviderWithCredentials())
            .build()

    private val httpPost = HttpPost(createUrl())

    fun execute(code: String): RunnerResult =
            try {
                httpPost.entity = StringEntity(code)

                val (response, duration) = measureTime {
                    httpClient.execute(httpPost)
                }

                RunnerResult(response.convertMessages(httpPost.uri),
                             response.statusLine.statusCode == HTTP_OK,
                             duration)
            } catch (e: Exception) {
                RunnerResult(ExceptionUtils.getStackFrames(e).toList(),
                             false,
                             Duration.ZERO)
            }

    fun cancel() {
        httpPost.abort()
    }

    private fun createProviderWithCredentials(): BasicCredentialsProvider =
            BasicCredentialsProvider().apply {
                val credentials = UsernamePasswordCredentials(username, password)
                setCredentials(AuthScope.ANY, credentials)
            }

    private fun createUrl(): String =
            "http://$host:$port" + "$path/service/api/javaCodeExecutor/execute".replace("//", "/")

    private fun <T> measureTime(toExecute: () -> T): Pair<T, Duration> {
        val stopwatch = Stopwatch.createStarted()
        val result = toExecute()
        val duration = stopwatch.elapsed()

        return result to duration
    }

    private fun HttpResponse.convertMessages(uri: URI): List<String> {
        val entityJsonObject = parseAsJsonObject()

        return when (this.statusLine.statusCode) {
            HTTP_OK             ->
                entityJsonObject!!.getMessages()
            HTTP_INTERNAL_ERROR ->
                listOf(this.statusLine.toString(), uri.toString(), "") + entityJsonObject!!.getMessage() + entityJsonObject.getStackTrace()
            else                ->
                listOf(this.statusLine.toString(), uri.toString(), "") + (entityJsonObject?.getMessage() ?: emptyList())
        }
    }

    private fun HttpResponse.parseAsJsonObject(): JsonObject? =
            try {
                JsonParser().parse(IOUtils.toString(this.entity.content, Charset.forName("UTF-8"))).asJsonObject
            } catch (e: Exception) {
                null
            }

    private fun JsonObject.getMessages(): List<String> =
            this.asJsonObject.getAsJsonArray("messages")
                    .map { it.asString }

    private fun JsonObject.getMessage(): List<String> =
            listOfNotNull(this.get("message")?.asString)

    private fun JsonObject.getStackTrace(): List<String> =
            this.get("stackTrace")?.asJsonArray?.map { it.asString } ?: emptyList()
}