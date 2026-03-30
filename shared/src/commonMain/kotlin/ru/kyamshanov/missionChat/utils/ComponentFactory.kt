package ru.kyamshanov.missionChat.utils

import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import ru.kyamshanov.missionChat.presentation.components.WelcomeScreenComponent
import ru.kyamshanov.missionChat.presentation.components.internal.InternalSidebarComponent

internal interface ComponentFactory {

    fun createWelcomeScreenComponent(
        params: WelcomeScreenParams,
    ): WelcomeScreenComponent

    fun createChatInputComponent(
        params: ChatInputParams,
    ): InternalChatInputComponent


    fun createSidebarComponent(
        params: SidebarParams,
    ): InternalSidebarComponent
}

internal class KoinComponentFactory : ComponentFactory, KoinComponent {

    override fun createWelcomeScreenComponent(params: WelcomeScreenParams): WelcomeScreenComponent =
        get { parametersOf(params) }

    override fun createChatInputComponent(params: ChatInputParams): InternalChatInputComponent =
        get { parametersOf(params) }

    override fun createSidebarComponent(params: SidebarParams): InternalSidebarComponent =
        get { parametersOf(params) }
}
