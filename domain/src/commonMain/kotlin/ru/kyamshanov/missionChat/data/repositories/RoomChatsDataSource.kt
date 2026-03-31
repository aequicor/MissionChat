package ru.kyamshanov.missionChat.data.repositories

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import ru.kyamshanov.missionChat.data.database.AppDatabase
import ru.kyamshanov.missionChat.data.database.entities.toDomain
import ru.kyamshanov.missionChat.data.database.entities.toEntity
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.MessageInference
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.domain.repositories.ChatsDataSource

internal class RoomChatsDataSource(
    private val database: AppDatabase,
    private val ioDispatcher: CoroutineDispatcher
) : ChatsDataSource {

    private val chatDao = database.chatDao()
    private val topicDao = database.topicDao()
    private val messageDao = database.messageDao()

    override suspend fun <R> transaction(block: suspend ChatsDataSource.() -> R): R =
        withContext(ioDispatcher) {
            database.useWriterConnection { transactor ->
                transactor.immediateTransaction {
                    block()
                }
            }
        }

    override suspend fun getChats(
        limit: Int,
        after: LocalDateTime?,
        before: LocalDateTime?,
        isArchived: Boolean,
        isReversed: Boolean
    ): List<Chat> = withContext(ioDispatcher) {
        chatDao.getChats(limit, after, before, isArchived, isReversed)
            .map { it.toDomain { id -> getTopic(id) } }
    }

    override suspend fun getTopics(
        chatId: Identifier,
        limit: Int,
        after: LocalDateTime?,
        before: LocalDateTime?,
        isArchived: Boolean,
        isReversed: Boolean
    ): List<Topic> = withContext(ioDispatcher) {
        topicDao.getTopics(chatId, limit, after, before, isReversed)
            .map { it.toDomain() }
    }

    override suspend fun getMessages(
        topicId: Identifier,
        limit: Int,
        after: LocalDateTime?,
        before: LocalDateTime?,
        isArchived: Boolean,
        isReversed: Boolean
    ): List<MessageInference> = withContext(ioDispatcher) {
        messageDao.getMessages(topicId, limit, after, before, isReversed)
            .map { it.toDomain() }
    }

    override suspend fun saveTopic(topic: Topic): Unit = withContext(ioDispatcher) {
        topicDao.insertTopic(topic.toEntity())
    }

    override suspend fun getTopic(topicId: Identifier): Topic = withContext(ioDispatcher) {
        topicDao.getTopicById(topicId).toDomain()
    }

    override suspend fun deleteTopic(topicId: Identifier) {
        withContext(ioDispatcher) {
            topicDao.deleteTopic(topicId)
        }
    }

    override suspend fun saveChat(chat: Chat) {
        withContext(ioDispatcher) {
            chatDao.insertChat(chat.toEntity())
        }
    }

    override suspend fun getChat(chatId: Identifier): Chat =
        chatDao.getChatById(chatId).toDomain { id -> getTopic(id) }

    override suspend fun deleteChat(chatId: Identifier) {
        withContext(ioDispatcher) {
            chatDao.deleteChat(chatId)
        }
    }

    override suspend fun saveMessage(topic: Topic, message: MessageInference) {
        withContext(ioDispatcher) {
            messageDao.insert(message.toEntity(topic.id))
        }
    }

    override suspend fun getMessage(messageId: Identifier): MessageInference =
        withContext(ioDispatcher) {
            messageDao.getMessageById(messageId)?.toDomain()
                ?: throw IllegalArgumentException("Message not found")
        }

    override suspend fun deleteMessage(messageId: Identifier) {
        withContext(ioDispatcher) {
            messageDao.deleteMessage(messageId)
        }
    }
}
