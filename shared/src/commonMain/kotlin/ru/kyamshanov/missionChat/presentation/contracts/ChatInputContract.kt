package ru.kyamshanov.missionChat.presentation.contracts

import kotlinx.serialization.Serializable
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.utils.empty

class ChatInputContract {

    @Serializable
    data class State(
        val typingHint: String,
        val inputValue: String = String.empty,
        val isGenerating: Boolean = false,
    ) : MVIState

    sealed interface Intent : MVIIntent {

        data class ChangeInputValue(
            val newValue: String
        ) : Intent

        data object ClickOnSendMessage : Intent

        data object ClickOnStartNewTopic : Intent

        data object StopGeneration : Intent
    }

    internal sealed interface InternalIntent : Intent {


        data object OnFinishGeneration : InternalIntent

    }

    data object Action : MVIAction
}
