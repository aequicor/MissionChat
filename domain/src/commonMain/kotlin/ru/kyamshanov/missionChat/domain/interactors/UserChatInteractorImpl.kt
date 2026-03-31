package ru.kyamshanov.missionChat.domain.interactors

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.datetime.LocalDateTime
import ru.kyamshanov.missionChat.data.models.Message
import ru.kyamshanov.missionChat.data.network.DeepseekApi
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.MessageInference
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.domain.repositories.ChatsDataSource
import ru.kyamshanov.missionChat.domain.utils.now

internal class UserChatInteractorImpl(
    private val repository: ChatsDataSource,
    private val api: DeepseekApi,
) : UserChatInteractor {

    override suspend fun getActiveChats(
        limit: Int,
        before: LocalDateTime,
        isReversed: Boolean
    ): List<Chat> = repository.getChats(
        limit = limit,
        before = before,
        isArchived = false,
        isReversed = isReversed,
    )

    override suspend fun getArchivedChats(
        limit: Int,
        before: LocalDateTime,
        isReversed: Boolean
    ): List<Chat> = repository.getChats(
        limit = limit,
        before = before,
        isArchived = true,
        isReversed = isReversed,
    )

    override suspend fun getTopics(
        chatId: Identifier,
        limit: Int,
        before: LocalDateTime,
        isReversed: Boolean
    ): List<Topic> =
        repository.getTopics(
            chatId = chatId,
            limit = limit,
            before = before,
            isReversed = isReversed,
        )

    override suspend fun getMessages(
        topicId: Identifier,
        limit: Int,
        before: LocalDateTime
    ): Map<Topic, List<MessageInference>> {
        val currentTopic = repository.getTopic(topicId)
        val topics = repository.getTopics(
            chatId = currentTopic.chatId,
            limit = limit,
            before = before,
            isReversed = true
        ).reversed()

        val result = mutableMapOf<Topic, List<MessageInference>>()
        var remainingLimit = limit

        for (topic in topics) {
            if (remainingLimit <= 0) break
            val messages = repository.getMessages(
                topicId = topic.id,
                limit = remainingLimit,
                before = if (topic.id == topicId) before else null,
                isReversed = true
            ).reversed()

            result[topic] = messages
            remainingLimit -= messages.size
        }

        return result
    }

    override suspend fun createChat(
        title: String,
        description: String?,
        firstTopicTitle: String
    ): Chat {
        val chatId = Identifier.new()
        val headTopicId = Identifier.new()

        val initTopic = Topic(
            id = headTopicId,
            chatId = chatId,
            title = firstTopicTitle,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val newChat = Chat(
            id = chatId,
            title = title,
            description = description,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            headTopic = initTopic,
            isArchived = false,
        )
        return repository.transaction {
            saveChat(newChat)
            saveTopic(initTopic)
            newChat
        }
    }

    override suspend fun createTopic(chatId: Identifier, title: String?): Topic =
        Topic(
            id = Identifier.new(),
            chatId = chatId,
            title = title ?: "Topic ${
                LocalDateTime.now().let { "${it.hour}:${it.minute}:${it.second}" }
            }",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        ).also { repository.saveTopic(it) }

    override fun sendMessage(
        topic: Topic,
        context: List<MessageInference>,
        message: MessageInference.HumanMessage
    ): Flow<MessageInference> = flow {
        val chatHistory = context.mapNotNull { inference ->
            when (inference) {
                is MessageInference.SystemMessage -> Message(
                    role = "system",
                    content = inference.text
                )

                is MessageInference.HumanMessage -> Message(role = "user", content = inference.text)
                is MessageInference.AssistantMessage -> Message(
                    role = "assistant",
                    content = inference.text
                )

                else -> null
            }
        }

        val assistantMessageId = Identifier.new()
        var fullText = ""
        var lastMessage: MessageInference = MessageInference.AssistantMessage(
            id = assistantMessageId,
            text = "Thinking...",
            createdAt = LocalDateTime.now()
        ).also { emit(it) }
        repository.saveMessage(topic, message)
        api.chatCompletionStream(
            userMessage = message.text,
            chatHistory = chatHistory
        ).onCompletion {
            lastMessage.also { repository.saveMessage(topic, it) }
        }.collect { chunk ->
            fullText += chunk
            emit(
                MessageInference.AssistantMessage(
                    id = assistantMessageId,
                    text = fullText,
                    createdAt = LocalDateTime.now()
                ).also { lastMessage = it }
            )
        }
    }

    override suspend fun deleteMessage(messageId: Identifier) {
        repository.deleteMessage(messageId)
    }

    override suspend fun setArchivationChat(
        chat: Chat,
        isArchived: Boolean
    ) {
        val updatedChat = chat.copy(isArchived = isArchived)
        repository.saveChat(updatedChat)
    }

}
