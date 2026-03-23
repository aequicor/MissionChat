package ru.kyamshanov.missionChat.utils

import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import ru.kyamshanov.missionChat.ChatInputComponent
import ru.kyamshanov.missionChat.MessagesComponent
import ru.kyamshanov.missionChat.WelcomeScreenComponent

internal interface ComponentFactory {

    fun createWelcomeScreenComponent(
        params: WelcomeScreenParams,
    ): WelcomeScreenComponent

    fun createChatInputComponent(
        params: ChatInputParams,
    ): ChatInputComponent

    fun createMessagesComponent(
        params: MessagesParams,
    ): MessagesComponent
}

internal class KoinComponentFactory : ComponentFactory, KoinComponent {

    override fun createWelcomeScreenComponent(params: WelcomeScreenParams): WelcomeScreenComponent =
        get { parametersOf(params) }

    override fun createChatInputComponent(params: ChatInputParams): ChatInputComponent =
        get { parametersOf(params) }

    override fun createMessagesComponent(params: MessagesParams): MessagesComponent =
        get { parametersOf(params) }
}