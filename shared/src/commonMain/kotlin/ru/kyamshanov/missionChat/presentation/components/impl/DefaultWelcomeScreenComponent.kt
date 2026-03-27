package ru.kyamshanov.missionChat.presentation.components.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.essenty.dsl.subscribe
import ru.kyamshanov.missionChat.domain.interactors.UserChatInteractor
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.presentation.components.ChatInputComponent
import ru.kyamshanov.missionChat.presentation.components.MessagesComponent
import ru.kyamshanov.missionChat.presentation.components.internal.InternalSidebarComponent
import ru.kyamshanov.missionChat.presentation.components.WelcomeScreenComponent
import ru.kyamshanov.missionChat.presentation.components.internal.ChatOrchestratorComponent
import ru.kyamshanov.missionChat.presentation.components.internal.selectedTopic
import ru.kyamshanov.missionChat.presentation.contracts.ChatOrchestratorContract
import ru.kyamshanov.missionChat.presentation.contracts.MessagesAction
import ru.kyamshanov.missionChat.presentation.contracts.MessagesIntent
import ru.kyamshanov.missionChat.utils.*
import kotlin.also

internal class DefaultWelcomeScreenComponent(
    componentContext: ComponentContext,
    private val componentFactory: ComponentFactory,
    private val userChatInteractor: UserChatInteractor,
) : WelcomeScreenComponent, ComponentContext by componentContext {

    private val coroutineScope = coroutineScope()

    private val chatNav = StackNavigation<ChatConfig>()
    private val currentChatComponent: MessagesComponent
        get() = chatContainer.value.active.instance.component

    private val chatOrchestratorComponent = componentFactory.createChatOrchestratorComponent(
        params = ChatOrchestratorParams(
            componentContext = childContext("chatOrchestrator"),
        )
    )

    init {
        chatOrchestratorComponent.store.subscribe { action ->
            when (action) {
                is ChatOrchestratorContract.Action.NavigateToTopic -> {
                    chatNav.replaceAll(
                        ChatConfig(
                            chatId = action.chat.id,
                            topicId = action.topic.id
                        )
                    )
                }
            }
        }
    }

    override val chatInputComponent: ChatInputComponent = componentFactory.createChatInputComponent(
        ChatInputParams(
            componentContext = childContext("chatInput"),
            onSendMessage = { message ->
                currentChatComponent.intent(MessagesIntent.SendNewMessage(message))
                true
            },
            onStartNewTopic = {
                chatOrchestratorComponent.store.intent(ChatOrchestratorContract.Intent.CreateNewTopic)
            },
            onStopGeneration = {
                currentChatComponent.intent(MessagesIntent.StopGeneration)
                true
            })
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
                    chatOrchestratorComponent.store.intent(
                        ChatOrchestratorContract.Intent.SelectTopic(
                            chatId = chatId,
                            topicId = topicId
                        )
                    )
                },
                onArchiveChat = { chatId ->
                    chatOrchestratorComponent.store.intent(
                        ChatOrchestratorContract.Intent.ArchiveChat(chatId)
                    )
                },
                onUnarchiveChat = { chatId ->
                    chatOrchestratorComponent.store.intent(
                        ChatOrchestratorContract.Intent.UnarchiveChat(chatId)
                    )
                }
            )
        )


    private fun chatChild(
        config: ChatConfig, componentContext: ComponentContext
    ): WelcomeScreenComponent.MessagesChat =
        chatOrchestratorComponent.selectedTopic.let {
            WelcomeScreenComponent.MessagesChat(
                component = componentFactory.createMessagesComponent(
                    MessagesParams(
                        chat = it?.first,
                        topic = it?.second,
                        componentContext = componentContext,
                        onChatCreated = {

                        },
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
        @Serializable(with = IdentifierSerializer::class) val chatId: Identifier?,
        @Serializable(with = IdentifierSerializer::class) val topicId: Identifier?,
    ) {

        init {
            require(!(chatId == null && topicId != null)) { "Topic cannot exist without a chat" }
        }
    }

}
