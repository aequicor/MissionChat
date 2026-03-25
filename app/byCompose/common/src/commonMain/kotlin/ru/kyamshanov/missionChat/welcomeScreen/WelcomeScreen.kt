package ru.kyamshanov.missionChat.welcomeScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ru.kyamshanov.missionChat.AppTheme
import ru.kyamshanov.missionChat.GlassBackground
import ru.kyamshanov.missionChat.models.subscribeAsUiState
import ru.kyamshanov.missionChat.models.toUI
import ru.kyamshanov.missionChat.presentation.components.WelcomeScreenComponent

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
                messagesComponentProvider = {
                    component.chatContainer.subscribeAsState().value.active.instance.component
                },
                chatInputComponent = component.chatInputComponent,
                sidebarComponent = component.sidebarComponent,
                modifier = modifier
            )
        }
    }
}
