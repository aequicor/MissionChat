package ru.kyamshanov.missionChat.presentation.container

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import ru.kyamshanov.missionChat.domain.interactors.UserChatInteractor
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.Interlocutor
import ru.kyamshanov.missionChat.domain.models.MessageInference
import ru.kyamshanov.missionChat.domain.utils.now
import ru.kyamshanov.missionChat.presentation.models.MessagesAction
import ru.kyamshanov.missionChat.presentation.models.MessagesIntent
import ru.kyamshanov.missionChat.presentation.models.MessagesState
import ru.kyamshanov.missionChat.presentation.models.toPresentation

private typealias Ctx = PipelineContext<MessagesState, MessagesIntent, MessagesAction>

internal class ChatContainer(
    chat: Chat,
    private val userChatInteractor: UserChatInteractor
) : Container<MessagesState, MessagesIntent, MessagesAction> {

    private var currentTopicId: Identifier? = chat.headTopic?.id
    private var generationJob: Job? = null
    private var messages: List<MessageInference> = emptyList()
    private val humanInterlocutor = Interlocutor.Human(name = "User")

    override val store = store(initial = MessagesState.Idle) {
        configure {
            debuggable = true
            name = "MessagesContainer"
        }

        init {
            loadMessages()
        }

        recover {
            updateState { MessagesState.Error(it) }
            null
        }

        reduce { intent ->
            when (intent) {
                is MessagesIntent.DeleteMessage -> deleteMessage(intent.id)
                MessagesIntent.LoadNextMessages -> loadMessages()
                is MessagesIntent.SendNewMessage -> sendMessage(intent.message)
                MessagesIntent.StopGeneration -> stopGeneration()
            }
        }
    }

    private suspend fun Ctx.loadMessages() {
        try {
            val topicId = currentTopicId
            if (topicId == null) {
                updateState { MessagesState.Loaded(emptyList(), false) }
                return
            }

            val newMessages = userChatInteractor.getMessages(
                topicId = topicId,
                limit = 50,
                before = messages.firstOrNull()?.createdAt ?: LocalDateTime.now()
            )
            messages = newMessages + messages
            updateState {
                MessagesState.Loaded(
                    messages = this@ChatContainer.messages.map { it.toPresentation() },
                    isGenerating = false
                )
            }
        } catch (e: Exception) {
            updateState { MessagesState.Error(e) }
        }
    }

    private suspend fun Ctx.deleteMessage(id: Identifier) {
        try {
            userChatInteractor.deleteMessage(id)
            messages = messages.filter { it.id != id }
            withState {
                if (this is MessagesState.Loaded) {
                    val messagesPres = this@ChatContainer.messages.map { it.toPresentation() }
                    updateState { this@withState.copy(messages = messagesPres) }
                }
            }
        } catch (e: Exception) {
            updateState { MessagesState.Error(e) }
        }
    }

    private suspend fun Ctx.sendMessage(text: String) {
        //TODO handle when generation job is not null

        val humanMessage = MessageInference.HumanMessage(
            id = Identifier.random(),
            text = text,
            createdAt = LocalDateTime.now(),
            human = humanInterlocutor,
        )
        messages = messages + humanMessage
        updateState {
            MessagesState.Loaded(
                messages = this@ChatContainer.messages.map { it.toPresentation() },
                isGenerating = true
            )
        }

        generationJob = launch {
            userChatInteractor.sendMessage(
                context = messages,
                message = humanMessage
            ).catch { e ->
                updateState { MessagesState.Error(Exception(e)) }
            }.onCompletion {
                withState {
                    if (this is MessagesState.Loaded) {
                        updateState { this@withState.copy(isGenerating = false) }
                    }
                }
                generationJob = null
            }.collect { incomingMessage ->
                val existingIndex = messages.indexOfFirst { it.id == incomingMessage.id }
                if (existingIndex != -1) {
                    messages = messages.toMutableList().apply { set(existingIndex, incomingMessage) }
                } else {
                    messages += incomingMessage
                }

                withState {
                    if (this is MessagesState.Loaded) {
                        val messagesPres = this@ChatContainer.messages.map { it.toPresentation() }
                        updateState { this@withState.copy(messages = messagesPres) }
                    }
                }
            }
        }
    }

    private suspend fun Ctx.stopGeneration() {
        generationJob?.cancel()
        generationJob = null
        withState {
            if (this is MessagesState.Loaded) {
                updateState { this@withState.copy(isGenerating = false) }
            }
        }
    }
}
