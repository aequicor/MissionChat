package ru.kyamshanov.missionChat.presentation.components.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce
import ru.kyamshanov.missionChat.domain.interactors.UserChatInteractor
import ru.kyamshanov.missionChat.presentation.components.internal.ChatOrchestratorComponent
import ru.kyamshanov.missionChat.presentation.contracts.ChatOrchestratorContract.Action
import ru.kyamshanov.missionChat.presentation.contracts.ChatOrchestratorContract.Intent
import ru.kyamshanov.missionChat.presentation.contracts.ChatOrchestratorContract.State

private typealias Ctx = PipelineContext<State, Intent, Action>

internal class DefaultChatOrchestratorComponent(
    componentContext: ComponentContext,
    private val userChatInteractor: UserChatInteractor,
) : ChatOrchestratorComponent, ComponentContext by componentContext {

    private val scope = coroutineScope()
    override val store: Store<State, Intent, Action> = store(initial = State()) {

        configure {
            debuggable = true
            name = "ChatOrchestrator"
        }

        init {
            loadChatsAndTopics()
        }

        reduce { intent ->
            when (intent) {
                is Intent.ArchiveChat -> {
                    userChatInteractor.setArchivationChat(intent.chat, true)
                    loadChatsAndTopics()
                }

                Intent.CreateNewChat -> {
                    userChatInteractor.createChat(title = "New Chat", firstTopicTitle = "General")
                    loadChatsAndTopics()
                }

                Intent.CreateNewTopic -> {
                    withState {
                        selectedTopic?.first?.let { chat ->
                            userChatInteractor.createTopic(chat.id, title = "New Topic")
                        }
                    }
                    loadChatsAndTopics()
                }

                is Intent.SelectTopic -> {
                    updateState { copy(selectedTopic = intent.chat to intent.topic) }
                    action(Action.NavigateToTopic(intent.chat, intent.topic))
                }

                is Intent.UnarchiveChat -> {
                    userChatInteractor.setArchivationChat(intent.chat, false)
                    loadChatsAndTopics()
                }
            }
        }
    }

    init {
        store.start(scope)
    }

    private suspend fun Ctx.loadChatsAndTopics() {
        try {
            val activeChats = userChatInteractor.getActiveChats()
            val activeChatsWithTopics = activeChats.associateWith { userChatInteractor.getTopics(it.id) }

            val archivedChats = userChatInteractor.getArchivedChats()
            val archivedChatsWithTopics = archivedChats.associateWith { userChatInteractor.getTopics(it.id) }

            updateState {
                val newSelectedTopic = selectedTopic ?: activeChatsWithTopics.keys.firstOrNull()?.let { chat ->
                    activeChatsWithTopics[chat]?.firstOrNull()?.let { topic -> chat to topic }
                }

                newSelectedTopic?.let { (chat, topic) ->
                    action(Action.NavigateToTopic(chat, topic))
                }

                copy(
                    activeChats = activeChatsWithTopics,
                    archivedChats = archivedChatsWithTopics,
                    selectedTopic = newSelectedTopic
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
