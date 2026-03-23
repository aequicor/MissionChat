package ru.kyamshanov.missionChat.domain.repositories

import kotlinx.datetime.LocalDateTime
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.MessageInference
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.domain.utils.now

/**
 * Repository interface for managing chats, topics, and messages.
 * Provides functionality for fetching data and performing CRUD operations.
 */
internal interface ChatRepository {

    /**
     * Retrieves a flow of chats.
     *
     * @param limit The maximum number of chats to retrieve.
     * @param before The timestamp used for pagination; fetches chats created before this time.
     * @return A list of [ru.kyamshanov.missionChat.domain.models.Chat] objects.
     */
    suspend fun getChats(
        limit: Int,
        before: LocalDateTime = LocalDateTime.now()
    ): List<Chat>

    /**
     * Retrieves a flow of topics for a specific chat.
     *
     * @param chatId The unique identifier of the chat.
     * @param limit The maximum number of topics to retrieve in a single batch.
     * @param before The timestamp used for pagination; fetches topics created before this time.
     * @return A list of [ru.kyamshanov.missionChat.domain] objects.
     */
    suspend fun getTopics(
        chatId: Identifier,
        limit: Int,
        before: LocalDateTime = LocalDateTime.now()
    ): List<Topic>

    /**
     * Retrieves a flow of messages for a specific topic.
     *
     * @param topicId The unique identifier of the topic.
     * @param limit The maximum number of messages to retrieve.
     * @param before The timestamp used for pagination; fetches messages created before this time.
     * @return A list of [ru.kyamshanov.missionChat.domain] objects.
     */
    suspend fun getMessages(
        topicId: Identifier,
        limit: Int,
        before: LocalDateTime = LocalDateTime.now()
    ): List<Pair<Topic, MessageInference>>

    /**
     * Creates a new chat.
     *
     * @param title The title of the chat.
     * @param description An optional description of the chat.
     * @return The created [Chat] object.
     */
    suspend fun createChat(title: String, description: String?): Chat

    /**
     * Updates an existing chat's details.
     *
     * @param chatId The unique identifier of the chat to update.
     * @param title The new title (optional).
     * @param description The new description (optional).
     * @return The updated [Chat] object.
     */
    suspend fun updateChat(chatId: Identifier, title: String?, description: String?): Chat

    /**
     * Deletes a chat by its identifier.
     */
    suspend fun deleteChat(chatId: Identifier)

    /**
     * Creates a new topic within a chat.
     *
     * @param chatId The unique identifier of the parent chat.
     * @param title The title of the topic.
     * @return The created [ru.kyamshanov.missionChat.domain] object.
     */
    suspend fun createTopic(chatId: Identifier, title: String): Topic

    /**
     * Updates an existing topic's title.
     *
     * @param topicId The unique identifier of the topic to update.
     * @param title The new title.
     * @return The updated [ru.kyamshanov.missionChat.domain] object.
     */
    suspend fun updateTopic(topicId: Identifier, title: String): Topic

    /**
     * Deletes a topic by its identifier.
     */
    suspend fun deleteTopic(topicId: Identifier)

    /**
     * Sends a message to a specific topic.
     *
     * @param topicId The unique identifier of the topic.
     * @param text The content of the message.
     * @return The sent [ru.kyamshanov.missionChat.domain] object.
     */
    suspend fun sendMessage(topicId: Identifier, text: String): MessageInference

    /**
     * Saves an assistant message to a specific topic.
     *
     * @param topicId The unique identifier of the topic.
     * @param message The assistant message to save.
     */
    suspend fun saveAssistantMessage(topicId: Identifier, message: MessageInference.AssistantMessage)

    /**
     * Edits the content of an existing message.
     *
     * @param messageId The unique identifier of the message to edit.
     * @param text The new content of the message.
     * @return The updated [ru.kyamshanov.missionChat.domain] object.
     */
    suspend fun editMessage(messageId: Identifier, text: String): MessageInference

    /**
     * Deletes a message by its identifier.
     */
    suspend fun deleteMessage(messageId: Identifier)
}