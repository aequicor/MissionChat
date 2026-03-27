package ru.kyamshanov.missionChat.welcomeScreen

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import ru.kyamshanov.missionChat.models.subscribeAsUiState
import ru.kyamshanov.missionChat.models.toUI
import ru.kyamshanov.missionChat.presentation.components.ChatInputComponent
import ru.kyamshanov.missionChat.presentation.contracts.ChatInputIntent


@Composable
fun InputSectionContent(
    component: ChatInputComponent,
    isGenerating: Boolean,
) {
    val state by component.subscribeAsUiState { it.toUI() }

    LaunchedEffect(isGenerating) {
        component.intent(ChatInputIntent.SetGenerating(isGenerating))
    }

    Row(
        Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            component.intent(ChatInputIntent.ClickOnStartNewTopic)
        }) {
            Icon(
                Icons.Default.Add,
                null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        TextField(
            value = state.inputValue,
            onValueChange = { component.intent(ChatInputIntent.ChangeInputValue(it)) },
            placeholder = {
                Text(
                    state.typingHint,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            },
            modifier = Modifier
                .weight(1f)
                .onPreviewKeyEvent {
                    if (it.key == Key.Enter && it.type == KeyEventType.KeyDown) {
                        if (it.isShiftPressed) {
                            false
                        } else {
                            if (isGenerating) {
                                component.intent(ChatInputIntent.StopGeneration)
                            } else if (state.inputValue.isNotBlank()) {
                                component.intent(ChatInputIntent.ClickOnSendMessage)
                            }
                            true
                        }
                    } else {
                        false
                    }
                },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.onSurface
            )
        )
        IconButton(
            onClick = {
                if (isGenerating) {
                    component.intent(ChatInputIntent.StopGeneration)
                } else if (state.inputValue.isNotBlank()) {
                    component.intent(ChatInputIntent.ClickOnSendMessage)
                }
            },
            enabled = state.inputValue.isNotBlank() || isGenerating
        ) {
            Icon(
                imageVector = if (isGenerating) Icons.Default.Stop else Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
