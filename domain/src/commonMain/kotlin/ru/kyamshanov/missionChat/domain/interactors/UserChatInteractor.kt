package ru.kyamshanov.missionChat.domain.interactors

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import ru.kyamshanov.missionChat.domain.models.MessageInference
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.domain.utils.now

/**
 * Interactor for managing user chats, topics, and messages.
 */
interface UserChatInteractor {

    /**
     * Retrieves a list of chats for the current user.
     * @param limit The maximum number of chats to retrieve.
     * @param before The timestamp to fetch chats created before.
     * @return A list of [Chat] objects.
     */
    suspend fun getChats(
        limit: Int = 50,
        before: LocalDateTime = LocalDateTime.now(),
    ): List<Chat>

    /**
     * Retrieves a list of topics within a specific chat.
     * @param chatId The unique identifier of the chat.
     * @param limit The maximum number of topics to retrieve.
     * @param before The timestamp to fetch topics created before.
     * @return A list of [Topic] objects.
     */
    suspend fun getTopics(
        chatId: Identifier,
        limit: Int = 50,
        before: LocalDateTime = LocalDateTime.now(),
    ): List<Topic>

    /**
     * Retrieves a list of messages within a specific topic.
     * @param topicId The unique identifier of the topic.
     * @param limit The maximum number of messages to retrieve.
     * @param before The timestamp to fetch messages created before.
     * @return A list of [MessageInference] objects.
     */
    suspend fun getMessages(
        topicId: Identifier,
        limit: Int = 50,
        before: LocalDateTime = LocalDateTime.now(),
    ): List<Pair<Topic, MessageInference>>

    /**
     * Creates a new chat.
     * @param title The title of the chat.
     * @param description An optional description for the chat.
     * @return The created [Chat] object.
     */
    suspend fun createChat(title: String, description: String? = null): Chat

    /**
     * Creates a new topic within a chat.
     * @param chatId The unique identifier of the chat where the topic will be created.
     * @param title The title of the topic.
     * @return The created [Topic] object.
     */
    suspend fun createTopic(chatId: Identifier, title: String): Topic

    /**
     * Sends a message and receives a stream of response updates.
     * @param context The list of previous messages to provide context for the conversation.
     * @param message The human-generated message to be sent.
     * @return A [Flow] emitting [MessageInference] updates (e.g., streaming AI response).
     */
    fun sendMessage(context: List<MessageInference>, message: MessageInference.HumanMessage): Flow<MessageInference>

    /**
     * Deletes a message by its identifier.
     * @param messageId The unique identifier of the message to delete.
     */
    suspend fun deleteMessage(messageId: Identifier)
}
