package ru.kyamshanov.missionChat.presentation.contracts

import kotlinx.serialization.Serializable
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

object DummyChatContract {

    @Serializable
    data object State : MVIState

    sealed interface Intent : MVIIntent

    sealed interface Action : MVIAction
}
