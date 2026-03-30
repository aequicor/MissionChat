package ru.kyamshanov.missionChat.presentation.components.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.kyamshanov.missionChat.domain.interactors.ChatOrchestrator
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.presentation.components.ChatInputComponent
import ru.kyamshanov.missionChat.presentation.components.WelcomeScreenComponent
import ru.kyamshanov.missionChat.presentation.components.WelcomeScreenComponent.ChatContainer
import ru.kyamshanov.missionChat.presentation.components.internal.InternalSidebarComponent
import ru.kyamshanov.missionChat.presentation.contracts.ChatContract
import ru.kyamshanov.missionChat.presentation.contracts.ChatInputContract.InternalIntent
import ru.kyamshanov.missionChat.presentation.models.WelcomeScreenEvent
import ru.kyamshanov.missionChat.utils.ChatInputParams
import ru.kyamshanov.missionChat.utils.ComponentFactory
import ru.kyamshanov.missionChat.utils.IdentifierSerializer
import ru.kyamshanov.missionChat.utils.SidebarParams

internal class DefaultWelcomeScreenComponent(
    componentContext: ComponentContext,
    componentFactory: ComponentFactory,
    private val chatOrchestrator: ChatOrchestrator,
) : WelcomeScreenComponent, ComponentContext by componentContext {

    private val coroutineScope = coroutineScope()
    private val eventBus = MutableSharedFlow<WelcomeScreenEvent>()

    override val sidebarComponent: InternalSidebarComponent =
        componentFactory.createSidebarComponent(
            SidebarParams(
                componentContext = childContext("sidebar"),
                onSelectedCallback = { chatId, topicId ->
                    coroutineScope.launch {
                        chatNav.replaceAll(ChatConfig.Selected(chatId, topicId))
                    }
                },
                onArchiveChat = { chatId ->
                    coroutineScope.launch {
                        chatOrchestrator.archiveChat(chatId)
                    }
                },
                onUnarchiveChat = { chatId ->
                    coroutineScope.launch {
                        chatOrchestrator.unarchiveChat(chatId)
                    }
                }
            )
        )

    private val chatNav = StackNavigation<ChatConfig>()

    override val chatContainer: Value<ChildStack<*, ChatContainer>> =
        childStack(
            source = chatNav,
            serializer = ChatConfig.serializer(),
            initialConfiguration = ChatConfig.None,
            handleBackButton = false,
            childFactory = ::createChatChild,
        )

    private fun createChatChild(
        config: ChatConfig,
        componentContext: ComponentContext
    ): ChatContainer = when (config) {
        ChatConfig.None -> ChatContainer.DummyChat(
            component = DefaultDummyChatComponent(componentContext)
        )

        is ChatConfig.Selected -> {
            val messageProvider = chatOrchestrator.getMessageProvider(config.chatId, config.topicId)
            ChatContainer.Chat(
                component = DefaultChatComponent(
                    componentContext = componentContext,
                    messageProvider = messageProvider,
                    eventBus = eventBus.asSharedFlow(),
                    onMessageSent = {
                        chatInputComponent.store.intent(InternalIntent.OnFinishGeneration)
                    },
                )
            )

        }
    }

    init {
        with(chatOrchestrator) {
            combine(activeChats, archivedChats) { active, archive ->
                val activeMap = active.items.associate { it.chat to it.firstTopics }
                val archiveMap = archive.items.associate { it.chat to it.firstTopics }
                sidebarComponent.updateChats(activeMap, archiveMap)
            }.launchIn(coroutineScope)

            coroutineScope.launch { loadNextArchiveChat() }
            coroutineScope.launch { loadNextActiveChat() }
        }
    }

    override val chatInputComponent: ChatInputComponent = componentFactory.createChatInputComponent(
        ChatInputParams(
            componentContext = childContext("chatInput"),
            onSendMessage = { message ->
                coroutineScope.launch {
                    (chatContainer.value.active.instance as? ChatContainer.DummyChat)?.also {
                        chatOrchestrator.startNewChat()
                    }
                    eventBus.emit(WelcomeScreenEvent.SendMessage(message))
                }
            },
            onStopGeneration = {
                val activeInstance = chatContainer.value.active.instance
                if (activeInstance is ChatContainer.Chat) {
                    activeInstance.component.store.intent(ChatContract.InternalIntent.StopGeneration)
                    true
                } else {
                    false
                }
            },
            onStartNewTopic = {
                coroutineScope.launch {
                    eventBus.emit(WelcomeScreenEvent.StartNewTopic)
                }
            })
    )

    @Serializable
    private sealed interface ChatConfig {
        @Serializable
        data object None : ChatConfig

        @Serializable
        data class Selected(
            @Serializable(with = IdentifierSerializer::class) val chatId: Identifier,
            @Serializable(with = IdentifierSerializer::class) val topicId: Identifier,
        ) : ChatConfig
    }
}
