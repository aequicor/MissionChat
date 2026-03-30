package ru.kyamshanov.missionChat.data.repositories

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import kotlinx.datetime.LocalDateTime
import ru.kyamshanov.missionChat.data.database.AppDatabase
import ru.kyamshanov.missionChat.data.database.entities.toDomain
import ru.kyamshanov.missionChat.data.database.entities.toEntity
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.domain.repositories.ChatsDataSource

internal class RoomChatsDataSource(
    private val database: AppDatabase
) : ChatsDataSource {

    private val chatDao = database.chatDao()
    private val topicDao = database.topicDao()
    private val messageDao = database.messageDao()

    override suspend fun transaction(block: suspend ChatsDataSource.() -> Unit) {
        database.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                block()
            }
        }
    }

    override suspend fun getChatsHierarchicalBefore(
        limit: Int,
        before: LocalDateTime,
        isArchived: Boolean
    ): List<Chat> =
        chatDao.getChats(limit, before, isArchived)
            .map { it.toDomain { id -> getTopic(id) } }


    override suspend fun getChatsHierarchicalAfter(
        limit: Int,
        after: LocalDateTime,
        isArchived: Boolean
    ): List<Chat> {
        TODO("Not yet implemented")
    }

    override suspend fun getChatsReversedBefore(
        limit: Int,
        before: LocalDateTime,
        isArchived: Boolean
    ): List<Chat> {
        TODO("Not yet implemented")
    }

    override suspend fun getChatsReversedAfter(
        limit: Int,
        before: LocalDateTime,
        isArchived: Boolean
    ): List<Chat> {
        TODO("Not yet implemented")
    }

    override suspend fun saveTopic(topic: Topic) {
        topicDao.insertTopic(topic.toEntity())
    }

    override suspend fun getTopic(topicId: Identifier): Topic =
        topicDao.getTopicById(topicId).toDomain()
}
