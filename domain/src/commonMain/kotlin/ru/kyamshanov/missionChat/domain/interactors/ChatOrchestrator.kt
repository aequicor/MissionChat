package ru.kyamshanov.missionChat.domain.interactors

import kotlinx.coroutines.flow.StateFlow
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.ChatPreview
import ru.kyamshanov.missionChat.domain.models.ChatsPaginationState
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.MessageInference
import ru.kyamshanov.missionChat.domain.models.MessagesPaginationState
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.domain.models.TopicsPaginationState

interface ChatListProvider {

    val activeChats: StateFlow<ChatsPaginationState>
    val archivedChats: StateFlow<ChatsPaginationState>

    suspend fun initChats()

    suspend fun loadNextActiveChats()
    suspend fun loadPreviousActiveChats()

    suspend fun loadNextArchiveChats()

    suspend fun loadPreviousArchiveChats()

    suspend fun archiveChat(chatId: Identifier)
    suspend fun unarchiveChat(chatId: Identifier)

    suspend fun startNewChat(): Chat

    fun getMessageProvider(chatId: Identifier, initialTopicId: Identifier): MessageProvider
}

/**
 * Интерфейс для управления сообщениями.
 */
interface MessageProvider {
    val messages: StateFlow<MessagesPaginationState>

    val currentTopic: StateFlow<Topic>

    suspend fun sendMessage(message: MessageInference.HumanMessage)

    suspend fun initMessages()

    suspend fun loadNextMessages()
    suspend fun loadPreviousMessages()

    suspend fun deleteMessage(topicId: Identifier, messageId: Identifier)

    suspend fun setCurrentTopic(topic: Topic)

    suspend fun startNewTopic()
}

/**
 * Общий оркестратор, объединяющий функциональность по SOLID (Interface Segregation).
 */
interface ChatOrchestrator : ChatListProvider
