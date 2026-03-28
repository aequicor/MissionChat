package ru.kyamshanov.missionChat.presentation.components.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.store
import ru.kyamshanov.missionChat.presentation.components.DummyChatComponent
import ru.kyamshanov.missionChat.presentation.contracts.DummyChatContract.Action
import ru.kyamshanov.missionChat.presentation.contracts.DummyChatContract.Intent
import ru.kyamshanov.missionChat.presentation.contracts.DummyChatContract.State

internal class DefaultDummyChatComponent(
    componentContext: ComponentContext
) : DummyChatComponent, ComponentContext by componentContext {

    override val store: Store<State, Intent, Action> = store(
        initial = State,
        scope = coroutineScope()
    ) {
        configure {
            name = "DummyChat"
        }
    }
}
