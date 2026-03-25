package ru.kyamshanov.missionChat.presentation.components.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pushToFront
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.essenty.dsl.retainedStore
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.presentation.components.ChatInputComponent
import ru.kyamshanov.missionChat.presentation.components.SidebarComponent
import ru.kyamshanov.missionChat.presentation.components.WelcomeScreenComponent
import ru.kyamshanov.missionChat.presentation.container.WelcomeScreenContainer
import ru.kyamshanov.missionChat.presentation.contracts.MessagesIntent
import ru.kyamshanov.missionChat.presentation.contracts.WelcomeAction
import ru.kyamshanov.missionChat.presentation.contracts.WelcomeIntent
import ru.kyamshanov.missionChat.presentation.contracts.WelcomeState
import ru.kyamshanov.missionChat.utils.*

internal class DefaultWelcomeScreenComponent(
    componentContext: ComponentContext,
    containerFactory: () -> WelcomeScreenContainer,
    private val componentFactory: ComponentFactory,
) : WelcomeScreenComponent, ComponentContext by componentContext,
    Store<WelcomeState, WelcomeIntent, WelcomeAction>
    by componentContext.retainedStore(factory = containerFactory) {

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

    override val sidebarComponent: SidebarComponent =
        componentFactory.createSidebarComponent(
            SidebarParams(
                componentContext = childContext("sidebar"),
                onSelected = { chat, topic ->
                    chatNav.pushToFront(ChatConfig(chat.id, topic.id))
                },
            )
        )


    private fun chatChild(
        config: ChatConfig,
        componentContext: ComponentContext
    ): WelcomeScreenComponent.MessagesChat =
        sidebarComponent.getChatWithTopicById(config.chatId, config.topicId)
            .let { (chat, topic) ->
                WelcomeScreenComponent.MessagesChat(
                    component = componentFactory.createMessagesComponent(
                        MessagesParams(
                            chat = chat,
                            topic = topic,
                            componentContext = componentContext,
                            onChatCreated = { sidebarComponent.addTopic(it, it.headTopic) },
                            onTopicCreated = { sidebarComponent.addTopic(chat!!, it) }
                        )
                    )
                )
            }

    private fun SidebarComponent.getChatWithTopicById(chatId: Identifier?, topicId: Identifier?): Pair<Chat?, Topic?> {
        if (chatId == null) return null to null
        val entry = state.value.chatsWithTopics.entries.find { it.key.id == chatId }
            ?: throw IllegalStateException("Chat not found")
        val topic =
            topicId?.let { tid -> entry.value.find { it.id == tid } ?: throw IllegalStateException("Topic not found") }
        return entry.key to topic
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
