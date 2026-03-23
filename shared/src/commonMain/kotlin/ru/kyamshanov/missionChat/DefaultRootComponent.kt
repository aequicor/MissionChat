package ru.kyamshanov.missionChat

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import ru.kyamshanov.missionChat.RootComponent.Child.DetailsChild
import ru.kyamshanov.missionChat.RootComponent.Child.WelcomeScreen
import ru.kyamshanov.missionChat.utils.ComponentFactory
import ru.kyamshanov.missionChat.utils.WelcomeScreenParams


internal class DefaultRootComponent(
    componentContext: ComponentContext,
    private val componentFactory: ComponentFactory
) : RootComponent, ComponentContext by componentContext, KoinComponent {

    private val navigation = StackNavigation<ScreenConfig>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = ScreenConfig.serializer(),
            initialConfiguration = ScreenConfig.Welcome, // The initial child component is List
            handleBackButton = true, // Automatically pop from the stack on back button presses
            childFactory = ::child,
        )

    private fun child(config: ScreenConfig, componentContext: ComponentContext): RootComponent.Child =
        when (config) {
            is ScreenConfig.Welcome -> {
                WelcomeScreen(componentFactory.createWelcomeScreenComponent(WelcomeScreenParams(componentContext)))
            }

            is ScreenConfig.Details -> DetailsChild(get { parametersOf(componentContext) })
        }

    @Serializable
    private sealed interface ScreenConfig {
        @Serializable
        data object Welcome : ScreenConfig

        @Serializable
        data class Details(val title: String) : ScreenConfig
    }
}