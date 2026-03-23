package ru.kyamshanov.missionChat

import com.arkivanov.decompose.ComponentContext
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.essenty.dsl.subscribe
import ru.kyamshanov.missionChat.presentation.container.ChatInputContainer
import ru.kyamshanov.missionChat.presentation.models.ChatInputAction
import ru.kyamshanov.missionChat.presentation.models.ChatInputIntent
import ru.kyamshanov.missionChat.presentation.models.ChatInputState
import ru.kyamshanov.missionChat.utils.retainedPersistedStore

internal class DefaultChatInputComponent(
    componentContext: ComponentContext,
    containerFactory: (ChatInputState) -> ChatInputContainer,
    onSendMessage: (String) -> Unit,
    onStopGeneration: () -> Unit,
) : ChatInputComponent, ComponentContext by componentContext,
    Store<ChatInputState, ChatInputIntent, ChatInputAction>
    by componentContext.retainedPersistedStore(
        initial = ChatInputState("Welcome to chating"),
        persistentKey = "ChatInput",
        serializer = ChatInputState.serializer(),
        builder = containerFactory
    ) {
    init {
        subscribe {
            actions.collect {
                when (it) {
                    is ChatInputAction.SendMessage -> onSendMessage(it.text)
                    ChatInputAction.StopGeneration -> onStopGeneration()
                }
            }
        }
    }
}
