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
import ru.kyamshanov.missionChat.domain.models.*
import ru.kyamshanov.missionChat.domain.utils.mix
import ru.kyamshanov.missionChat.domain.utils.now
import ru.kyamshanov.missionChat.presentation.contracts.MessagesAction
import ru.kyamshanov.missionChat.presentation.contracts.MessagesIntent
import ru.kyamshanov.missionChat.presentation.contracts.MessagesState
import ru.kyamshanov.missionChat.presentation.models.toIdentifier
import ru.kyamshanov.missionChat.utils.add
import ru.kyamshanov.missionChat.utils.set
import ru.kyamshanov.missionChat.utils.toTopics

private typealias Ctx = PipelineContext<MessagesState, MessagesIntent, MessagesAction>

internal class ChatContainer(
    chatId: Identifier?,
) : Container<MessagesState, MessagesIntent, MessagesAction> {

    private var currentChatId = headTopic?.first?.id
    private var headTopicId: Identifier? = headTopic?.second?.id ?: headTopic?.first?.headTopic?.id
    private var generationJob: Job? = null
    private var messages: Map<Topic, List<MessageInference>> = LinkedHashMap()
    private val humanInterlocutor = Interlocutor.Human(name = "User")

    override val store = store(initial = MessagesState.Idle) {
        configure {
            debuggable = true
            name = "MessagesContainer"
        }

        init {
            if (currentChatId == null) {
                updateState { MessagesState.Loaded(emptyList()) }
            } else {
                loadMessages()
            }
        }

        recover {
            updateState { MessagesState.Error(it) }
            null
        }

        reduce { intent ->
            when (intent) {
                is MessagesIntent.DeleteMessage -> {
                    deleteMessage(
                        topicId = intent.topicId.toIdentifier(),
                        messageId = intent.messageId.toIdentifier()
                    )
                }

                MessagesIntent.LoadNextMessages -> loadMessages()
                is MessagesIntent.SendNewMessage -> sendMessage(intent.message)
                MessagesIntent.StopGeneration -> stopGeneration()
            }
        }
    }

    private suspend fun Ctx.loadMessages() {
        try {
            val topicId = headTopicId
            if (topicId == null) {
                updateState { MessagesState.Loaded(emptyList(), false) }
                return
            }

            val newMessages = userChatInteractor.getMessages(
                topicId = topicId,
                limit = 50,
                before = messages.entries.lastOrNull()?.value?.lastOrNull()?.createdAt
                    ?: LocalDateTime.now()
            )
            messages = messages.mix(newMessages)
            syncState()
        } catch (e: Exception) {
            updateState { MessagesState.Error(e) }
        }
    }

    private suspend fun Ctx.deleteMessage(topicId: Identifier, messageId: Identifier) {
        try {
            userChatInteractor.deleteMessage(messageId)
            val topic = messages.keys.first { it.id == topicId }
            messages[topic]?.toMutableList()?.filter { it.id != messageId }?.also {
                messages = messages.toMutableMap().apply { set(topic, it) }
            }
            withState {
                if (this is MessagesState.Loaded) {
                    syncState()
                }
            }
        } catch (e: Exception) {
            updateState { MessagesState.Error(e) }
        }
    }

    private suspend fun Ctx.sendMessage(text: String) {
        if (generationJob != null) return
        if (currentChatId == null) {
            createNewChat()
        }
        val topic = headTopicId.let { id -> messages.keys.first { it.id == id } }
        val messagesContext = messages[topic].orEmpty()
        val humanMessage = MessageInference.HumanMessage(
            id = Identifier.new(),
            text = text,
            createdAt = LocalDateTime.now(),
            human = humanInterlocutor,
        )
        messages = messages.add(topic, humanMessage)
        syncState(isGenerating = true)

        generationJob = launch {
            userChatInteractor.sendMessage(
                topic = topic,
                context = messagesContext,
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
                val existingIndex =
                    messages[topic].orEmpty().indexOfFirst { it.id == incomingMessage.id }
                messages = if (existingIndex != -1) {
                    messages.set(topic, existingIndex, incomingMessage)
                } else {
                    messages.add(topic, incomingMessage)
                }

                withState {
                    if (this is MessagesState.Loaded) {
                        syncState(isGenerating = true)
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

    private suspend fun Ctx.createNewChat() {
        try {
            val chat = userChatInteractor.createChat(title = "New Chat", null, "New topic")
            currentChatId = chat.id
            headTopicId = chat.headTopic.id
            messages = messages.toMutableMap().apply { put(chat.headTopic, emptyList()) }
            action(MessagesAction.ChatCreated(chat))
        } catch (e: Exception) {
            updateState { MessagesState.Error(e) }
        }
    }

    private suspend fun Ctx.syncState(isGenerating: Boolean = false) {
        updateState {
            val messagesPres = this@ChatContainer.messages.toTopics()
            MessagesState.Loaded(topics = messagesPres, isGenerating = isGenerating)
        }
    }
}
