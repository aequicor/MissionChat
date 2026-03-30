package ru.kyamshanov.missionChat.presentation.components

import pro.respawn.flowmvi.api.Store
import ru.kyamshanov.missionChat.presentation.contracts.ChatContract.Action
import ru.kyamshanov.missionChat.presentation.contracts.ChatContract.Intent
import ru.kyamshanov.missionChat.presentation.contracts.ChatContract.State
import ru.kyamshanov.missionChat.presentation.contracts.ChatInputContract

interface ChatComponent {

    val store: Store<State, Intent, Action>

    val inputStore: Store<ChatInputContract.State, ChatInputContract.Intent, ChatInputContract.Action>

}