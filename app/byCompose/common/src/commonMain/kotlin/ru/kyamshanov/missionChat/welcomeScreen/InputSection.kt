package ru.kyamshanov.missionChat.welcomeScreen

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import ru.kyamshanov.missionChat.models.subscribeAsUiState
import ru.kyamshanov.missionChat.models.toUI
import ru.kyamshanov.missionChat.presentation.contracts.ChatInputContract.Intent


@Composable
fun InputSectionContent(
    component: ChatInputComponent,
) {
    val state by component.store.subscribeAsUiState { it.toUI() }
    val isGenerating = state.isGenerating
    Row(
        Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            component.store.intent(Intent.ClickOnStartNewTopic)
        }) {
            Icon(
                Icons.Default.Add,
                null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        TextField(
            value = state.inputValue,
            onValueChange = { component.store.intent(Intent.ChangeInputValue(it)) },
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
                            if (state.isGenerating) {
                                component.store.intent(Intent.StopGeneration)
                            } else if (state.inputValue.isNotBlank()) {
                                component.store.intent(Intent.ClickOnSendMessage)
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
                    component.store.intent(Intent.StopGeneration)
                } else if (state.inputValue.isNotBlank()) {
                    component.store.intent(Intent.ClickOnSendMessage)
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
