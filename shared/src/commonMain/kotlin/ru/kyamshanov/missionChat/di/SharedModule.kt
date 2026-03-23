package ru.kyamshanov.missionChat.di

import org.koin.dsl.module
import ru.kyamshanov.missionChat.*
import ru.kyamshanov.missionChat.presentation.container.ChatContainer
import ru.kyamshanov.missionChat.presentation.container.ChatInputContainer
import ru.kyamshanov.missionChat.presentation.container.WelcomeScreenContainer
import ru.kyamshanov.missionChat.utils.*

val sharedModule = module {
    includes(DomainDiModule)
    single<RootComponentFactory> { KoinRootComponentFactory() }
    single<ComponentFactory> { KoinComponentFactory() }

    factory<WelcomeScreenComponent> { (params: WelcomeScreenParams) ->
        DefaultWelcomeScreenComponent(
            componentContext = params.componentContext,
            containerFactory = { WelcomeScreenContainer() },
            componentFactory = get()
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
        )
    }
}