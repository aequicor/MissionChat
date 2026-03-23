package ru.kyamshanov.missionChat

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun App(rootComponent: RootComponent, modifier: Modifier = Modifier.fillMaxSize()) {
    AppTheme {
        RootContent(component = rootComponent, modifier = Modifier.fillMaxSize())
    }
}