package ru.kyamshanov.missionChat.domain.repositories

import kotlinx.datetime.LocalDateTime
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.MessageInference
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.domain.utils.now

internal interface ChatsDataSource {

    suspend fun <R> transaction(block: suspend ChatsDataSource.() -> R): R

    /**
     * return chats sorted by Chat#updatedAt from **oldest to newest** before $before
     * sorted(old --> new)
     */
    suspend fun getChats(
        limit: Int = 50,
        after: LocalDateTime? = null,
        before: LocalDateTime? = LocalDateTime.now(),
        isArchived: Boolean = false,
        isReversed: Boolean = false,
    ): List<Chat>

    suspend fun getTopics(
        chatId: Identifier,
        limit: Int = 50,
        after: LocalDateTime? = null,
        before: LocalDateTime? = LocalDateTime.now(),
        isArchived: Boolean = false,
        isReversed: Boolean = false,
    ): List<Topic>

    suspend fun getMessages(
        topicId: Identifier,
        limit: Int = 50,
        after: LocalDateTime? = null,
        before: LocalDateTime? = LocalDateTime.now(),
        isArchived: Boolean = false,
        isReversed: Boolean = false,
    ): List<MessageInference>

    suspend fun saveChat(chat: Chat)

    suspend fun getChat(chatId: Identifier): Chat

    suspend fun deleteChat(chatId: Identifier)

    suspend fun saveTopic(topic: Topic)

    suspend fun getTopic(topicId: Identifier): Topic

    suspend fun deleteTopic(topicId: Identifier)

    suspend fun saveMessage(
        topic: Topic,
        message: MessageInference
    )

    suspend fun getMessage(messageId: Identifier): MessageInference

    suspend fun deleteMessage(messageId: Identifier)
}