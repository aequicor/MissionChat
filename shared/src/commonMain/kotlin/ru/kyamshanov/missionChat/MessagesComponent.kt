package ru.kyamshanov.missionChat

import com.arkivanov.decompose.ComponentContext
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.essenty.dsl.retainedStore
import ru.kyamshanov.missionChat.presentation.container.ChatContainer
import ru.kyamshanov.missionChat.presentation.models.MessagesAction
import ru.kyamshanov.missionChat.presentation.models.MessagesIntent
import ru.kyamshanov.missionChat.presentation.models.MessagesState

interface MessagesComponent :
    Store<MessagesState, MessagesIntent, MessagesAction>


internal class DefaultMessagesComponent(
    componentContext: ComponentContext,
    containerFactory: () -> ChatContainer,
) : MessagesComponent, ComponentContext by componentContext,
    Store<MessagesState, MessagesIntent, MessagesAction>
    by componentContext.retainedStore(factory = containerFactory)