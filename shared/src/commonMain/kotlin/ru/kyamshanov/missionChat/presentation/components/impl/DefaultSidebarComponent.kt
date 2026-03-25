package ru.kyamshanov.missionChat.presentation.components.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.kyamshanov.missionChat.domain.interactors.UserChatInteractor
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.presentation.components.SidebarComponent

class DefaultSidebarComponent(
    componentContext: ComponentContext,
    private val userChatInteractor: UserChatInteractor,
    private val onSelectedCallback: (Chat, Topic) -> Unit,
) : SidebarComponent, ComponentContext by componentContext {

    private val scope = coroutineScope()
    private val _state = MutableStateFlow(SidebarComponent.State())
    override val state: StateFlow<SidebarComponent.State> = _state.asStateFlow()

    init {
        loadChatsAndTopics()
    }

    override fun onSelect(
        chat: Chat,
        topic: Topic
    ) {
        onSelectedCallback(chat, topic)
    }

    override fun addTopic(
        chat: Chat,
        topic: Topic
    ) {
        _state.update {
            val map = it.chatsWithTopics.toMutableMap()
            map[chat] = map.getOrDefault(chat, emptyList()) + topic
            it.copy(chatsWithTopics = map)
        }
    }

    private fun loadChatsAndTopics() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val chats = userChatInteractor.getChats()
                val chatsWithTopics = mutableMapOf<Chat, List<Topic>>()
                for (chat in chats) {
                    val topics = userChatInteractor.getTopics(chat.id)
                    chatsWithTopics[chat] = topics
                }
                _state.update { it.copy(chatsWithTopics = chatsWithTopics, isLoading = false) }
                chats.firstOrNull()?.let { chat ->
                    chatsWithTopics[chat]?.firstOrNull()?.let { topic -> onSelect(chat, topic) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e) }
            }
        }
    }

}
