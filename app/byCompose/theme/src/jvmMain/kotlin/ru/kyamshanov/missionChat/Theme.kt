@file:JvmName("ThemeJvm")

package ru.kyamshanov.missionChat

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

val extendedLight = ExtendedColorScheme(
    brand = ColorFamily(
        brandLight,
        onBrandLight,
        brandContainerLight,
        onBrandContainerLight,
    ),
    success = ColorFamily(
        successLight,
        onSuccessLight,
        successContainerLight,
        onSuccessContainerLight,
    ),
    warning = ColorFamily(
        warningLight,
        onWarningLight,
        warningContainerLight,
        onWarningContainerLight,
    ),
    cosmicDeep = ColorFamily(
        cosmicDeepLight,
        onCosmicDeepLight,
        cosmicDeepContainerLight,
        onCosmicDeepContainerLight,
    ),
)

val extendedDark = ExtendedColorScheme(
    brand = ColorFamily(
        brandDark,
        onBrandDark,
        brandContainerDark,
        onBrandContainerDark,
    ),
    success = ColorFamily(
        successDark,
        onSuccessDark,
        successContainerDark,
        onSuccessContainerDark,
    ),
    warning = ColorFamily(
        warningDark,
        onWarningDark,
        warningContainerDark,
        onWarningContainerDark,
    ),
    cosmicDeep = ColorFamily(
        cosmicDeepDark,
        onCosmicDeepDark,
        cosmicDeepContainerDark,
        onCosmicDeepContainerDark,
    ),
)

val unspecified_scheme = ColorFamily(
    Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified
)

actual val LocalExtendedColors: ProvidableCompositionLocal<ExtendedColorScheme> = staticCompositionLocalOf {
    ExtendedColorScheme(
        brand = unspecified_scheme,
        success = unspecified_scheme,
        warning = unspecified_scheme,
        cosmicDeep = unspecified_scheme,
    )
}

actual val AppShapes : Shapes = Shapes(
    medium = RoundedCornerShape(16.dp),
)
@Composable
actual fun AppTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    content: @Composable() () -> Unit
) {
    val colorScheme = when {
        darkTheme -> darkScheme
        else -> lightScheme
    }

    val extendedColorScheme = when {
        darkTheme -> extendedDark
        else -> extendedLight
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColorScheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content,
            shapes = AppShapes,
        )
    }
}
