package ru.kyamshanov.missionChat.di

import org.koin.dsl.module
import ru.kyamshanov.missionChat.presentation.components.WelcomeScreenComponent
import ru.kyamshanov.missionChat.presentation.components.impl.DefaultChatInputComponent
import ru.kyamshanov.missionChat.presentation.components.impl.DefaultSidebarComponent
import ru.kyamshanov.missionChat.presentation.components.impl.DefaultWelcomeScreenComponent
import ru.kyamshanov.missionChat.presentation.components.internal.InternalSidebarComponent
import ru.kyamshanov.missionChat.presentation.factories.KoinRootComponentFactory
import ru.kyamshanov.missionChat.presentation.factories.RootComponentFactory
import ru.kyamshanov.missionChat.utils.ChatInputParams
import ru.kyamshanov.missionChat.utils.ComponentFactory
import ru.kyamshanov.missionChat.utils.KoinComponentFactory
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
            chatOrchestrator = get()
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

    factory<InternalSidebarComponent> { (params: SidebarParams) ->
        DefaultSidebarComponent(
            componentContext = params.componentContext,
            onSelectedCallback = params.onSelectedCallback,
            onArchiveChat = params.onArchiveChat,
            onUnarchiveChat = params.onUnarchiveChat,
        )
    }
}