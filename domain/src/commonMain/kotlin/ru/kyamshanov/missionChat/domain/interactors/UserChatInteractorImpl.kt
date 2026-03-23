@file:OptIn(ExperimentalTime::class)

package ru.kyamshanov.missionChat.domain.interactors

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ru.kyamshanov.missionChat.`domain-legacy`.models.ChatWindowState
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.MessageInference
import ru.kyamshanov.missionChat.data.models.Message
import ru.kyamshanov.missionChat.data.network.DeepseekApi
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.domain.repositories.ChatRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class UserChatInteractorImpl(
    private val repository: ChatRepository,
    private val api: DeepseekApi,
) : UserChatInteractor {

    override suspend fun getChats(limit: Int, before: LocalDateTime): List<Chat> =
        repository.getChats(limit, before)

    override suspend fun getTopics(chatId: Identifier, limit: Int, before: LocalDateTime): List<Topic> =
        repository.getTopics(chatId.toUuid(), limit, before)

    override suspend fun getMessages(topicId: Identifier, limit: Int, before: LocalDateTime): List<MessageInference> {
        return repository.getMessages(topicId.toUuid(), limit, before)
    }

    override suspend fun createChat(title: String, description: String?): Chat =
        repository.createChat(title, description)

    override suspend fun createTopic(chatId: Identifier, title: String): Topic =
        repository.createTopic(chatId.toUuid(), title)

    override fun sendMessage(topicId: Identifier, text: String): Flow<ChatWindowState.Answering> = flow {
        val userMessage = repository.sendMessage(topicId.toUuid(), text)
        val currentMessages = repository.getMessages(topicId.toUuid(), limit = 50)
        val chatHistory = currentMessages.map { Message(it.role, it.text) }
        val assistantMessageId = Identifier.random()
        var currentResponseContent = ""

        api.chatCompletionStream(
            userMessage = text,
            chatHistory = chatHistory.filterNot { it.role == "user" && it.content == text }
        )
            .collect { chunk ->
                currentResponseContent += chunk
                val answeringState = ChatWindowState.Answering(
                    MessageInference.AssistantMessage(
                        id = assistantMessageId,
                        text = currentResponseContent,
                        createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                        associatedHuman = (userMessage as? MessageInference.HumanMessage)?.human
                    )
                )
                emit(answeringState)
            }
    }

    override suspend fun deleteMessage(messageId: Identifier) {
        repository.deleteMessage(messageId.toUuid())
    }

    private val MessageInference.role: String
        get() = when (this) {
            is MessageInference.AssistantMessage -> "assistant"
            is MessageInference.HumanMessage -> "user"
            is MessageInference.SystemMessage -> "system"
            is MessageInference.FunctionCallingResponse -> "tool"
        }
}
