package ru.kyamshanov.missionChat.presentation.components.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce
import ru.kyamshanov.missionChat.domain.interactors.UserChatInteractor
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.presentation.components.internal.ChatOrchestratorComponent
import ru.kyamshanov.missionChat.presentation.contracts.ChatOrchestratorContract.Action
import ru.kyamshanov.missionChat.presentation.contracts.ChatOrchestratorContract.Intent
import ru.kyamshanov.missionChat.presentation.contracts.ChatOrchestratorContract.State

private typealias Ctx = PipelineContext<State, Intent, Action>

internal class DefaultChatOrchestratorComponent(
    componentContext: ComponentContext,
    private val userChatInteractor: UserChatInteractor,
) : ChatOrchestratorComponent, ComponentContext by componentContext {

    override val store: Store<State, Intent, Action> = store(
        initial = State(),
        scope = coroutineScope()
    ) {
        configure {
            debuggable = true
            name = "ChatOrchestrator"
        }

        init {
            loadChatsAndTopics()
        }

        reduce { intent ->
            when (intent) {
                is Intent.ArchiveChat -> onArchiveChat(intent)
                Intent.CreateNewChat -> onCreateNewChat()
                Intent.CreateNewTopic -> onCreateNewTopic()
                is Intent.SelectTopic -> onSelectTopic(intent)
                is Intent.UnarchiveChat -> onUnarchiveChat(intent)
            }
        }
    }

    private suspend fun Ctx.onArchiveChat(intent: Intent.ArchiveChat) {
        var chat: Chat? = null
        withState { chat = activeChats.keys.find { it.id == intent.chatId } }
        if (chat != null) {
            userChatInteractor.setArchivationChat(chat!!, true)
            loadChatsAndTopics()
        }
    }

    private suspend fun Ctx.onCreateNewChat() {
        userChatInteractor.createChat(
            title = "New Chat",
            firstTopicTitle = "General"
        )
        loadChatsAndTopics()
    }

    private suspend fun Ctx.onCreateNewTopic() {
        var chat: Chat? = null
        withState { chat = selectedTopic?.first }

        if (chat != null) {
            userChatInteractor.createTopic(chat!!.id, title = "New Topic")
            loadChatsAndTopics()
        }
    }

    private suspend fun Ctx.onSelectTopic(intent: Intent.SelectTopic) {
        var pair: Pair<Chat, Topic>? = null
        withState {
            val chat =
                (activeChats.keys + archivedChats.keys).find { it.id == intent.chatId }
            val topic = (activeChats[chat]
                ?: archivedChats[chat])?.find { it.id == intent.topicId }
            pair = if (chat != null && topic != null) chat to topic else null
        }
        if (pair != null) {
            updateState { copy(selectedTopic = pair) }
            action(Action.NavigateToTopic(pair!!.first, pair!!.second))
        }
    }

    private suspend fun Ctx.onUnarchiveChat(intent: Intent.UnarchiveChat) {
        var chat: Chat? = null
        withState { chat = archivedChats.keys.find { it.id == intent.chatId } }
        if (chat != null) {
            userChatInteractor.setArchivationChat(chat!!, false)
            loadChatsAndTopics()
        }
    }

    private suspend fun Ctx.loadChatsAndTopics() {
        try {
            val activeChats = userChatInteractor.getActiveChats()
            val activeChatsWithTopics =
                activeChats.associateWith { userChatInteractor.getTopics(it.id) }

            val archivedChats = userChatInteractor.getArchivedChats()
            val archivedChatsWithTopics =
                archivedChats.associateWith { userChatInteractor.getTopics(it.id) }

            var oldSelectedTopic: Pair<Chat, Topic>? = null
            withState { oldSelectedTopic = selectedTopic }

            val refreshedChat = (activeChatsWithTopics.keys + archivedChatsWithTopics.keys)
                .find { it.id == oldSelectedTopic?.first?.id }
            val refreshedTopic =
                (activeChatsWithTopics[refreshedChat] ?: archivedChatsWithTopics[refreshedChat])
                    ?.find { it.id == oldSelectedTopic?.second?.id }

            val newSelectedTopic = if (refreshedChat != null && refreshedTopic != null) {
                refreshedChat to refreshedTopic
            } else {
                activeChatsWithTopics.keys.firstOrNull()?.let { chat ->
                    activeChatsWithTopics[chat]?.firstOrNull()?.let { topic -> chat to topic }
                }
            }

            updateState {
                copy(
                    activeChats = activeChatsWithTopics,
                    archivedChats = archivedChatsWithTopics,
                    selectedTopic = newSelectedTopic
                )
            }

            if (newSelectedTopic != null &&
                (oldSelectedTopic == null ||
                        newSelectedTopic.first.id != oldSelectedTopic?.first?.id ||
                        newSelectedTopic.second.id != oldSelectedTopic?.second?.id)
            ) {
                action(Action.NavigateToTopic(newSelectedTopic.first, newSelectedTopic.second))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
