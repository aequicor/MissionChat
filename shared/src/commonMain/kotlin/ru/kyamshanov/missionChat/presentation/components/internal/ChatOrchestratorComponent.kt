@file:OptIn(DelicateStoreApi::class)

package ru.kyamshanov.missionChat.presentation.components.internal

import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.state
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.presentation.contracts.ChatOrchestratorContract

internal typealias ChatOrchestratorStore = Store<ChatOrchestratorContract.State, ChatOrchestratorContract.Intent, ChatOrchestratorContract.Action>

internal interface ChatOrchestratorComponent {

    val store: ChatOrchestratorStore
}


internal val ChatOrchestratorComponent.selectedTopic: Pair<Chat, Topic>?
    get() = store.state.selectedTopic

internal fun Store<ChatOrchestratorContract.State, ChatOrchestratorContract.Intent, ChatOrchestratorContract.Action>.getTopicByID(
    chatId: Identifier,
    topicId: Identifier
): Pair<Chat, Topic> =
    state.activeChats.run {
        val chat = keys.first { it.id == chatId }
        chat to get(chat)!!.first { it.id == topicId }
    }