package ru.kyamshanov.missionChat.presentation.components.impl

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.presentation.components.internal.InternalSidebarComponent
import ru.kyamshanov.missionChat.presentation.components.SidebarComponent
import ru.kyamshanov.missionChat.presentation.models.ChatUiModel
import ru.kyamshanov.missionChat.presentation.models.TopicUiModel
import ru.kyamshanov.missionChat.presentation.models.toIdentifier
import ru.kyamshanov.missionChat.presentation.models.toUiID
import ru.kyamshanov.missionChat.utils.toUI

class DefaultSidebarComponent(
    componentContext: ComponentContext,
    private val onSelectedCallback: (chatId: Identifier, topicId: Identifier) -> Unit,
    private val onArchiveChat: (chatId: Identifier) -> Unit,
    private val onUnarchiveChat: (chatId: Identifier) -> Unit,
) : InternalSidebarComponent, ComponentContext by componentContext {

    private val _state = MutableStateFlow(SidebarComponent.State())
    override val state: StateFlow<SidebarComponent.State> = _state.asStateFlow()

    override fun onSelect(
        chat: ChatUiModel,
        topic: TopicUiModel
    ) {
        onSelectedCallback(chat.id.toIdentifier(), topic.id.toIdentifier())
        _state.update { it.copy(selectedChat = chat, selectedTopic = topic) }
    }


    override fun archiveChat(chat: ChatUiModel) {
        onArchiveChat(chat.id.toIdentifier())
        _state.update {
            it.copy(
                activeChats = it.activeChats - chat,
                archivedChats = it.archivedChats + chat,
            )
        }
    }

    override fun unarchiveChat(chat: ChatUiModel) {
        onUnarchiveChat(chat.id.toIdentifier())
        _state.update {
            it.copy(
                activeChats = it.activeChats + chat,
                archivedChats = it.archivedChats - chat,
            )
        }
    }

    override fun deleteChat(chat: ChatUiModel) {
        TODO("Not yet implemented")
    }

    override fun updateChats(
        activeChats: Map<Chat, List<Topic>>,
        archivedChats: Map<Chat, List<Topic>>
    ) {
        _state.update {
            it.copy(
                activeChats = activeChats.toUI(),
                archivedChats = archivedChats.toUI(),
            )
        }
    }

    override fun addTopic(
        chat: Chat,
        topic: Topic
    ) {
        _state.update { value ->
            val chatIndex = value.activeChats.indexOfFirst { it.id == chat.id.toUiID() }
            val chat = value.activeChats[chatIndex]
            val updatedTopics = chat.topics + topic.toUI()
            val updatedChat = chat.copy(topics = updatedTopics)
            value.copy(
                archivedChats = value.activeChats.toMutableList().also { it[chatIndex] = updatedChat }
            )
        }
    }

    override fun selectTopic(
        chat: Chat,
        topic: Topic
    ) {
        _state.update { value ->
            val chat = value.activeChats.first { it.id == chat.id.toUiID() }
            val topic = chat.topics.first { it.id == topic.id.toUiID() }
            value.copy(selectedChat = chat, selectedTopic = topic)
        }
    }

}
