package ru.kyamshanov.missionChat.di

import org.koin.dsl.module
import ru.kyamshanov.missionChat.presentation.components.MessagesComponent
import ru.kyamshanov.missionChat.presentation.components.WelcomeScreenComponent
import ru.kyamshanov.missionChat.presentation.components.impl.DefaultChatInputComponent
import ru.kyamshanov.missionChat.presentation.components.impl.DefaultChatOrchestratorComponent
import ru.kyamshanov.missionChat.presentation.components.impl.DefaultMessagesComponent
import ru.kyamshanov.missionChat.presentation.components.impl.DefaultSidebarComponent
import ru.kyamshanov.missionChat.presentation.components.impl.DefaultWelcomeScreenComponent
import ru.kyamshanov.missionChat.presentation.components.internal.ChatOrchestratorComponent
import ru.kyamshanov.missionChat.presentation.components.internal.InternalChatInputComponent
import ru.kyamshanov.missionChat.presentation.components.internal.InternalSidebarComponent
import ru.kyamshanov.missionChat.presentation.container.ChatContainer
import ru.kyamshanov.missionChat.presentation.factories.KoinRootComponentFactory
import ru.kyamshanov.missionChat.presentation.factories.RootComponentFactory
import ru.kyamshanov.missionChat.utils.ChatInputParams
import ru.kyamshanov.missionChat.utils.ChatOrchestratorParams
import ru.kyamshanov.missionChat.utils.ComponentFactory
import ru.kyamshanov.missionChat.utils.KoinComponentFactory
import ru.kyamshanov.missionChat.utils.MessagesParams
import ru.kyamshanov.missionChat.utils.SidebarParams
import ru.kyamshanov.missionChat.utils.WelcomeScreenParams

val sharedModule = module {
    includes(DomainDiModule)
    single<RootComponentFactory> { KoinRootComponentFactory() }
    single<ComponentFactory> { KoinComponentFactory() }

    factory<WelcomeScreenComponent> { (params: WelcomeScreenParams) ->
        DefaultWelcomeScreenComponent(
            componentContext = params.componentContext,
            componentFactory = get(),
            userChatInteractor = get()
        )
    }

    factory<InternalChatInputComponent> { (params: ChatInputParams) ->
        DefaultChatInputComponent(
            componentContext = params.componentContext,
            onSendMessage = params.onSendMessage,
            onStopGeneration = params.onStopGeneration,
            onStartNewTopic = params.onStartNewTopic,
        )
    }

    factory<ChatOrchestratorComponent> { (params: ChatOrchestratorParams) ->
        DefaultChatOrchestratorComponent(
            componentContext = params.componentContext,
            userChatInteractor = get()
        )
    }

    factory<MessagesComponent> { (params: MessagesParams) ->
        DefaultMessagesComponent(
            componentContext = params.componentContext,
            containerFactory = { ChatContainer(params.chat, params.topic, get()) },
            onChatCreated = params.onChatCreated,
        )
    }

    factory<InternalSidebarComponent> { (params: SidebarParams) ->
        DefaultSidebarComponent(
            componentContext = params.componentContext,
            onSelectedCallback = params.onSelectedCallback,
            onArchiveChat = params.onArchiveChat,
            onUnarchiveChat = params.onUnarchiveChat,
        )
    }
}