package ru.kyamshanov.missionChat.presentation.components.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
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
import ru.kyamshanov.missionChat.presentation.models.WelcomeScreenEvent
import ru.kyamshanov.missionChat.presentation.models.toIdentifier
import ru.kyamshanov.missionChat.utils.toTopics
import ru.kyamshanov.missionChat.utils.toUI

internal class DefaultChatComponent(
    componentContext: ComponentContext,
    private val messageProvider: MessageProvider,
    private val eventBus: Flow<WelcomeScreenEvent>,
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
            launch {
                messageProvider.initMessages()
                eventBus.collect {
                    when (it) {
                        is WelcomeScreenEvent.SendMessage -> onInsertMessage(it.msg)
                        WelcomeScreenEvent.StartNewTopic -> onStartNewTopic()
                    }
                }
            }
            launch {
                messageProvider.messages.collect { paginationState ->
                    updateState {
                        copy(
                            topics = paginationState.items
                                .associate { it.topic to it.messages }
                                .toTopics()
                        )
                    }
                }
            }
            launch {
                messageProvider.currentTopic.collect {
                    updateState { copy(currentTopic = it.toUI()) }
                }
            }
        }

        reduce { intent ->
            when (intent) {
                is Intent.DeleteMessage -> onDeleteMessage(intent)
                Intent.LoadNextMessages -> onLoadNextMessages()
                Intent.LoadPreviousMessages -> onLoadPreviousMessages()
                InternalIntent.StopGeneration -> exchangeJob?.cancel()
            }
        }
    }

    private suspend fun onDeleteMessage(intent: Intent.DeleteMessage) {
        messageProvider.deleteMessage(
            topicId = intent.topicId.toIdentifier(),
            messageId = intent.messageId.toIdentifier()
        )
    }

    private suspend fun onLoadPreviousMessages() {
        messageProvider.loadPreviousMessages()
    }

    private suspend fun onLoadNextMessages() {
        messageProvider.loadNextMessages()
    }

    private suspend fun onInsertMessage(message: String) {
        kotlinx.coroutines.coroutineScope {
            exchangeJob = launch {
                try {
                    val humanMessage = MessageInference.HumanMessage(
                        id = Identifier.new(),
                        text = message,
                        createdAt = LocalDateTime.now(),
                        human = Interlocutor.Human(name = "User"),
                    )
                    messageProvider.sendMessage(humanMessage)
                    onMessageSent(Result.success(Unit))
                } catch (e: Exception) {
                    onMessageSent(Result.failure(e))
                }
            }
        }
    }

    private suspend fun onStartNewTopic() {
        messageProvider.startNewTopic()
    }
}
