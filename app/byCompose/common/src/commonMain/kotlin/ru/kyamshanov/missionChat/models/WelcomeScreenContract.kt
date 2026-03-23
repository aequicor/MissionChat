package ru.kyamshanov.missionChat.models

import androidx.compose.runtime.Immutable
import ru.kyamshanov.missionChat.presentation.models.WelcomeState

@Immutable
data class WelcomeStateUI(
    val title: String
)

fun WelcomeState.toUI(): WelcomeStateUI =
    WelcomeStateUI(title = title)