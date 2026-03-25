package ru.kyamshanov.missionChat.presentation.factories

import com.arkivanov.decompose.ComponentContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import ru.kyamshanov.missionChat.presentation.components.RootComponent
import ru.kyamshanov.missionChat.presentation.components.impl.DefaultRootComponent

internal class KoinRootComponentFactory : RootComponentFactory, KoinComponent {
    override fun create(componentContext: ComponentContext): RootComponent =
        DefaultRootComponent(componentContext, get())

}