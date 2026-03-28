@file:OptIn(ExperimentalUuidApi::class)

package ru.kyamshanov.missionChat.presentation.components.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.reduce
import ru.kyamshanov.missionChat.domain.interactors.UserChatInteractor
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.presentation.components.ChatComponent
import ru.kyamshanov.missionChat.presentation.contracts.ChatContract.Action
import ru.kyamshanov.missionChat.presentation.contracts.ChatContract.Intent
import ru.kyamshanov.missionChat.presentation.contracts.ChatContract.InternalIntent
import ru.kyamshanov.missionChat.presentation.contracts.ChatContract.State
import ru.kyamshanov.missionChat.presentation.models.ChatTopicModel
import ru.kyamshanov.missionChat.presentation.models.TopicUiModel
import ru.kyamshanov.missionChat.presentation.models.toUiID
import ru.kyamshanov.missionChat.utils.toTopics
import kotlin.uuid.ExperimentalUuidApi

private typealias Ctx = PipelineContext<State, Intent, Action>

internal class DefaultChatComponent(
    componentContext: ComponentContext,
    private val userChatInteractor: UserChatInteractor,
    private val chat: Chat,
    private val openedTopic: Topic,
) : ChatComponent, ComponentContext by componentContext {

    override val store: Store<State, Intent, Action> = store(
        initial = State(
            topics = listOf(
                ChatTopicModel(
                    topic = TopicUiModel(
                        id = openedTopic.id.toUiID(),
                        title = openedTopic.title
                    ),
                    messages = emptyList()
                )
            ),
            currentTopic = TopicUiModel(
                id = openedTopic.id.toUiID(),
                title = openedTopic.title
            )
        ),
        scope = coroutineScope()
    ) {
        configure {
            name = "ChatComponent"
        }

        reduce { intent ->
            when (intent) {
                is Intent.DeleteMessage -> onDeleteMessage(intent)
                Intent.LoadNextMessages -> onLoadNextMessages()
                Intent.LoadPreviousMessages -> onLoadPreviousMessages()
                is InternalIntent.InsertMessage -> onInsertMessage(intent)
            }
        }
    }

    private suspend fun Ctx.onDeleteMessage(intent: Intent.DeleteMessage) {
        userChatInteractor.deleteMessage(Identifier.fromString(intent.message.id))
        updateState {
            copy(topics = topics.map { topicModel ->
                topicModel.copy(messages = topicModel.messages.filter { it.id != intent.message.id })
            })
        }
    }

    private suspend fun Ctx.onLoadPreviousMessages() {
        val topicId = openedTopic.id
        val messagesMap = userChatInteractor.getMessages(topicId = topicId)
        val newTopics = messagesMap.toTopics()
        updateState {
            copy(topics = newTopics)
        }
    }

    private suspend fun Ctx.onLoadNextMessages() {
        // TODO: Implement next messages loading
    }

    private suspend fun Ctx.onInsertMessage(intent: InternalIntent.InsertMessage) {
        updateState {
            val updatedTopics = topics.map { topicModel ->
                if (topicModel.topic.id == currentTopic?.id) {
                    topicModel.copy(messages = topicModel.messages + intent.msg)
                } else {
                    topicModel
                }
            }
            copy(topics = updatedTopics)
        }
    }
}
