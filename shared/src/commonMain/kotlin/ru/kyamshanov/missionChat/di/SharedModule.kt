package ru.kyamshanov.missionChat.di

import org.koin.dsl.module
import ru.kyamshanov.missionChat.ChatInputComponent
import ru.kyamshanov.missionChat.DefaultChatInputComponent
import ru.kyamshanov.missionChat.DefaultMessagesComponent
import ru.kyamshanov.missionChat.DefaultWelcomeScreenComponent
import ru.kyamshanov.missionChat.KoinRootComponentFactory
import ru.kyamshanov.missionChat.MessagesComponent
import ru.kyamshanov.missionChat.RootComponentFactory
import ru.kyamshanov.missionChat.WelcomeScreenComponent
import ru.kyamshanov.missionChat.presentation.container.ChatInputContainer
import ru.kyamshanov.missionChat.presentation.container.ChatContainer
import ru.kyamshanov.missionChat.presentation.container.WelcomeScreenContainer
import ru.kyamshanov.missionChat.utils.ChatInputParams
import ru.kyamshanov.missionChat.utils.ComponentFactory
import ru.kyamshanov.missionChat.utils.KoinComponentFactory
import ru.kyamshanov.missionChat.utils.MessagesParams
import ru.kyamshanov.missionChat.utils.WelcomeScreenParams

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
            containerFactory = { ChatContainer(get()) },
        )
    }
}