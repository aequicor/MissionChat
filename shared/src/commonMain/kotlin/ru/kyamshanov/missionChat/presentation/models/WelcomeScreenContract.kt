package ru.kyamshanov.missionChat.presentation.models

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

data class WelcomeState(
    val title: String
) : MVIState


sealed interface WelcomeIntent : MVIIntent

sealed interface WelcomeAction : MVIAction