package ru.kyamshanov.missionChat.data.repositories

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import jdk.jfr.internal.event.EventConfiguration.timestamp
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.JsonNull.content
import ru.kyamshanov.missionChat.data.database.AppDatabase
import ru.kyamshanov.missionChat.data.database.entities.ChatEntity
import ru.kyamshanov.missionChat.data.database.entities.MessageEntity
import ru.kyamshanov.missionChat.data.database.entities.TopicEntity
import ru.kyamshanov.missionChat.domain.models.*
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
    private val database: AppDatabase
) : ChatRepository {

    private val chatDao = database.chatDao()
    private val topicDao = database.topicDao()
    private val messageDao = database.messageDao()

    override suspend fun getChats(
        limit: Int,
        before: LocalDateTime,
        isArchived: Boolean
    ): List<Chat> =
        chatDao.getChats(limit, before.toEpochMilliseconds(), isArchived)
            .map { it.toDomain(topicDao.getTopicById(it.headTopic)!!.toDomain()) }

    override suspend fun getTopics(chatId: Identifier, limit: Int, before: LocalDateTime): List<Topic> {
        return topicDao.getTopics(chatId, limit, before.toEpochMilliseconds()).map { it.toDomain() }
    }

    override suspend fun getMessages(
        topicId: Identifier,
        limit: Int,
        before: LocalDateTime
    ): LinkedHashMap<Topic, List<MessageInference>> {
        val result = LinkedHashMap<Topic, List<MessageInference>>()
        var currentTopicId: Identifier? = topicId
        var currentBefore = before.toEpochMilliseconds()
        var items = 0

        while (currentTopicId != null && items < limit) {
            val topicEntity = topicDao.getTopicById(currentTopicId) ?: break
            val domainTopic = topicEntity.toDomain()
            val needed = limit - items

            val messages = messageDao.getMessages(currentTopicId.toString(), needed, currentBefore)
            result[domainTopic] = messages.map { it.toDomain() }
            items += messages.size

            val previousTopic = topicDao.getTopics(
                chatId = topicEntity.chatId,
                limit = 1,
                before = topicEntity.createdAt - 1,
            ).firstOrNull()
            currentTopicId = previousTopic?.id
            //currentBefore = Long.MAX_VALUE
        }
        return result
    }

    override suspend fun createChat(title: String, description: String?, firstTopicTitle: String): Chat {
        val now = LocalDateTime.nowEpochMilliseconds
        val topicId = Identifier.random()
        val chatId = Identifier.random()
        val topic = TopicEntity(
            id = topicId,
            chatId = chatId,
            title = title,
            createdAt = now,
            updatedAt = now
        )
        val chat = ChatEntity(
            id = chatId,
            title = title,
            description = description,
            createdAt = now,
            updatedAt = now,
            headTopic = topicId,
            isArchived = false,
        )
        database.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                chatDao.insertChat(chat)
                topicDao.insertTopic(topic)
            }
        }
        chatDao.insertChat(chat)
        return chat.toDomain(topic.toDomain())
    }

    override suspend fun updateChat(
        chatId: Identifier,
        title: String?,
        description: String?,
        isArchived: Boolean?
    ): Chat {
        val existing = chatDao.getChatById(chatId) ?: throw IllegalArgumentException("Chat not found")
        val updated = existing.copy(
            title = title ?: existing.title,
            description = description ?: existing.description,
            updatedAt = LocalDateTime.nowEpochMilliseconds,
            isArchived = isArchived ?: existing.isArchived,
        )
        chatDao.updateChat(updated)
        return updated.toDomain(topicDao.getTopicById(updated.headTopic)?.toDomain()!!)
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
            createdAt = now,
            updatedAt = now
        )
        val chat = chatDao.getChatById(chatId) ?: throw IllegalStateException("Chat with id $chatId not found")
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                chatDao.updateChat(chat.copy(headTopic = topic.id))
                topicDao.insertTopic(topic)
            }
        }
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

    override suspend fun saveMessage(
        topicId: Identifier,
        message: MessageInference
    ) {
        val entity = message.toEntity(topicId)
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

    private fun ChatEntity.toDomain(topic: Topic) = Chat(
        id = id,
        title = title,
        createdAt = createdAt.toLocalDateTime(),
        updatedAt = updatedAt.toLocalDateTime(),
        headTopic = topic.also { require(headTopic == it.id) { "" } }
    )

    private fun TopicEntity.toDomain() = Topic(
        id = id,
        title = title,
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
            )

            "TOOL" -> TODO()

            else -> MessageInference.HumanMessage(id, content, dateTime, Interlocutor.Human("Unknown"))
        }
    }

    private fun MessageInference.toEntity(topicId: Identifier): MessageEntity {
        val type = when (this) {
            is MessageInference.AssistantFunctionCalling -> {
                "TOOL"
            }

            is MessageInference.AssistantMessage -> {
                "ASSISTANT"
            }

            is MessageInference.FunctionCallingResponse -> {
                "TOOL"
            }

            is MessageInference.HumanMessage -> {
                "HUMAN"
            }

            is MessageInference.SystemMessage -> {
                "SYSTEM"
            }
        }
        return MessageEntity(
            id = id,
            conversationId = topicId.toString(),
            type = type,
            content = text,
            humanName = null,
            assistantAssociatedHumanName = null,
            toolId = null,
            timestamp = createdAt.toEpochMilliseconds()
        )
    }
}