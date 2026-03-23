package ru.kyamshanov.missionChat

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import ru.kyamshanov.missionChat.welcomeScreen.WelcomeScreen

@Composable
fun RootContent(component: RootComponent, modifier: Modifier = Modifier) {
    Children(
        stack = component.stack,
        modifier = modifier,
        animation = stackAnimation(fade()),
    ) {
        when (val child = it.instance) {
            is RootComponent.Child.WelcomeScreen -> WelcomeScreen(component = child.component)
            is RootComponent.Child.DetailsChild -> DetailsContent(component = child.component)
        }
    }
}