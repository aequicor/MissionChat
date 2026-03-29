package ru.kyamshanov.missionChat.presentation.components

import kotlinx.coroutines.flow.StateFlow
import ru.kyamshanov.missionChat.presentation.models.ChatUiModel
import ru.kyamshanov.missionChat.presentation.models.TopicUiModel

interface SidebarComponent {

    val state: StateFlow<State>

    fun onSelect(chat: ChatUiModel, topic: TopicUiModel)

    fun archiveChat(chat: ChatUiModel)

    fun unarchiveChat(chat: ChatUiModel)

    data class State(
        val activeChats: List<ChatUiModel> = emptyList(),
        val archivedChats: List<ChatUiModel> = emptyList(),
        val selectedChat: ChatUiModel? = null,
        val selectedTopic: TopicUiModel? = null,
        val isLoading: Boolean = false,
        val error: Throwable? = null
    ) {

        init {
            require(!(selectedChat != null && selectedTopic == null))
        }
    }
}
