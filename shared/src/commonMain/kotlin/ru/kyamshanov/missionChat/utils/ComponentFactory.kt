package ru.kyamshanov.missionChat.utils

import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import ru.kyamshanov.missionChat.presentation.components.ChatInputComponent
import ru.kyamshanov.missionChat.presentation.components.internal.InternalSidebarComponent
import ru.kyamshanov.missionChat.presentation.components.MessagesComponent
import ru.kyamshanov.missionChat.presentation.components.SidebarComponent
import ru.kyamshanov.missionChat.presentation.components.WelcomeScreenComponent
import ru.kyamshanov.missionChat.presentation.components.internal.ChatOrchestratorComponent
import ru.kyamshanov.missionChat.presentation.components.internal.InternalChatInputComponent

internal interface ComponentFactory {

    fun createWelcomeScreenComponent(
        params: WelcomeScreenParams,
    ): WelcomeScreenComponent

    fun createChatInputComponent(
        params: ChatInputParams,
    ): InternalChatInputComponent

    fun createMessagesComponent(
        params: MessagesParams,
    ): MessagesComponent

    fun createSidebarComponent(
        params: SidebarParams,
    ): InternalSidebarComponent

    fun createChatOrchestratorComponent(
        params: ChatOrchestratorParams
    ): ChatOrchestratorComponent
}

internal class KoinComponentFactory : ComponentFactory, KoinComponent {

    override fun createWelcomeScreenComponent(params: WelcomeScreenParams): WelcomeScreenComponent =
        get { parametersOf(params) }

    override fun createChatInputComponent(params: ChatInputParams): InternalChatInputComponent =
        get { parametersOf(params) }

    override fun createMessagesComponent(params: MessagesParams): MessagesComponent =
        get { parametersOf(params) }

    override fun createSidebarComponent(params: SidebarParams): InternalSidebarComponent =
        get { parametersOf(params) }

    override fun createChatOrchestratorComponent(params: ChatOrchestratorParams): ChatOrchestratorComponent =
        get { parametersOf(params) }
}
