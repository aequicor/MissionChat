package ru.kyamshanov.missionChat

import com.arkivanov.decompose.ComponentContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface RootComponentFactory {

    fun create(componentContext: ComponentContext): RootComponent
}

internal class KoinRootComponentFactory : RootComponentFactory, KoinComponent {
    override fun create(componentContext: ComponentContext): RootComponent =
        DefaultRootComponent(componentContext, get())

}