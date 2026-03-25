package ru.kyamshanov.missionChat.utils

import com.arkivanov.decompose.ComponentContext
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.Topic

/**
 * Data class to hold assisted parameters for [ru.kyamshanov.missionChat.presentation.components.WelcomeScreenComponent].
 * @property componentContext The Decompose component context.
 */
data class WelcomeScreenParams(
    val componentContext: ComponentContext
)

/**
 * Data class to hold assisted parameters for [ru.kyamshanov.missionChat.presentation.components.SidebarComponent].
 */
data class SidebarParams(
    val componentContext: ComponentContext,
    val onSelectedCallback: (chatId: Identifier, topicId: Identifier) -> Unit,
    val onArchiveChat: (chatId: Identifier) -> Unit,
    val onUnarchiveChat: (chatId: Identifier) -> Unit,
)

/**
 * Data class to hold assisted parameters for [ru.kyamshanov.missionChat.presentation.components.ChatInputComponent].
 * @property componentContext The Decompose component context.
 * @property onSendMessage Callback to be invoked when a message is sent.
 */
data class ChatInputParams(
    val componentContext: ComponentContext,
    val onSendMessage: (String) -> Unit,
    val onStopGeneration: () -> Unit,
)

/**
 * Data class to hold assisted parameters for [ru.kyamshanov.missionChat.presentation.components.MessagesComponent].
 * @property componentContext The Decompose component context.
 */
data class MessagesParams(
    val chat: Chat?,
    val topic: Topic?,
    val componentContext: ComponentContext,
    val onChatCreated: (Chat) -> Unit,
    val onTopicCreated: (Topic) -> Unit,
)
