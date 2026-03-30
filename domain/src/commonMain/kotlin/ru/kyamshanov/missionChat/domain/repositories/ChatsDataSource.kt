package ru.kyamshanov.missionChat.domain.repositories

import kotlinx.datetime.LocalDateTime
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.MessageInference
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.domain.utils.now

internal interface ChatsDataSource {

    suspend fun transaction(block: suspend ChatsDataSource.() -> Unit)

    /**
     * return chats sorted by Chat#updatedAt from **oldest to newest** before $before
     * sorted(old --> new)
     */
    suspend fun getChatsHierarchicalBefore(
        limit: Int,
        before: LocalDateTime,
        isArchived: Boolean
    ): List<Chat>

    suspend fun getChatsHierarchicalAfter(
        limit: Int,
        after: LocalDateTime,
        isArchived: Boolean
    ): List<Chat>

    /**
     * return chats sorted by Chat#updatedAt from **newest to oldest** before $before
     * sorted(new --> old)
     */
    suspend fun getChatsReversedBefore(
        limit: Int,
        before: LocalDateTime,
        isArchived: Boolean
    ): List<Chat>

    suspend fun getChatsReversedAfter(
        limit: Int,
        before: LocalDateTime,
        isArchived: Boolean
    ): List<Chat>

}