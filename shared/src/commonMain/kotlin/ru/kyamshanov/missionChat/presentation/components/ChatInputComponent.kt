package ru.kyamshanov.missionChat.presentation.components

import pro.respawn.flowmvi.api.Store
import ru.kyamshanov.missionChat.presentation.contracts.ChatInputContract

interface ChatInputComponent {
    val store: Store<ChatInputContract.State, ChatInputContract.Intent, ChatInputContract.Action>
}