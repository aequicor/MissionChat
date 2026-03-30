@file:Suppress("UnusedReceiverParameter")

package ru.kyamshanov.missionChat.presentation.components.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.dsl.updateStateImmediate
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import ru.kyamshanov.missionChat.domain.interactors.MessageProvider
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.Interlocutor
import ru.kyamshanov.missionChat.domain.models.MessageInference
import ru.kyamshanov.missionChat.domain.utils.now
import ru.kyamshanov.missionChat.presentation.components.ChatComponent
import ru.kyamshanov.missionChat.presentation.contracts.ChatContract.Action
import ru.kyamshanov.missionChat.presentation.contracts.ChatContract.Intent
import ru.kyamshanov.missionChat.presentation.contracts.ChatContract.InternalIntent
import ru.kyamshanov.missionChat.presentation.contracts.ChatContract.State
import ru.kyamshanov.missionChat.presentation.contracts.ChatInputContract
import ru.kyamshanov.missionChat.presentation.models.toIdentifier
import ru.kyamshanov.missionChat.utils.empty
import ru.kyamshanov.missionChat.utils.toTopics
import ru.kyamshanov.missionChat.utils.toUI

private typealias Ctx = PipelineContext<State, Intent, Action>
private typealias InputCtx = PipelineContext<ChatInputContract.State, ChatInputContract.Intent, ChatInputContract.Action>

internal class DefaultChatComponent(
    componentContext: ComponentContext,
    private val messageProvider: MessageProvider,
    private val messagesQueue: SharedFlow<String>,
    private val startNewTopic: SharedFlow<Unit>,
    private val onMessageSent: (Result<Unit>) -> Unit,
) : ChatComponent, ComponentContext by componentContext {

    private var exchangeJob: Job? = null

    override val store: Store<State, Intent, Action> = store(
        initial = State(topics = emptyList()),
        scope = coroutineScope()
    ) {
        configure {
            name = "ChatComponent"
        }

        init {
            launch { messageProvider.loadPreviousMessages() }
            launch {
                messageProvider.messages.collect {
                    updateState { copy(topics = it.toTopics()) }
                }
            }
            launch {
                messageProvider.currentTopic.collect {
                    updateState { copy(currentTopic = it?.toUI()) }
                }
            }
            launch {
                messagesQueue.collect {
                    onInsertMessage(it)
                }
            }
            launch {
                startNewTopic.collect {
                    // TODO: Handle start new topic
                }
            }
        }

        reduce { intent ->
            when (intent) {
                is Intent.DeleteMessage -> onDeleteMessage(intent)
                Intent.LoadNextMessages -> onLoadNextMessages()
                Intent.LoadPreviousMessages -> onLoadPreviousMessages()
                InternalIntent.StopGeneration -> handleStopGeneration()
            }
        }
    }

    override val inputStore: Store<ChatInputContract.State, ChatInputContract.Intent, ChatInputContract.Action> =
        store(
            initial = ChatInputContract.State(typingHint = "Введите сообщение..."),
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
                    is ChatInputContract.Intent.ChangeInputValue -> handleChangeInputValue(intent)
                    is ChatInputContract.Intent.ClickOnSendMessage -> handleSendMessage()
                    is ChatInputContract.Intent.StopGeneration -> handleInputStopGeneration()
                    ChatInputContract.InternalIntent.OnFinishGeneration -> handleInputFinishGeneration()
                    is ChatInputContract.Intent.ClickOnStartNewTopic -> handleStartNewTopic()
                }
            }
        }

    // --- Store Reducers ---
    private suspend fun Ctx.onDeleteMessage(intent: Intent.DeleteMessage) {
        messageProvider.deleteMessage(intent.messageId.toIdentifier())
    }

    private suspend fun Ctx.onLoadPreviousMessages() {
        messageProvider.loadPreviousMessages()
    }

    private suspend fun Ctx.onLoadNextMessages() {
        messageProvider.loadNextMessages()
    }

    private fun Ctx.handleStopGeneration() {
        exchangeJob?.cancel()
        inputStore.intent(ChatInputContract.InternalIntent.OnFinishGeneration)
    }

    // --- InputStore Reducers ---

    private fun InputCtx.handleChangeInputValue(
        intent: ChatInputContract.Intent.ChangeInputValue
    ) {
        updateStateImmediate { copy(inputValue = intent.newValue) }
    }

    private suspend fun InputCtx.handleSendMessage() {
        withState {
            if (isGenerating || inputValue.isBlank()) return@withState
            val message = inputValue
            updateStateImmediate { copy(inputValue = String.empty, isGenerating = true) }
            onInsertMessage(message)
        }
    }

    private fun InputCtx.handleInputStopGeneration() {
        exchangeJob?.cancel()
        updateStateImmediate { copy(isGenerating = false) }
    }

    private fun InputCtx.handleInputFinishGeneration() {
        updateStateImmediate { copy(isGenerating = false) }
    }

    private fun InputCtx.handleStartNewTopic() {
        // TODO: Handle start new topic
    }

    // --- Private Helpers ---

    private suspend fun onInsertMessage(message: String) {
        if (message.isBlank()) return
        exchangeJob?.cancel()

        exchangeJob = coroutineScope().launch {
            try {
                val humanMessage = MessageInference.HumanMessage(
                    id = Identifier.new(),
                    text = message,
                    createdAt = LocalDateTime.now(),
                    human = Interlocutor.Human(name = "User"),
                )
                messageProvider.sendMessage(humanMessage).collect { }
                onMessageSent(Result.success(Unit))
            } catch (e: Exception) {
                onMessageSent(Result.failure(e))
            } finally {
                inputStore.intent(ChatInputContract.InternalIntent.OnFinishGeneration)
            }
        }
    }
}
