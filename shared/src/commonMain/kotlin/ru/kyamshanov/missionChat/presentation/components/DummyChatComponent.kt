package ru.kyamshanov.missionChat.presentation.components

import pro.respawn.flowmvi.api.Store
import ru.kyamshanov.missionChat.presentation.contracts.DummyChatContract.Action
import ru.kyamshanov.missionChat.presentation.contracts.DummyChatContract.Intent
import ru.kyamshanov.missionChat.presentation.contracts.DummyChatContract.State

interface DummyChatComponent {

    val store: Store<State, Intent, Action>
}