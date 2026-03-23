package ru.kyamshanov.missionChat.utils

import com.arkivanov.decompose.ComponentContext
import ru.kyamshanov.missionChat.domain.models.Chat

/**
 * Data class to hold assisted parameters for [ru.kyamshanov.missionChat.WelcomeScreenComponent].
 * @property componentContext The Decompose component context.
 */
data class WelcomeScreenParams(
    val componentContext: ComponentContext
)

/**
 * Data class to hold assisted parameters for [ru.kyamshanov.missionChat.ChatInputComponent].
 * @property componentContext The Decompose component context.
 * @property onSendMessage Callback to be invoked when a message is sent.
 */
data class ChatInputParams(
    val componentContext: ComponentContext,
    val onSendMessage: (String) -> Unit,
    val onStopGeneration: () -> Unit,
)

/**
 * Data class to hold assisted parameters for [ru.kyamshanov.missionChat.MessagesComponent].
 * @property componentContext The Decompose component context.
 */
data class MessagesParams(
    val chat: Chat,
    val componentContext: ComponentContext
)
