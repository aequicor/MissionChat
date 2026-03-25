package ru.kyamshanov.missionChat.presentation.components

import kotlinx.coroutines.flow.StateFlow
import ru.kyamshanov.missionChat.presentation.models.ChatUiModel
import ru.kyamshanov.missionChat.presentation.models.TopicUiModel

interface SidebarComponent {

    val state: StateFlow<State>

    fun onSelect(chat: ChatUiModel, topic: ChatUiModel)

    fun addTopic(chat: ChatUiModel, topic: ChatUiModel)

    data class State(
        val activeChats: Map<ChatUiModel, List<TopicUiModel>> = emptyMap(),
        val archivedChats: Map<ChatUiModel, List<TopicUiModel>> = emptyMap(),
        val selectedChat: ChatUiModel? = null,
        val selectedTopic: TopicUiModel? = null,
        val isLoading: Boolean = false,
        val error: Throwable? = null
    ) {

        init {
            require(!(selectedChat != null && selectedTopic == null))
            if (selectedChat != null) {
                require(selectedTopic != null)
                require(
                    activeChats[selectedChat]?.contains(selectedTopic) == true
                            || archivedChats[selectedChat]?.contains(selectedTopic) == true
                )
            }
        }
    }
}
