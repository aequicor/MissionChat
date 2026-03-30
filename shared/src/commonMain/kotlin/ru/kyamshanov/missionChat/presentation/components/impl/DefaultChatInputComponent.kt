package ru.kyamshanov.missionChat.presentation.components.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.dsl.updateStateImmediate
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import ru.kyamshanov.missionChat.presentation.contracts.ChatInputContract
import ru.kyamshanov.missionChat.presentation.contracts.ChatInputContract.Action
import ru.kyamshanov.missionChat.presentation.contracts.ChatInputContract.Intent
import ru.kyamshanov.missionChat.presentation.contracts.ChatInputContract.State
import ru.kyamshanov.missionChat.utils.empty

internal class DefaultChatInputComponent(
    componentContext: ComponentContext,
    private val onSendMessage: (String) -> Unit,
    private val onStopGeneration: () -> Boolean,
    private val onStartNewTopic: () -> Unit,
) : InternalChatInputComponent, ComponentContext by componentContext {

    override val store = store<State, Intent, Action>(
        initial = State("Welcome"),
        scope = coroutineScope()
    ) {
        configure {
            debuggable = true
            name = "ChatInputContainer"
        }

        recover {
            it.printStackTrace()
            null
        }
        reduce { intent ->
            when (intent) {
                is Intent.ChangeInputValue -> {
                    updateStateImmediate { copy(inputValue = intent.newValue) }
                }

                is Intent.ClickOnSendMessage -> {
                    withState {
                        if (isGenerating) return@withState
                        onSendMessage(inputValue)
                        updateStateImmediate { copy(inputValue = String.empty) }
                    }
                }

                is Intent.StopGeneration,
                ChatInputContract.InternalIntent.OnFinishGeneration -> {
                    withState {
                        if (!isGenerating) return@withState
                        val savedState = this
                        updateState { copy(isGenerating = false) }
                        if (!onStopGeneration()) updateStateImmediate { savedState }
                    }
                }

                is Intent.ClickOnStartNewTopic -> {
                    onStartNewTopic()
                }
            }
        }
    }

    override fun onInternalIntent(internalIntent: ChatInputContract.InternalIntent) {
        store.intent(internalIntent)
    }
}
