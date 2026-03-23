package ru.kyamshanov.missionChat.data.network

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import ru.kyamshanov.missionChat.config.BuildKonfig
import ru.kyamshanov.missionChat.data.models.DeepSeekRequest
import ru.kyamshanov.missionChat.data.models.DeepSeekResponse
import ru.kyamshanov.missionChat.data.models.DeepSeekStreamResponse
import ru.kyamshanov.missionChat.data.models.Message

internal class DeepseekApiImpl(
    private val client: HttpClient,
    private val json: Json,
    private val apiKey: String = BuildKonfig.DEEPSEEEK_API_KEY,
) : DeepseekApi {

    override fun chatCompletionStream(
        userMessage: String,
        chatHistory: List<Message>,
        model: String,
        systemPrompt: String,
        temperature: Double,
        maxTokens: Int
    ): Flow<String> = flow {
        val context = currentCoroutineContext()
        val requestBody = DeepSeekRequest(
            messages = buildList {
                add(Message(DeepseekConstants.ROLE_SYSTEM, systemPrompt))
                addAll(chatHistory)
                add(Message(DeepseekConstants.ROLE_USER, userMessage))
            },
            stream = true,
            temperature = temperature,
            max_tokens = maxTokens
        )

        client.preparePost(DeepseekConstants.ENDPOINT_URL) {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.execute { response ->
            val channel: ByteReadChannel = response.bodyAsChannel()

            while (!channel.isClosedForRead) {
                val line = channel.readLine() ?: break
                if (line.isBlank()) continue
                if (line.startsWith(DeepseekConstants.STREAM_DATA_PREFIX)) {
                    val data = line.removePrefix(DeepseekConstants.STREAM_DATA_PREFIX).trim()
                    if (data == DeepseekConstants.STREAM_DONE_MARKER) break
                    val chunk = json.decodeFromString<DeepSeekStreamResponse>(data)
                    val content = chunk.choices.firstOrNull()?.delta?.content
                    if (content != null) {
                        withContext(context) {
                            emit(content)
                        }
                    }
                }
            }
        }
    }

    override suspend fun chatCompletion(
        userMessage: String,
        chatHistory: List<Message>,
        model: String,
        systemPrompt: String,
        temperature: Double,
        maxTokens: Int
    ): String {
        val requestBody = DeepSeekRequest(
            messages = buildList {
                add(Message(DeepseekConstants.ROLE_SYSTEM, systemPrompt))
                addAll(chatHistory)
                add(Message(DeepseekConstants.ROLE_USER, userMessage))
            },
            stream = false,
            temperature = temperature,
            max_tokens = maxTokens
        )

        val response = client.post(DeepseekConstants.ENDPOINT_URL) {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        val responseBody = response.bodyAsText()

        val result = json.decodeFromString<DeepSeekResponse>(responseBody)
        return result.choices.firstOrNull()?.message?.content.orEmpty()
    }
}
