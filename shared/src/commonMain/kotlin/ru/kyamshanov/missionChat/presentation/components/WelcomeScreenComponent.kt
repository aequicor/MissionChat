package ru.kyamshanov.missionChat.presentation.components

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import pro.respawn.flowmvi.api.Store
import ru.kyamshanov.missionChat.presentation.contracts.WelcomeAction
import ru.kyamshanov.missionChat.presentation.contracts.WelcomeIntent
import ru.kyamshanov.missionChat.presentation.contracts.WelcomeState

/**
 * Component for the Welcome Screen, managing the main chat interface.
 */
interface WelcomeScreenComponent {

    /**
     * Component for managing chats and topics sidebar.
     */
    val sidebarComponent: SidebarComponent

    /**
     * Navigation stack for the chat message area.
     */
    val chatContainer: Value<ChildStack<*, MessagesChat>>

    /**
     * Component responsible for message input and sending.
     */
    val chatInputComponent: ChatInputComponent

    /**
     * Wrapper class for the [MessagesComponent] within the navigation stack.
     */
    class MessagesChat(val component: MessagesComponent)
}
