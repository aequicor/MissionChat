package ru.kyamshanov.missionChat

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value

interface RootComponent {

    val stack: Value<ChildStack<*, Child>>

    sealed class Child {
        class WelcomeScreen(val component: WelcomeScreenComponent) : Child()
        class DetailsChild(val component: DetailsComponent) : Child()
    }
}