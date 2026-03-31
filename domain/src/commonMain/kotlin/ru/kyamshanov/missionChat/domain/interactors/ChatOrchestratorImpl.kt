package ru.kyamshanov.missionChat.domain.interactors

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDateTime
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.ChatPreview
import ru.kyamshanov.missionChat.domain.models.ChatsPaginationState
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.MessageInference
import ru.kyamshanov.missionChat.domain.models.MessagesPaginationState
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.domain.models.TopicMessages
import ru.kyamshanov.missionChat.domain.utils.now
import kotlin.math.PI

internal class ChatOrchestratorImpl(
    private val interactor: UserChatInteractor,
) : ChatOrchestrator {

    private val _activeChats = MutableStateFlow(ChatsPaginationState())
    override val activeChats: StateFlow<ChatsPaginationState> = _activeChats.asStateFlow()

    private val _archivedChats = MutableStateFlow(ChatsPaginationState())
    override val archivedChats: StateFlow<ChatsPaginationState> = _archivedChats.asStateFlow()

    override suspend fun initChats() = coroutineScope {
        val limit = 20
        val toPaginationState: suspend (List<Chat>) -> ChatsPaginationState = { chats ->
            val hasPrev = chats.size >= limit
            val chatPreviews = chats.map { chat ->
                val firstThreeTopics = interactor.getTopics(chat.id, limit = 3, isReversed = true)
                ChatPreview(chat, firstThreeTopics)
            }
            ChatsPaginationState(
                items = chatPreviews,
                hasNext = false,
                hasPrev = hasPrev,
            )
        }

        val active = async {
            toPaginationState(interactor.getActiveChats(limit = limit, isReversed = true))
        }
        val archive = async {
            toPaginationState(interactor.getArchivedChats(limit = limit, isReversed = true))
        }
        _activeChats.update { active.await() }
        _archivedChats.update { archive.await() }
    }

    override suspend fun loadNextArchiveChats() {
        TODO()
    }

    override suspend fun loadNextActiveChats() {
        TODO()
    }

    override suspend fun loadPreviousArchiveChats() {
        //loadPreviousChats(isArchived = true)
        TODO()
    }

    override suspend fun loadPreviousActiveChats() {
        //loadPreviousChats(isArchived = false)
        TODO()
    }

    override suspend fun archiveChat(chatId: Identifier) {
        val (chat, topics) = activeChats.value.items.first { it.chat.id == chatId }
        _activeChats.update {
            it.copy(items = it.items.filter { preview -> preview.chat.id != chatId })
        }
        _archivedChats.update {
            it.copy(items = it.items + ChatPreview(chat, topics))
        }
        interactor.setArchivationChat(chat, true)
    }

    override suspend fun unarchiveChat(chatId: Identifier) {
        val (chat, topics) = archivedChats.value.items.first { it.chat.id == chatId }
        _archivedChats.update {
            it.copy(items = it.items.filter { preview -> preview.chat.id != chatId })
        }
        _activeChats.update {
            it.copy(items = it.items + ChatPreview(chat, topics))
        }
        interactor.setArchivationChat(chat, false)
    }

    override suspend fun startNewChat(): Chat {
        val time = LocalDateTime.now()
        val chat = interactor.createChat(
            title = "Chat ${time.hour}:${time.minute}:${time.second}",
            description = null,
            firstTopicTitle = "Topic ${time.hour}:${time.minute}:${time.second}"
        )
        val topic = chat.headTopic

        _activeChats.update {
            it.copy(items = listOf(ChatPreview(chat, listOf(topic))) + it.items)
        }
        return chat
    }

    private suspend fun loadPreviousChats(isArchived: Boolean) {
        val stateFlow = if (isArchived) _archivedChats else _activeChats
        val current = stateFlow.value
        if (!current.hasPrev || current.isLoadingPrev) return

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
        private val _messages = MutableStateFlow(MessagesPaginationState(emptyList()))
        override val messages = _messages.asStateFlow()

        private val _currentTopic = MutableStateFlow(initialTopic)
        override val currentTopic = _currentTopic.asStateFlow()

        private val currentTopicId: Identifier
            get() = currentTopic.value.id
        private val chatId: Identifier = initialTopic.chatId


        override suspend fun sendMessage(message: MessageInference.HumanMessage) {
            val currentTopic = _currentTopic.value

            val context =
                _messages.value.items.firstOrNull { it.topic.id == currentTopic.id }?.messages.orEmpty()

            interactor.sendMessage(currentTopic, context, message)
                .onStart { insertMessageLocal(currentTopic, message) }
                .collect { updatedMessage ->
                    insertMessageLocal(currentTopic, updatedMessage)
                }
        }

        override suspend fun initMessages() {
            val limit = LIMIT
            val messages = interactor.getMessages(currentTopicId, limit = LIMIT)
            val hasPrev = messages.values.sumOf { it.size } < limit
            val hasNext = true
            val topicMessages = messages.map { (k, v) -> TopicMessages(k, v) }
            _messages.update { MessagesPaginationState(topicMessages, hasNext, hasPrev) }
        }

        private fun insertMessageLocal(topic: Topic, message: MessageInference) {
            _messages.update { state ->
                val updatedItems = state.items.map {
                    if (it.topic.id == topic.id) {
                        var isReplaced = false
                        val updatedMessaged = it.messages.map { msg ->
                            if (msg.id == message.id) {
                                isReplaced = true
                                message
                            } else msg
                        }

                        if (isReplaced) {
                            it.copy(messages = updatedMessaged)
                        } else {
                            it.copy(messages = it.messages + message)
                        }
                    } else it
                }
                state.copy(items = updatedItems)
            }
        }

        override suspend fun loadNextMessages() {
            // Not supported
        }

        override suspend fun loadPreviousMessages() {

        }

        override suspend fun deleteMessage(topicId: Identifier, messageId: Identifier) {

        }

        override suspend fun setCurrentTopic(topic: Topic) {
            _currentTopic.value = topic
        }

        override suspend fun startNewTopic() {
            val newTopic = interactor.createTopic(chatId, null)
            _currentTopic.update { newTopic }
            _messages.update {
                it.copy(items = it.items + TopicMessages(newTopic, emptyList()))
            }
            _activeChats.update { state ->
                val updatedItems = state.items.map {
                    if (it.chat.id == chatId) {
                        it.copy(firstTopics = listOf(newTopic) + it.firstTopics)
                    } else {
                        it
                    }
                }
                state.copy(items = updatedItems)
            }
        }
    }

    companion object {
        private const val LIMIT = 20
    }
}
