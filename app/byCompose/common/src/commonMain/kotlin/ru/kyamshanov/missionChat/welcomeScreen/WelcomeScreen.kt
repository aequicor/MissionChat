package ru.kyamshanov.missionChat.welcomeScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import ru.kyamshanov.missionChat.AppTheme
import ru.kyamshanov.missionChat.GlassBackground
import ru.kyamshanov.missionChat.WelcomeScreenComponent
import ru.kyamshanov.missionChat.models.subscribeAsUiState
import ru.kyamshanov.missionChat.models.toUI

@Composable
fun WelcomeScreen(
    component: WelcomeScreenComponent,
    modifier: Modifier = Modifier
) {
    AppTheme {
        GlassBackground {
            val modelState by component.subscribeAsUiState { it.toUI() }

            WelcomeChat(
                title = modelState.title,
                messagesComponent = component.messagesComponent,
                chatInputComponent = component.chatInputComponent,
                modifier = modifier
            )
        }
    }
}
