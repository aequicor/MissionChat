package ru.kyamshanov.missionChat.di

import org.koin.dsl.module
import ru.kyamshanov.missionChat.presentation.components.ChatInputComponent
import ru.kyamshanov.missionChat.presentation.components.MessagesComponent
import ru.kyamshanov.missionChat.presentation.components.SidebarComponent
import ru.kyamshanov.missionChat.presentation.components.WelcomeScreenComponent
import ru.kyamshanov.missionChat.presentation.components.impl.DefaultChatInputComponent
import ru.kyamshanov.missionChat.presentation.components.impl.DefaultMessagesComponent
import ru.kyamshanov.missionChat.presentation.components.impl.DefaultSidebarComponent
import ru.kyamshanov.missionChat.presentation.components.impl.DefaultWelcomeScreenComponent
import ru.kyamshanov.missionChat.presentation.container.ChatContainer
import ru.kyamshanov.missionChat.presentation.container.ChatInputContainer
import ru.kyamshanov.missionChat.presentation.container.WelcomeScreenContainer
import ru.kyamshanov.missionChat.presentation.factories.KoinRootComponentFactory
import ru.kyamshanov.missionChat.presentation.factories.RootComponentFactory
import ru.kyamshanov.missionChat.utils.*

val sharedModule = module {
    includes(DomainDiModule)
    single<RootComponentFactory> { KoinRootComponentFactory() }
    single<ComponentFactory> { KoinComponentFactory() }

    factory<WelcomeScreenComponent> { (params: WelcomeScreenParams) ->
        DefaultWelcomeScreenComponent(
            componentContext = params.componentContext,
            containerFactory = { WelcomeScreenContainer() },
            componentFactory = get(),
        )
    }

    factory<ChatInputComponent> { (params: ChatInputParams) ->
        DefaultChatInputComponent(
            componentContext = params.componentContext,
            containerFactory = { ChatInputContainer(it) },
            onSendMessage = params.onSendMessage,
            onStopGeneration = params.onStopGeneration,
        )
    }

    factory<MessagesComponent> { (params: MessagesParams) ->
        DefaultMessagesComponent(
            componentContext = params.componentContext,
            containerFactory = { ChatContainer(params.chat, get()) },
            onChatCreated = params.onChatCreated,
            onTopicCreated = params.onTopicCreated,
        )
    }

    factory<SidebarComponent> { (params: SidebarParams) ->
        DefaultSidebarComponent(
            componentContext = params.componentContext,
            userChatInteractor = get(),
            onSelectedCallback = params.onSelected
        )
    }
}