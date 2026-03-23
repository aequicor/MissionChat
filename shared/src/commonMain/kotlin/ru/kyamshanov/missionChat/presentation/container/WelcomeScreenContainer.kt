package ru.kyamshanov.missionChat.presentation.container

import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.recover
import ru.kyamshanov.missionChat.presentation.models.WelcomeAction
import ru.kyamshanov.missionChat.presentation.models.WelcomeIntent
import ru.kyamshanov.missionChat.presentation.models.WelcomeState

//private typealias Ctx = PipelineContext<State, Intent, Action>

internal class WelcomeScreenContainer :
    Container<WelcomeState, WelcomeIntent, WelcomeAction> {

    override val store = store(initial = WelcomeState("Welcome to chat")) {

        recover {
            it.printStackTrace()
            null
        }
        configure {
            debuggable = true
            name = ""
        }
    }
}