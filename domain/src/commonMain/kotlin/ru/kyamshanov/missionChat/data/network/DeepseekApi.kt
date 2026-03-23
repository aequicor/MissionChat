package ru.kyamshanov.missionChat.data.network

import kotlinx.coroutines.flow.Flow
import ru.kyamshanov.missionChat.data.models.Message

/**
 * Интерфейс для взаимодействия с API Deepseek.
 */
internal interface DeepseekApi {

    /**
     * Выполняет потоковый запрос к чат-модели.
     *
     * @param userMessage Сообщение от пользователя.
     * @param model Идентификатор используемой модели (по умолчанию "deepseek-chat").
     * @param systemPrompt Системная инструкция для настройки поведения модели.
     * @param temperature Параметр креативности ответов (0.0 - 2.0).
     * @param maxTokens Максимальное количество токенов в ответе.
     * @return [Flow] со строковыми фрагментами (чанками) ответа.
     */
    fun chatCompletionStream(
        userMessage: String,
        chatHistory: List<Message> = emptyList(),
        model: String = DeepseekConstants.DEFAULT_MODEL,
        systemPrompt: String = DeepseekConstants.DEFAULT_SYSTEM_PROMPT,
        temperature: Double = 0.7,
        maxTokens: Int = 4096
    ): Flow<String>

    /**
     * Выполняет синхронный (suspend) запрос к чат-модели.
     *
     * @param userMessage Сообщение от пользователя.
     * @param chatHistory История переписки.
     * @param model Идентификатор используемой модели.
     * @param systemPrompt Системная инструкция.
     * @param temperature Параметр креативности.
     * @param maxTokens Максимальное количество токенов.
     * @return Полный текст ответа модели.
     */
    suspend fun chatCompletion(
        userMessage: String,
        chatHistory: List<Message> = emptyList(),
        model: String = DeepseekConstants.DEFAULT_MODEL,
        systemPrompt: String = DeepseekConstants.DEFAULT_SYSTEM_PROMPT,
        temperature: Double = 0.7,
        maxTokens: Int = 4096
    ): String
}
