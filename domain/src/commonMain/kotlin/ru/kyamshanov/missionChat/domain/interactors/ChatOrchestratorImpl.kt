package ru.kyamshanov.missionChat.domain.interactors

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDateTime
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.ChatPreview
import ru.kyamshanov.missionChat.domain.models.ChatsPaginationState
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.MessageInference
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.domain.models.TopicsPaginationState
import ru.kyamshanov.missionChat.domain.utils.now

internal class ChatOrchestratorImpl(
    private val interactor: UserChatInteractor,
) : ChatOrchestrator {

    private val _activeChats = MutableStateFlow(ChatsPaginationState())
    override val activeChats: StateFlow<ChatsPaginationState> = _activeChats.asStateFlow()

    private val _archivedChats = MutableStateFlow(ChatsPaginationState())
    override val archivedChats: StateFlow<ChatsPaginationState> = _archivedChats.asStateFlow()

    private val _selectedChat = MutableStateFlow<Chat?>(null)
    override val selectedChat: StateFlow<Chat?> = _selectedChat.asStateFlow()

    private val _selectedTopic = MutableStateFlow<Topic?>(null)
    override val selectedTopic: StateFlow<Topic?> = _selectedTopic.asStateFlow()

    private val _messages = MutableStateFlow<Map<Topic, List<MessageInference>>>(emptyMap())
    override val messages: StateFlow<Map<Topic, List<MessageInference>>> = _messages.asStateFlow()

    override suspend fun loadNextArchiveChat() {
        loadChats(isArchived = true, isNext = true)
    }

    override suspend fun loadNextUnarchiveChat() {
        loadChats(isArchived = false, isNext = true)
    }

    override suspend fun loadPreviousArchiveChat() {
        loadChats(isArchived = true, isNext = false)
    }

    override suspend fun loaPreviousUnarchiveChat() {
        loadChats(isArchived = false, isNext = false)
    }

    override suspend fun select(topic: Pair<Chat, Topic>) {
        _selectedChat.value = topic.first
        _selectedTopic.value = topic.second
        setCurrentTopic(topic.second)
    }

    override suspend fun loadTopics(chat: Chat): TopicProvider {
        return TopicProviderImpl(chat).apply { loadNext() }
    }

    override fun sendMessage(
        message: MessageInference.HumanMessage
    ): Flow<MessageInference> {
        val topic = _selectedTopic.value ?: throw IllegalStateException("No topic selected")

        updateMessages(topic, message)

        val context = _messages.value[topic] ?: emptyList()

        return interactor.sendMessage(topic, context, message)
            .onEach { updateMessages(topic, it) }
    }

    override suspend fun loadNextMessages() {
        // Текущий API интерактора не поддерживает загрузку "после" (forward pagination)
    }

    override suspend fun loadPreviousMessages() {
        val topic = _selectedTopic.value ?: return
        val currentMessagesMap = _messages.value

        val oldestTopic = currentMessagesMap.keys.minByOrNull { it.createdAt } ?: topic
        val oldestMessage = currentMessagesMap[oldestTopic]?.firstOrNull()

        val before = oldestMessage?.createdAt ?: oldestTopic.createdAt

        val olderMessagesMap =
            interactor.getMessages(oldestTopic.id, limit = PAGINATION_LIMIT, before = before)

        _messages.update { current ->
            val newMap = current.toMutableMap()
            olderMessagesMap.forEach { (t, msgs) ->
                val existing = newMap[t] ?: emptyList()
                newMap[t] = (msgs + existing).distinctBy { it.id }.sortedBy { it.createdAt }
            }
            newMap.toList().sortedBy { it.first.createdAt }.toMap()
        }
    }

    override suspend fun deleteMessage(messageId: Identifier) {
        interactor.deleteMessage(messageId)
        _messages.update { current ->
            current.mapValues { (_, msgs) -> msgs.filter { it.id != messageId } }
        }
    }

    override suspend fun setCurrentTopic(topic: Topic) {
        if (_selectedTopic.value?.id == topic.id && _messages.value.isNotEmpty()) return

        _selectedTopic.value = topic
        val chat = (_activeChats.value.items + _archivedChats.value.items)
            .find { it.chat.id == topic.chatId }?.chat
        _selectedChat.value = chat

        val initialMessages = interactor.getMessages(topic.id, limit = PAGINATION_LIMIT)
        _messages.value = initialMessages
    }

    private suspend fun loadChats(isArchived: Boolean, isNext: Boolean) {
        val stateFlow = if (isArchived) _archivedChats else _activeChats
        val currentState = stateFlow.value

        if (isNext && (!currentState.hasNext || currentState.isLoadingNext)) return
        if (!isNext && (!currentState.hasPrev || currentState.isLoadingPrev)) return

        stateFlow.update { it.copy(isLoadingNext = isNext, isLoadingPrev = !isNext) }

        try {
            val cursor = if (isNext) {
                currentState.items.lastOrNull()?.chat?.createdAt ?: LocalDateTime.now()
            } else {
                currentState.items.firstOrNull()?.chat?.createdAt ?: LocalDateTime.now()
            }

            val chats = if (isArchived) {
                interactor.getArchivedChats(limit = PAGINATION_LIMIT, before = cursor)
            } else {
                interactor.getActiveChats(limit = PAGINATION_LIMIT, before = cursor)
            }

            val previews = chats.map { chat ->
                ChatPreview(chat, interactor.getTopics(chat.id, limit = 5))
            }

            stateFlow.update { state ->
                if (isNext) {
                    state.copy(
                        items = state.items + previews,
                        hasNext = previews.size == PAGINATION_LIMIT,
                        isLoadingNext = false
                    )
                } else {
                    state.copy(isLoadingPrev = false)
                }
            }
        } catch (e: Exception) {
            stateFlow.update { it.copy(isLoadingNext = false, isLoadingPrev = false) }
        }
    }

    private fun updateMessages(topic: Topic, message: MessageInference) {
        _messages.update { current ->
            val topicMessages = current[topic] ?: emptyList()
            val existingIndex = topicMessages.indexOfFirst { it.id == message.id }
            val newList = if (existingIndex != -1) {
                topicMessages.toMutableList().apply { set(existingIndex, message) }
            } else {
                (topicMessages + message).sortedBy { it.createdAt }
            }
            (current + (topic to newList)).toList().sortedBy { it.first.createdAt }.toMap()
        }
    }

    private inner class TopicProviderImpl(
        private val chat: Chat,
    ) : TopicProvider {
        private val _topics = MutableStateFlow<TopicsPaginationState?>(null)
        override val topics: StateFlow<TopicsPaginationState?> = _topics.asStateFlow()

        override suspend fun loadNext() {
            val currentState = _topics.value ?: TopicsPaginationState()
            if (!currentState.hasNext || currentState.isLoadingNext) return

            _topics.value = currentState.copy(isLoadingNext = true)

            val before = currentState.items.lastOrNull()?.createdAt ?: LocalDateTime.now()
            val newTopics = interactor.getTopics(chat.id, limit = PAGINATION_LIMIT, before = before)

            _topics.value = currentState.copy(
                items = currentState.items + newTopics,
                hasNext = newTopics.size == PAGINATION_LIMIT,
                isLoadingNext = false
            )
        }

        override suspend fun loadPrevious() {
            // Не реализовано в интеракторе
        }

        override suspend fun select(topic: Topic?) {
            if (topic != null) {
                this@ChatOrchestratorImpl.select(chat to topic)
            } else {
                _selectedTopic.value = null
            }
        }
    }

    companion object {
        private const val PAGINATION_LIMIT = 50
    }
}
