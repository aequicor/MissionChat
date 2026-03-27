package ru.kyamshanov.missionChat.presentation.components.internal

import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.presentation.components.SidebarComponent

internal interface InternalSidebarComponent : SidebarComponent {

    fun updateChats(
        activeChats: Map<Chat, List<Topic>>,
        archivedChats: Map<Chat, List<Topic>>,
    )

    fun addTopic(chat: Chat, topic: Topic)

    fun selectTopic(chat: Chat, topic: Topic)
}