package ru.kyamshanov.missionChat.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val role: String, // "system", "user" или "assistant"
    val content: String
)

@Serializable
data class DeepSeekRequest(
    val model: String = "deepseek-chat", // или "deepseek-coder"
    val messages: List<Message>,
    val temperature: Double = 0.7,
    val stream: Boolean = false,
    val max_tokens: Int,
)

@Serializable
data class Choice(
    val index: Int,
    val message: Message
)

@Serializable
data class DeepSeekResponse(
    val id: String,
    val choices: List<Choice>
)

// Модели для стримингового ответа
@Serializable
data class Delta(
    val content: String? = null // Может быть null в начале или конце потока
)

@Serializable
data class StreamChoice(
    val delta: Delta
)

@Serializable
data class DeepSeekStreamResponse(
    val choices: List<StreamChoice>
)