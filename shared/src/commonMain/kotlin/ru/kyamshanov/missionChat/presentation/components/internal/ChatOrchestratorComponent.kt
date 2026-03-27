package ru.kyamshanov.missionChat.presentation.components.internal

import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.state
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.presentation.contracts.ChatOrchestratorContract

internal interface ChatOrchestratorComponent {

    val store: Store<ChatOrchestratorContract.State, ChatOrchestratorContract.Intent, ChatOrchestratorContract.Action>
}


@OptIn(DelicateStoreApi::class)
internal val ChatOrchestratorComponent.selectedTopic: Pair<Chat, Topic>?
    get() = store.state.selectedTopic