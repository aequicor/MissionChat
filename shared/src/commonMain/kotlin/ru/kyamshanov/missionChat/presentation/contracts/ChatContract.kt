package ru.kyamshanov.missionChat.presentation.contracts

import kotlinx.serialization.Serializable
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import ru.kyamshanov.missionChat.presentation.models.ChatTopicModel
import ru.kyamshanov.missionChat.presentation.models.MessagePresentationModel
import ru.kyamshanov.missionChat.presentation.models.TopicUiModel

object ChatContract {

    @Serializable
    data class State(
        val topics: List<ChatTopicModel>,
        val currentTopic: TopicUiModel? = null,
    ) : MVIState

    sealed interface Intent : MVIIntent {

        data class DeleteMessage(val message: MessagePresentationModel) : Intent

        data object LoadPreviousMessages : Intent

        data object LoadNextMessages : Intent
    }

    internal sealed interface InternalIntent : Intent {

        data class InsertMessage(val msg: MessagePresentationModel) : InternalIntent
    }

    sealed interface Action : MVIAction
}
