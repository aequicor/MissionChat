package ru.kyamshanov.missionChat.data.repositories

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import kotlinx.datetime.LocalDateTime
import ru.kyamshanov.missionChat.data.database.AppDatabase
import ru.kyamshanov.missionChat.data.database.entities.ChatEntity
import ru.kyamshanov.missionChat.data.database.entities.MessageEntity
import ru.kyamshanov.missionChat.data.database.entities.TopicEntity
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.Interlocutor
import ru.kyamshanov.missionChat.domain.models.MessageInference
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.domain.repositories.ChatRepository
import ru.kyamshanov.missionChat.domain.utils.now

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
        chatDao.getChats(limit, before, isArchived)
            .map { it.toDomain(topicDao.getTopicById(it.headTopic)!!.toDomain()) }

    override suspend fun getTopics(
        chatId: Identifier,
        limit: Int,
        before: LocalDateTime
    ): List<Topic> {
        return topicDao.getTopics(chatId, limit, before).map { it.toDomain() }
    }

    /**
     * Получает сообщения, начиная с указанной темы, с поддержкой пагинации по темам чата.
     *
     * **Порядок сортировки:**
     * - **Темы** в результирующем [LinkedHashMap] идут в хронологическом порядке: **от старых к новым**.
     * - **Сообщения** внутри каждой темы также отсортированы в хронологическом порядке: **от старых к новым** (самые свежие сообщения — в конце списка).
     *
     * Алгоритм:
     * 1. Запрашивает сообщения для текущей темы [currentTopicId], не позднее [currentBefore].
     * 2. Если сообщений получено меньше, чем [remainingLimit], ищет предыдущую тему в этом же чате.
     * 3. Если предыдущая тема найдена, повторяет процесс для неё, сбрасывая временное ограничение
     *    на "сейчас" (чтобы забрать последние сообщения из старой темы).
     * 4. После сбора данных (которые собирались от новых к старым) результат разворачивается,
     *    чтобы и темы, и сообщения внутри них шли от старых к новым.
     */
    override suspend fun getMessages(
        topicId: Identifier,
        limit: Int,
        before: LocalDateTime
    ): LinkedHashMap<Topic, List<MessageInference>> {
        val result = LinkedHashMap<Topic, List<MessageInference>>()
        var currentTopicId: Identifier? = topicId
        var currentBefore = before
        var remainingLimit = limit

        while (currentTopicId != null && remainingLimit > 0) {
            val topicEntity = topicDao.getTopicById(currentTopicId) ?: break
            val domainTopic = topicEntity.toDomain()

            val messages = messageDao.getMessages(
                topicId = currentTopicId,
                limit = remainingLimit,
                before = currentBefore
            )

            if (messages.isNotEmpty()) {
                result[domainTopic] = messages.map { it.toDomain() }
                remainingLimit -= messages.size
            }

            if (remainingLimit > 0) {
                val previousTopic = topicDao.getTopicsReversed(
                    chatId = topicEntity.chatId,
                    limit = 1,
                    before = topicEntity.createdAt
                ).firstOrNull()

                currentTopicId = previousTopic?.id
                currentBefore = LocalDateTime.now()
            } else {
                currentTopicId = null
            }
        }

        val chronologicalMap = LinkedHashMap<Topic, List<MessageInference>>()
        result.keys.reversed().forEach { topic ->
            chronologicalMap[topic] = result[topic]!!
        }
        return chronologicalMap
    }

    override suspend fun createChat(
        title: String,
        description: String?,
        firstTopicTitle: String
    ): Chat {
        val now = LocalDateTime.now()
        val topicId = Identifier.new()
        val chatId = Identifier.new()
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
        return chat.toDomain(topic.toDomain())
    }

    override suspend fun updateChat(
        chatId: Identifier,
        title: String?,
        description: String?,
        isArchived: Boolean?
    ): Chat {
        val existing =
            chatDao.getChatById(chatId) ?: throw IllegalArgumentException("Chat not found")
        val updated = existing.copy(
            title = title ?: existing.title,
            description = description ?: existing.description,
            updatedAt = LocalDateTime.now(),
            isArchived = isArchived ?: existing.isArchived,
        )
        chatDao.updateChat(updated)
        return updated.toDomain(topicDao.getTopicById(updated.headTopic)?.toDomain()!!)
    }

    override suspend fun deleteChat(chatId: Identifier) {
        chatDao.deleteChat(chatId)
    }

    override suspend fun createTopic(chatId: Identifier, title: String?): Topic {
        val now = LocalDateTime.now()
        val topic = TopicEntity(
            id = Identifier.new(),
            chatId = chatId,
            title = title ?: "Topic ${now.hour}:${now.minute}",
            createdAt = now,
            updatedAt = now
        )
        val chat = chatDao.getChatById(chatId)
            ?: throw IllegalStateException("Chat with id $chatId not found")
        database.useWriterConnection { connection ->
            connection.immediateTransaction {
                chatDao.updateChat(chat.copy(headTopic = topic.id))
                topicDao.insertTopic(topic)
            }
        }
        return topic.toDomain()
    }

    override suspend fun updateTopic(topicId: Identifier, title: String): Topic {
        val existing =
            topicDao.getTopicById(topicId) ?: throw IllegalArgumentException("Topic not found")
        val updated = existing.copy(
            title = title,
            updatedAt = LocalDateTime.now()
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
        val existing = messageDao.getMessageById(messageId)
            ?: throw IllegalArgumentException("Message not found")
        val updated = existing.copy(
            content = text,
            updatedAt = LocalDateTime.now()
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
        createdAt = createdAt,
        updatedAt = updatedAt,
        headTopic = topic.also { require(headTopic == it.id) { "" } },
        description = description,
        isArchived = isArchived,
    )

    private fun TopicEntity.toDomain() = Topic(
        id = id,
        chatId = chatId,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun MessageEntity.toDomain(): MessageInference {
        return when (type) {
            "SYSTEM" -> MessageInference.SystemMessage(id, content, createdAt, null)
            "HUMAN" -> MessageInference.HumanMessage(
                id,
                content,
                createdAt,
                Interlocutor.Human("Unknown")
            )

            "ASSISTANT" -> MessageInference.AssistantMessage(
                id,
                content,
                createdAt,
                null,
            )

            else -> MessageInference.HumanMessage(
                id,
                content,
                createdAt,
                Interlocutor.Human("Unknown")
            )
        }
    }

    private fun MessageInference.toEntity(topicId: Identifier): MessageEntity {
        val type = when (this) {
            is MessageInference.AssistantFunctionCalling -> "TOOL"
            is MessageInference.AssistantMessage -> "ASSISTANT"
            is MessageInference.FunctionCallingResponse -> "TOOL"
            is MessageInference.HumanMessage -> "HUMAN"
            is MessageInference.SystemMessage -> "SYSTEM"
        }
        return MessageEntity(
            id = id,
            topicId = topicId,
            type = type,
            content = text,
            createdAt = createdAt,
            updatedAt = createdAt
        )
    }
}
