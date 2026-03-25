package ru.kyamshanov.missionChat.presentation.components.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pushToFront
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.essenty.dsl.retainedStore
import ru.kyamshanov.missionChat.domain.interactors.UserChatInteractor
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.presentation.components.ChatInputComponent
import ru.kyamshanov.missionChat.presentation.components.InternalSidebarComponent
import ru.kyamshanov.missionChat.presentation.components.SidebarComponent
import ru.kyamshanov.missionChat.presentation.components.WelcomeScreenComponent
import ru.kyamshanov.missionChat.presentation.contracts.MessagesIntent
import ru.kyamshanov.missionChat.presentation.contracts.WelcomeAction
import ru.kyamshanov.missionChat.presentation.contracts.WelcomeIntent
import ru.kyamshanov.missionChat.presentation.contracts.WelcomeState
import ru.kyamshanov.missionChat.utils.*
import kotlin.also

internal class DefaultWelcomeScreenComponent(
    componentContext: ComponentContext,
    private val componentFactory: ComponentFactory,
    private val userChatInteractor: UserChatInteractor,
) : WelcomeScreenComponent, ComponentContext by componentContext {

    private val coroutineScope = coroutineScope()
    private var activeChats = emptyMap<Chat, List<Topic>>()
    private var archivedChats = emptyMap<Chat, List<Topic>>()
    private var selectedChat: Chat? = null
    private var selectedTopic: Topic? = null

    private val chatNav = StackNavigation<ChatConfig>()

    override val chatInputComponent: ChatInputComponent =
        componentFactory.createChatInputComponent(
            ChatInputParams(
                componentContext = childContext("chatInput"),
                onSendMessage = { message ->
                    chatContainer.value.active.instance.component.intent(MessagesIntent.SendNewMessage(message))
                },
                onStopGeneration = {
                    chatContainer.value.active.instance.component.intent(MessagesIntent.StopGeneration)
                }
            )
        )


    override val chatContainer: Value<ChildStack<*, WelcomeScreenComponent.MessagesChat>> =
        childStack(
            key = "ChatContainerStack",
            source = chatNav,
            serializer = ChatConfig.serializer(),
            initialConfiguration = ChatConfig(null, null),
            handleBackButton = false,
            childFactory = ::chatChild,
        )


    override val sidebarComponent: InternalSidebarComponent =
        componentFactory.createSidebarComponent(
            SidebarParams(
                componentContext = childContext("sidebar"),
                onSelectedCallback = { chatId, topicId ->
                    val chat = activeChats.keys.first { it.id == chatId }
                    val topic = activeChats[chat]?.first { it.id == topicId }!!
                    selectTopic(chat, topic)
                },
                onArchiveChat = { chatId ->
                    val chat = activeChats.keys.first { it.id == chatId }
                    val topics = activeChats[chat].orEmpty()
                    activeChats -= chat
                    archivedChats = archivedChats.toMutableMap().apply { put(chat, topics) }
                    if (selectedChat == chat) {
                        activeChats.entries.firstOrNull().also { entry ->
                            if (entry == null) {
                                selectTopic(null, null)
                            } else {
                                val topic = entry.value.first()
                                selectTopic(entry.key, topic)
                            }
                        }
                    }
                },
                onUnarchiveChat = { chatId ->
                    val chat = archivedChats.keys.first { it.id == chatId }
                    val topics = archivedChats[chat].orEmpty()
                    archivedChats -= chat
                    archivedChats = activeChats.toMutableMap().apply { put(chat, topics) }
                }
            )
        )

    init {
        loadChatsAndTopics()
    }

    private fun loadChatsAndTopics() {
        coroutineScope.launch {
            try {
                val activeChats = userChatInteractor.getActiveChats()
                val activeChatsWithTopics = mutableMapOf<Chat, List<Topic>>()
                for (chat in activeChats) {
                    val topics = userChatInteractor.getTopics(chat.id)
                    activeChatsWithTopics[chat] = topics
                }
                val archivedChats = userChatInteractor.getArchivedChats()
                val archivedChatsWithTopics = mutableMapOf<Chat, List<Topic>>()
                for (chat in archivedChats) {
                    val topics = userChatInteractor.getTopics(chat.id)
                    archivedChatsWithTopics[chat] = topics
                }
                this@DefaultWelcomeScreenComponent.activeChats = activeChatsWithTopics
                this@DefaultWelcomeScreenComponent.archivedChats = archivedChatsWithTopics
                sidebarComponent.updateChats(activeChatsWithTopics, archivedChatsWithTopics)
                activeChatsWithTopics.entries.firstOrNull()?.also { (chat, topics) ->
                    topics.firstOrNull()?.also { topic ->
                        sidebarComponent.selectTopic(chat, topic)
                    }
                }
            } catch (e: Exception) {
                TODO()
            }
        }
    }

    private fun selectTopic(chat: Chat?, topic: Topic?) {
        if (chat == null) {
            require(topic == null)
            selectedChat = null
            selectedTopic = null
            chatNav.pushToFront(ChatConfig(null, null))
            return
        }
        requireNotNull(topic)
        require(activeChats.contains(chat))
        selectedChat = chat
        selectedTopic = topic
        chatNav.pushToFront(ChatConfig(chat.id, topic.id))
    }


    private fun chatChild(
        config: ChatConfig,
        componentContext: ComponentContext
    ): WelcomeScreenComponent.MessagesChat =
        if (config.chatId == null) {
            null to null
        } else {
            activeChats.firstNotNullOf {
                if (it.key.id == config.chatId) {
                    it.key to it.value.first { it.id == config.topicId }
                } else {
                    null
                }
            }
        }.let { (chat, topic) ->
            WelcomeScreenComponent.MessagesChat(
                component = componentFactory.createMessagesComponent(
                    MessagesParams(
                        chat = chat,
                        topic = topic,
                        componentContext = componentContext,
                        onChatCreated = { createChatOrTopic(it, it.headTopic) },
                        onTopicCreated = { createChatOrTopic(chat!!, it) }
                    )
                )
            )
        }

    private fun createChatOrTopic(chat: Chat, topic: Topic) {
        activeChats[chat]?.also { topics ->
            activeChats = activeChats.toMutableMap().apply { put(chat, topics + topic) }
        } ?: run {
            activeChats = activeChats.toMutableMap().apply { put(chat, listOf(topic)) }
        }
        sidebarComponent.updateChats(activeChats, archivedChats)
        sidebarComponent.selectTopic(chat, topic)
    }

    @Serializable
    private data class ChatConfig(
        @Serializable(with = IdentifierSerializer::class)
        val chatId: Identifier?,
        @Serializable(with = IdentifierSerializer::class)
        val topicId: Identifier?,
    ) {

        init {
            require(!(chatId == null && topicId != null)) { "Topic cannot exist without a chat" }
        }
    }

}
