package ru.kyamshanov.missionChat.domain.interactors

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDateTime
import ru.kyamshanov.missionChat.data.models.Message
import ru.kyamshanov.missionChat.data.network.DeepseekApi
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.MessageInference
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.domain.repositories.ChatRepository
import ru.kyamshanov.missionChat.domain.utils.now

internal class UserChatInteractorImpl(
    private val repository: ChatRepository,
    private val api: DeepseekApi,
) : UserChatInteractor {

    override suspend fun getChats(limit: Int, before: LocalDateTime): List<Chat> =
        repository.getChats(limit, before)

    override suspend fun getTopics(chatId: Identifier, limit: Int, before: LocalDateTime): List<Topic> =
        repository.getTopics(chatId, limit, before)

    override suspend fun getMessages(
        topicId: Identifier,
        limit: Int,
        before: LocalDateTime
    ): LinkedHashMap<Topic, List<MessageInference>> = repository.getMessages(topicId, limit, before)

    override suspend fun createChat(title: String, description: String?, firstTopicTitle: String): Chat =
        repository.createChat(title, description, firstTopicTitle)

    override suspend fun createTopic(chatId: Identifier, title: String): Topic =
        repository.createTopic(chatId, title)

    override fun sendMessage(
        context: List<MessageInference>,
        message: MessageInference.HumanMessage
    ): Flow<MessageInference> = flow {
        val chatHistory = context.mapNotNull { inference ->
            when (inference) {
                is MessageInference.SystemMessage -> Message(role = "system", content = inference.text)
                is MessageInference.HumanMessage -> Message(role = "user", content = inference.text)
                is MessageInference.AssistantMessage -> Message(role = "assistant", content = inference.text)
                else -> null
            }
        }

        val assistantMessageId = Identifier.random()
        var fullText = ""

        api.chatCompletionStream(
            userMessage = message.text,
            chatHistory = chatHistory
        ).collect { chunk ->
            fullText += chunk
            emit(
                MessageInference.AssistantMessage(
                    id = assistantMessageId,
                    text = fullText,
                    createdAt = LocalDateTime.now()
                )
            )
        }
    }

    override suspend fun deleteMessage(messageId: Identifier) {
        repository.deleteMessage(messageId)
    }

}
