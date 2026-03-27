package ru.kyamshanov.missionChat.presentation.components.impl

import com.arkivanov.decompose.ComponentContext
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.essenty.dsl.retainedStore
import pro.respawn.flowmvi.essenty.dsl.subscribe
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.presentation.components.MessagesComponent
import ru.kyamshanov.missionChat.presentation.container.ChatContainer
import ru.kyamshanov.missionChat.presentation.contracts.MessagesAction
import ru.kyamshanov.missionChat.presentation.contracts.MessagesIntent
import ru.kyamshanov.missionChat.presentation.contracts.MessagesState

internal class DefaultMessagesComponent(
    componentContext: ComponentContext,
    containerFactory: () -> ChatContainer,
    onChatCreated: (Chat) -> Unit,
) : MessagesComponent, ComponentContext by componentContext,
    Store<MessagesState, MessagesIntent, MessagesAction>
    by componentContext.retainedStore(factory = containerFactory) {

    init {
        subscribe {
            actions.collect {
                when (it) {
                    is MessagesAction.ChatCreated -> onChatCreated(it.chat)
                }
            }
        }
    }

}