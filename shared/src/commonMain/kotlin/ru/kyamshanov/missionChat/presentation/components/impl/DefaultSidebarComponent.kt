package ru.kyamshanov.missionChat.presentation.components.impl

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.presentation.components.SidebarComponent
import ru.kyamshanov.missionChat.presentation.components.internal.InternalSidebarComponent
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
    }

    override fun unarchiveChat(chat: ChatUiModel) {
        onUnarchiveChat(chat.id.toIdentifier())
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

    override fun selectChat(chat: Chat?) {
        _state.update { state ->
            if (chat == null) {
                state.copy(selectedChat = null, selectedTopic = null)
            } else {
                val chatUi = state.activeChats.find { it.id == chat.id.toUiID() }
                    ?: state.archivedChats.find { it.id == chat.id.toUiID() }

                val firstTopic = chatUi?.topics?.firstOrNull()
                if (chatUi != null && firstTopic != null) {
                    state.copy(selectedChat = chatUi, selectedTopic = firstTopic)
                } else {
                    state.copy(selectedChat = null, selectedTopic = null)
                }
            }
        }
    }

    override fun addTopic(topic: Pair<Chat, Topic>) {
        _state.update { value ->
            val (domainChat, domainTopic) = topic
            val activeIndex = value.activeChats.indexOfFirst { it.id == domainChat.id.toUiID() }
            if (activeIndex != -1) {
                val chat = value.activeChats[activeIndex]
                val updatedChat = chat.copy(topics = chat.topics + domainTopic.toUI())
                value.copy(
                    activeChats = value.activeChats.toMutableList()
                        .apply { set(activeIndex, updatedChat) }
                )
            } else {
                val archivedIndex =
                    value.archivedChats.indexOfFirst { it.id == domainChat.id.toUiID() }
                if (archivedIndex != -1) {
                    val chat = value.archivedChats[archivedIndex]
                    val updatedChat = chat.copy(topics = chat.topics + domainTopic.toUI())
                    value.copy(
                        archivedChats = value.archivedChats.toMutableList()
                            .apply { set(archivedIndex, updatedChat) }
                    )
                } else value
            }
        }
    }

    override fun selectTopic(topic: Pair<Chat, Topic>?) {
        _state.update { value ->
            if (topic == null) {
                value.copy(selectedChat = null, selectedTopic = null)
            } else {
                val (domainChat, domainTopic) = topic
                val chatUi = value.activeChats.find { it.id == domainChat.id.toUiID() }
                    ?: value.archivedChats.find { it.id == domainChat.id.toUiID() }

                val topicUi = chatUi?.topics?.find { it.id == domainTopic.id.toUiID() }

                if (chatUi != null && topicUi != null) {
                    value.copy(selectedChat = chatUi, selectedTopic = topicUi)
                } else {
                    value.copy(selectedChat = null, selectedTopic = null)
                }
            }
        }
    }

}
