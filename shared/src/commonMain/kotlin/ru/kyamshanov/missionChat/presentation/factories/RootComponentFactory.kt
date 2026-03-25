package ru.kyamshanov.missionChat.presentation.factories

import com.arkivanov.decompose.ComponentContext
import ru.kyamshanov.missionChat.presentation.components.RootComponent

interface RootComponentFactory {

    fun create(componentContext: ComponentContext): RootComponent
}

