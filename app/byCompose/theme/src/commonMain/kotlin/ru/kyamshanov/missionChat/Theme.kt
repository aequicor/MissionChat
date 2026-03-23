package ru.kyamshanov.missionChat

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object MissionTheme {
    val colors: ExtendedColorScheme
        @Composable
        get() = LocalExtendedColors.current

    val backgroundGradient: List<Color>
        @Composable
        get() = if (isSystemInDarkTheme()) backgroundGradientDark else backgroundGradientLight
}

@Immutable
data class ExtendedColorScheme(
    val brand: ColorFamily,
    val success: ColorFamily,
    val warning: ColorFamily,
    val cosmicDeep: ColorFamily,
)

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

expect val LocalExtendedColors: ProvidableCompositionLocal<ExtendedColorScheme>

expect val AppShapes : Shapes

@Composable
fun GlassBackground(
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    // Анимируем угол наклона градиента для эффекта переливания
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = durationBasedTween(15000), // 15 секунд на полный круг
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = MissionTheme.backgroundGradient,
                    start = Offset(
                        x = 500f * cos(angle) + 500f,
                        y = 500f * sin(angle) + 500f
                    ),
                    end = Offset(
                        x = 500f * cos(angle + PI.toFloat()) + 500f,
                        y = 500f * sin(angle + PI.toFloat()) + 500f
                    )
                )
            )
    ) {
        content()
    }
}

// Вспомогательная функция для плавности
private fun durationBasedTween(duration: Int): TweenSpec<Float> = 
    tween(durationMillis = duration, easing = LinearEasing)

@Composable
expect fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
)
