package ru.kyamshanov.missionChat.domain.interactors

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDateTime
import ru.kyamshanov.missionChat.domain.models.ChatPreview
import ru.kyamshanov.missionChat.domain.models.ChatsPaginationState
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.MessageInference
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.domain.utils.now

internal class ChatOrchestratorImpl(
    private val interactor: UserChatInteractor,
) : ChatOrchestrator {

    private val _activeChats = MutableStateFlow(ChatsPaginationState())
    override val activeChats: StateFlow<ChatsPaginationState> = _activeChats.asStateFlow()

    private val _archivedChats = MutableStateFlow(ChatsPaginationState())
    override val archivedChats: StateFlow<ChatsPaginationState> = _archivedChats.asStateFlow()

    override suspend fun loadNextArchiveChat() {
        loadPreviousChats(isArchived = true)
    }

    override suspend fun loadNextActiveChat() {
        loadPreviousChats(isArchived = false)
    }

    override suspend fun loadPreviousArchiveChat() {
        // Not supported by the interactor which uses 'before' for pagination
    }

    override suspend fun loadPreviousActiveChat() {
        // Not supported by the interactor which uses 'before' for pagination
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
    }

    private suspend fun loadPreviousChats(isArchived: Boolean) {
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

    override fun getMessageProvider(
        chatId: Identifier,
        initialTopicId: Identifier
    ): MessageProvider =
        activeChats.value.items
            .first { it.chat.id == chatId }.firstTopics
            .first { it.id == initialTopicId }
            .let { topic ->
                MessageProviderImpl(topic)
            }

    private inner class MessageProviderImpl(
        initialTopic: Topic
    ) : MessageProvider {
        private val _messages =
            MutableStateFlow<Map<Topic, List<MessageInference>>>(mapOf(initialTopic to emptyList()))
        override val messages: StateFlow<Map<Topic, List<MessageInference>>> =
            _messages.asStateFlow()

        private val _currentTopic = MutableStateFlow(initialTopic)
        override val currentTopic: StateFlow<Topic> = _currentTopic.asStateFlow()

        private val chatId: Identifier = initialTopic.chatId
        private val currentTopicId: Identifier
            get() = currentTopic.value.id


        override suspend fun sendMessage(message: MessageInference.HumanMessage) {
            val currentTopic = _currentTopic.value
            insertMessageLocal(currentTopic, message)

            val context = _messages.value[currentTopic] ?: emptyList()

            interactor.sendMessage(currentTopic, context, message).collect { updatedMessage ->
                insertMessageLocal(currentTopic, updatedMessage)
            }
        }

        private fun insertMessageLocal(topic: Topic, message: MessageInference) {
            _messages.update { current ->
                val topicMessages = current[topic] ?: emptyList()
                val index = topicMessages.indexOfFirst { it.id == message.id }
                val newList = if (index >= 0) {
                    topicMessages.toMutableList().apply { set(index, message) }
                } else {
                    topicMessages + message
                }
                current + (topic to newList)
            }
        }

        override suspend fun loadNextMessages() {
            // Not supported
        }

        override suspend fun loadPreviousMessages() {
            val topicIdToLoad = currentTopicId
            val before = LocalDateTime.now()

            val result = interactor.getMessages(topicIdToLoad, limit = LIMIT, before = before)
            println(result)
            _messages.update { current ->
                val merged = current.toMutableMap()
                val startWith = mutableMapOf<Topic, List<MessageInference>>()
                result.forEach { (t, msgs) ->
                    merged[t]?.also { old ->
                        merged[t] = msgs + old
                    } ?: run {
                        startWith[t] = msgs
                    }
                }
                startWith + merged
            }
        }

        override suspend fun deleteMessage(topicId: Identifier, messageId: Identifier) {
            interactor.deleteMessage(messageId)
            _messages.update { current ->
                current.mapValues { (_, msgs) -> msgs.filter { it.id != messageId } }
            }
        }

        override suspend fun setCurrentTopic(topic: Topic) {
            _currentTopic.value = topic
        }

        override suspend fun startNewTopic() {
            val topic = interactor.createTopic(chatId, null)
            _messages.update {
                it.toMutableMap().apply { put(topic, emptyList()) }
            }
            _activeChats.update {
                val newItems = it.items.map { preview ->
                    if (preview.chat.id == chatId) {
                        preview.copy(firstTopics = preview.firstTopics + topic)
                    } else preview
                }
                it.copy(items = newItems)
            }
            _currentTopic.value = topic
        }
    }

    companion object {
        private const val LIMIT = 20
    }
}
