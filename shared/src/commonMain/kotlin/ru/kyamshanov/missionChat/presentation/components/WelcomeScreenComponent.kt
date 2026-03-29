package ru.kyamshanov.missionChat.presentation.components

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value

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
    val chatContainer: Value<ChildStack<*, ChatContainer>>

    /**
     * Component responsible for message input and sending.
     */
    val chatInputComponent: ChatInputComponent

    sealed interface ChatContainer {

        data class DummyChat(val component: DummyChatComponent) : ChatContainer

        data class Chat(val component: ChatComponent) : ChatContainer
    }
}
