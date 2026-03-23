package ru.kyamshanov.missionChat.data.repositories

import kotlinx.datetime.LocalDateTime
import ru.kyamshanov.missionChat.data.database.AppDatabase
import ru.kyamshanov.missionChat.data.database.entities.ChatEntity
import ru.kyamshanov.missionChat.data.database.entities.MessageEntity
import ru.kyamshanov.missionChat.data.database.entities.TopicEntity
import ru.kyamshanov.missionChat.domain.models.Interlocutor
import ru.kyamshanov.missionChat.domain.models.MessageInference
import ru.kyamshanov.missionChat.domain.models.Tool
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.repositories.ChatRepository
import ru.kyamshanov.missionChat.domain.utils.nowEpochMilliseconds
import ru.kyamshanov.missionChat.domain.utils.toEpochMilliseconds
import ru.kyamshanov.missionChat.domain.utils.toLocalDateTime

/**
 * Room-based implementation of [ChatRepository].
 *
 * This repository handles data operations for chats, topics, and messages using the local Room database.
 * It maps database entities to domain models and vice versa.
 */
internal class RoomChatRepository(
    database: AppDatabase
) : ChatRepository {

    private val chatDao = database.chatDao()
    private val topicDao = database.topicDao()
    private val messageDao = database.messageDao()

    override suspend fun getChats(limit: Int, before: LocalDateTime): List<Chat> {
        return chatDao.getChats(limit, before.toEpochMilliseconds()).map { it.toDomain() }
    }

    override suspend fun getTopics(chatId: Identifier, limit: Int, before: LocalDateTime): List<Topic> {
        return topicDao.getTopics(chatId, limit, before.toEpochMilliseconds()).map { it.toDomain() }
    }

    override suspend fun getMessages(topicId: Identifier, limit: Int, before: LocalDateTime?): List<MessageInference> {
        val beforeMillis = before?.toEpochMilliseconds() ?: LocalDateTime.nowEpochMilliseconds
        return messageDao.getMessages(topicId.toString(), limit, beforeMillis).map { it.toDomain() }
    }

    override suspend fun createChat(title: String, description: String?): Chat {
        val now = LocalDateTime.nowEpochMilliseconds
        val chat = ChatEntity(
            id = Identifier.random(),
            title = title,
            description = description,
            createdAt = now,
            updatedAt = now
        )
        chatDao.insertChat(chat)
        return chat.toDomain()
    }

    override suspend fun updateChat(chatId: Identifier, title: String?, description: String?): Chat {
        val existing = chatDao.getChatById(chatId) ?: throw IllegalArgumentException("Chat not found")
        val updated = existing.copy(
            title = title ?: existing.title,
            description = description ?: existing.description,
            updatedAt = LocalDateTime.nowEpochMilliseconds,
        )
        chatDao.updateChat(updated)
        return updated.toDomain()
    }

    override suspend fun deleteChat(chatId: Identifier) {
        chatDao.deleteChat(chatId)
    }

    override suspend fun createTopic(chatId: Identifier, title: String): Topic {
        val now = LocalDateTime.nowEpochMilliseconds
        val topic = TopicEntity(
            id = Identifier.random(),
            chatId = chatId,
            title = title,
            previousTopicId = null,
            nextTopicId = null,
            createdAt = now,
            updatedAt = now
        )
        topicDao.insertTopic(topic)
        return topic.toDomain()
    }

    override suspend fun updateTopic(topicId: Identifier, title: String): Topic {
        val existing = topicDao.getTopicById(topicId) ?: throw IllegalArgumentException("Topic not found")
        val updated = existing.copy(
            title = title,
            updatedAt = LocalDateTime.nowEpochMilliseconds
        )
        topicDao.updateTopic(updated)
        return updated.toDomain()
    }

    override suspend fun deleteTopic(topicId: Identifier) {
        topicDao.deleteTopic(topicId)
    }

    override suspend fun sendMessage(topicId: Identifier, text: String): MessageInference {
        val now = LocalDateTime.nowEpochMilliseconds
        val message = MessageEntity(
            id = Identifier.random(),
            conversationId = topicId.toString(),
            type = "HUMAN",
            content = text,
            humanName = "User",
            timestamp = now
        )
        messageDao.insert(message)
        return message.toDomain()
    }

    override suspend fun saveAssistantMessage(topicId: Identifier, message: MessageInference.AssistantMessage) {
        val entity = MessageEntity(
            id = message.id,
            conversationId = topicId.toString(),
            type = "ASSISTANT",
            content = message.text,
            assistantAssociatedHumanName = message.associatedHuman?.name,
            responseStartWith = message.responseStartWith,
            timestamp = message.createdAt.toEpochMilliseconds()
        )
        messageDao.insert(entity)
    }

    override suspend fun editMessage(messageId: Identifier, text: String): MessageInference {
        val existing = messageDao.getMessageById(messageId) ?: throw IllegalArgumentException("Message not found")
        val updated = existing.copy(
            content = text,
            timestamp = LocalDateTime.nowEpochMilliseconds
        )
        messageDao.update(updated)
        return updated.toDomain()
    }

    override suspend fun deleteMessage(messageId: Identifier) {
        messageDao.deleteMessage(messageId)
    }

    private fun ChatEntity.toDomain() = Chat(
        id = id,
        title = title,
        createdAt = createdAt.toLocalDateTime(),
        updatedAt = updatedAt.toLocalDateTime()
    )

    private fun TopicEntity.toDomain() = Topic(
        id = id,
        title = title,
        previousTopicId = previousTopicId,
        nextTopicId = nextTopicId,
        createdAt = createdAt.toLocalDateTime(),
        updatedAt = updatedAt.toLocalDateTime()
    )

    private fun MessageEntity.toDomain(): MessageInference {
        val dateTime = timestamp.toLocalDateTime()
        return when (type) {
            "SYSTEM" -> MessageInference.SystemMessage(id, content, dateTime, humanName?.let { Interlocutor.Human(it) })
            "HUMAN" -> MessageInference.HumanMessage(id, content, dateTime, Interlocutor.Human(humanName ?: "Unknown"))
            "ASSISTANT" -> MessageInference.AssistantMessage(
                id,
                content,
                dateTime,
                assistantAssociatedHumanName?.let { Interlocutor.Human(it) },
                responseStartWith
            )

            "TOOL" -> MessageInference.FunctionCallingResponse(id, content, dateTime, object : Tool {
                override val id: String = toolId ?: ""
            })

            else -> MessageInference.HumanMessage(id, content, dateTime, Interlocutor.Human("Unknown"))
        }
    }
}