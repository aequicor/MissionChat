package ru.kyamshanov.missionChat.domain.interactors

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.ChatsPaginationState
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.MessageInference
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.domain.models.TopicsPaginationState

interface ChatListProvider {

    val activeChats: StateFlow<ChatsPaginationState>
    val archivedChats: StateFlow<ChatsPaginationState>
    val selectedChat: StateFlow<Chat?>
    val selectedTopic: StateFlow<Topic?>

    suspend fun loadNextArchiveChat()
    suspend fun loadNextUnarchiveChat()

    suspend fun loadPreviousArchiveChat()
    suspend fun loaPreviousUnarchiveChat()

    suspend fun select(topic: Pair<Chat, Topic>)

    suspend fun loadTopics(chat: Chat): TopicProvider
}

/**
 * Интерфейс для работы с топиками чата.
 */
interface TopicProvider {

    val topics: StateFlow<TopicsPaginationState?>

    suspend fun loadNext()
    suspend fun loadPrevious()

    suspend fun select(topic: Topic?)
}

/**
 * Интерфейс для управления сообщениями.
 */
interface MessageProvider {
    val messages: StateFlow<Map<Topic, List<MessageInference>>>

    fun sendMessage(
        message: MessageInference.HumanMessage
    ): Flow<MessageInference>

    suspend fun loadNextMessages()
    suspend fun loadPreviousMessages()

    suspend fun deleteMessage(messageId: Identifier)

    suspend fun setCurrentTopic(topic: Topic)
}

/**
 * Общий оркестратор, объединяющий функциональность по SOLID (Interface Segregation).
 */
interface ChatOrchestrator : ChatListProvider, MessageProvider
