package ru.kyamshanov.missionChat.presentation.contracts

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.Topic

internal object ChatOrchestratorContract {

    data class State(
        val activeChats: Map<Chat, List<Topic>> = emptyMap(),
        val archivedChats: Map<Chat, List<Topic>> = emptyMap(),
        val selectedTopic: Pair<Chat, Topic>? = null,
    ) : MVIState

    sealed interface Intent : MVIIntent {

        data class SelectTopic(
            val chatId: Identifier,
            val topicId: Identifier,
        ) : Intent

        data class ArchiveChat(
            val chatId: Identifier,
        ) : Intent

        data class UnarchiveChat(
            val chatId: Identifier,
        ) : Intent

        data object CreateNewChat : Intent

        data object CreateNewTopic : Intent
    }

    sealed interface Action : MVIAction {

        data class NavigateToTopic(
            val chat: Chat,
            val topic: Topic,
        ) : Action

    }

}
