package ru.kyamshanov.missionChat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ru.kyamshanov.missionChat.presentation.components.DetailsComponent

@Composable
fun DetailsContent(component: DetailsComponent, modifier: Modifier = Modifier) {
    val model by component.model.subscribeAsState()
    Box(modifier = modifier.safeDrawingPadding()) {
        CustomButton(onClick = { component.onBack() }){
            Text(text = model.title)
        }
    }
}