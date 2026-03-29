package ru.kyamshanov.missionChat.domain.interactors

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    override suspend fun loadNextArchiveChat() {
        loadChats(isArchived = true)
    }

    override suspend fun loadNextActiveChat() {
        loadChats(isArchived = false)
    }

    override suspend fun loadPreviousArchiveChat() {
        // Not supported by the interactor which uses 'before' for pagination
    }

    override suspend fun loadPreviousActiveChat() {
        // Not supported by the interactor which uses 'before' for pagination
    }

    override suspend fun select(
        chatId: Identifier,
        topicId: Identifier
    ) {
        val (chat, topics) = activeChats.value.items.first { it.chat.id == chatId }
        val topic = topics.first { it.id == topicId }
        _selectedChat.value = chat
        _selectedTopic.value = topic
    }

    override suspend fun archiveChat(chatId: Identifier) {
        val (chat, _) = activeChats.value.items.first { it.chat.id == chatId }
        _activeChats.update {
            it.copy(items = it.items.filter { preview -> preview.chat.id != chatId })
        }
        _archivedChats.update {
            it.copy(items = it.items + ChatPreview(chat, emptyList()))
        }
        interactor.setArchivationChat(chat, true)
    }

    override suspend fun unarchiveChat(chatId: Identifier) {
        val (chat, _) = archivedChats.value.items.first { it.chat.id == chatId }
        _archivedChats.update {
            it.copy(items = it.items.filter { preview -> preview.chat.id != chatId })
        }
        _activeChats.update {
            it.copy(items = it.items + ChatPreview(chat, emptyList()))
        }
        interactor.setArchivationChat(chat, false)
    }

    override suspend fun startNewChat() {
        val chat = interactor.createChat("New chat v2", "some description", "Topic title v2")
        val topic = chat.headTopic
        _activeChats.update {
            it.copy(items = it.items + ChatPreview(chat, listOf(topic)))
        }
        select(chat.id, topic.id)
    }

    private suspend fun loadChats(isArchived: Boolean) {
        val stateFlow = if (isArchived) _archivedChats else _activeChats
        val current = stateFlow.value
        if (!current.hasNext || current.isLoadingNext) return

        stateFlow.update { it.copy(isLoadingNext = true) }
        try {
            val before = current.items.lastOrNull()?.chat?.createdAt ?: LocalDateTime.now()
            val chats = if (isArchived) {
                interactor.getArchivedChats(limit = LIMIT, before = before)
            } else {
                interactor.getActiveChats(limit = LIMIT, before = before)
            }

            val previews = chats.map { chat ->
                ChatPreview(chat, interactor.getTopics(chat.id, limit = 5))
            }

            stateFlow.update {
                it.copy(
                    items = it.items + previews,
                    hasNext = chats.size >= LIMIT,
                    isLoadingNext = false
                )
            }
        } catch (e: Exception) {
            stateFlow.update { it.copy(isLoadingNext = false) }
        }
    }

    override fun loadTopics(chat: Chat): TopicProvider = TopicProviderImpl(chat)

    override fun getMessageProvider(
        chatId: Identifier,
        initialTopicId: Identifier
    ): MessageProvider =
        MessageProviderImpl(chatId, initialTopicId)

    private inner class TopicProviderImpl(private val chat: Chat) : TopicProvider {
        private val _topics = MutableStateFlow<TopicsPaginationState?>(null)
        override val topics: StateFlow<TopicsPaginationState?> = _topics.asStateFlow()

        override suspend fun loadNext() {
            val current = _topics.value ?: TopicsPaginationState()
            if (!current.hasNext || current.isLoadingNext) return

            _topics.update {
                it?.copy(isLoadingNext = true) ?: TopicsPaginationState(isLoadingNext = true)
            }

            try {
                val before = current.items.lastOrNull()?.createdAt ?: LocalDateTime.now()
                val newTopics = interactor.getTopics(chat.id, limit = LIMIT, before = before)
                _topics.update {
                    it?.copy(
                        items = it.items + newTopics,
                        hasNext = newTopics.size >= LIMIT,
                        isLoadingNext = false
                    ) ?: TopicsPaginationState(
                        items = newTopics,
                        hasNext = newTopics.size >= LIMIT,
                        isLoadingNext = false
                    )
                }
            } catch (e: Exception) {
                _topics.update { it?.copy(isLoadingNext = false) }
            }
        }

        override suspend fun loadPrevious() {
            // Not supported
        }

        override suspend fun select(topic: Topic?) {
            if (topic != null) {
                this@ChatOrchestratorImpl.select(chat.id, topic.id)
            }
        }
    }

    private inner class MessageProviderImpl(
        private val chatId: Identifier,
        initialTopicId: Identifier
    ) : MessageProvider {
        private val _messages = MutableStateFlow<Map<Topic, List<MessageInference>>>(emptyMap())
        override val messages: StateFlow<Map<Topic, List<MessageInference>>> =
            _messages.asStateFlow()

        private val _currentTopic = MutableStateFlow<Topic?>(null)
        override val currentTopic: StateFlow<Topic?> = _currentTopic.asStateFlow()

        private var currentTopicId: Identifier = initialTopicId

        override suspend fun sendMessage(message: MessageInference.HumanMessage) {
            val currentTopic =
                _activeChats.value.items
                    .first { it.chat.id == chatId }.firstTopics
                    .first { it.id == currentTopicId }

            updateLocalMessages(currentTopic, message)

            val context = _messages.value[currentTopic] ?: emptyList()

            interactor.sendMessage(currentTopic, context, message).collect { updatedMessage ->
                updateLocalMessages(currentTopic, updatedMessage)
            }
        }

        private fun updateLocalMessages(topic: Topic, message: MessageInference) {
            _messages.update { current ->
                val topicMessages = current[topic] ?: emptyList()
                val index = topicMessages.indexOfFirst { it.id == message.id }
                val newList = if (index >= 0) {
                    topicMessages.toMutableList().apply { set(index, message) }
                } else {
                    (topicMessages + message).sortedBy { it.createdAt }
                }
                current + (topic to newList)
            }
        }

        override suspend fun loadNextMessages() {
            // Not supported
        }

        override suspend fun loadPreviousMessages() {
            val currentMessagesMap = _messages.value
            val oldestTopic = currentMessagesMap.keys.minByOrNull { it.createdAt }

            val topicIdToLoad = oldestTopic?.id ?: currentTopicId
            val before = currentMessagesMap[oldestTopic]?.firstOrNull()?.createdAt
                ?: oldestTopic?.createdAt
                ?: LocalDateTime.now()

            val result = interactor.getMessages(topicIdToLoad, limit = LIMIT, before = before)
            _messages.update { current ->
                val merged = current.toMutableMap()
                result.forEach { (t, msgs) ->
                    val existing = merged[t] ?: emptyList()
                    merged[t] = (msgs + existing).distinctBy { it.id }.sortedBy { it.createdAt }
                    if (t.id == currentTopicId && _currentTopic.value == null) {
                        _currentTopic.value = t
                    }
                }
                merged
            }
        }

        override suspend fun deleteMessage(messageId: Identifier) {
            interactor.deleteMessage(messageId)
            _messages.update { current ->
                current.mapValues { (_, msgs) -> msgs.filter { it.id != messageId } }
            }
        }

        override suspend fun setCurrentTopic(topic: Topic) {
            currentTopicId = topic.id
            _currentTopic.value = topic
            // If we don't have messages for this topic, load them
            if (!_messages.value.containsKey(topic)) {
                val initialMessages = interactor.getMessages(topic.id, limit = LIMIT)
                _messages.update { it + initialMessages }
            }
        }

        override suspend fun startNewTopic() {
            TODO()
        }
    }

    companion object {
        private const val LIMIT = 20
    }
}
