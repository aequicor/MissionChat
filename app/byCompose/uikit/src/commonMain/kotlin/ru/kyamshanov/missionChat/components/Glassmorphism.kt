package ru.kyamshanov.missionChat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp


@Stable
fun Modifier.glassmorphism(
    backgroundColor: Color,
    shape: Shape = RoundedCornerShape(16.dp),
): Modifier =
    this.clip(shape)
        .background(
            Brush.verticalGradient(
                colors = listOf(
                    backgroundColor.copy(alpha = 0.25f),
                    backgroundColor.copy(alpha = 0.15f)
                )
            )
        )
        .border(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    backgroundColor.copy(alpha = 0.4f),
                    backgroundColor.copy(alpha = 0.2f)
                )
            ), shape = shape
        )
