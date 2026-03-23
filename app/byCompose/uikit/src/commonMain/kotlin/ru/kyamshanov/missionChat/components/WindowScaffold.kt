package ru.kyamshanov.missionChat.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A custom scaffold component designed for window-based layouts that manages a sidebar, a toolbar, and main content.
 * It dynamically decides whether to show a persistent sidebar or a floating one based on available width.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param minSlidebarWidth The minimum width required for the sidebar to be displayed in its persistent state.
 * @param maxSlidebarWidth The maximum width the sidebar can occupy.
 * @param maxContentWidth The maximum width the main content area can occupy.
 * @param toolbarHeight The fixed height allocated for the toolbar.
 * @param innerPaddings The padding applied between elements and the window edges.
 * @param slidebarContent Content for the persistent sidebar, receives visibility state.
 * @param floatingSlidebarContent Content for the sidebar when it's in a floating/hidden state.
 * @param toolbarContent Content for the toolbar, receives the sidebar visibility state.
 * @param content The main body content of the scaffold.
 */
@Composable
fun WindowScaffold(
    modifier: Modifier = Modifier,
    minSlidebarWidth: Dp = 150.dp,
    maxSlidebarWidth: Dp = 300.dp,
    maxContentWidth: Dp = 1000.dp,
    toolbarHeight: Dp = 200.dp,
    innerPaddings: Dp = 16.dp,
    slidebarContent: @Composable (isVisible: Boolean) -> Unit,
    floatingSlidebarContent: @Composable (isVisible: Boolean) -> Unit,
    toolbarContent: @Composable (isSlidebarVisible: Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    SubcomposeLayout(
        modifier = modifier,
    ) { constraints ->
        val innerPaddingsPx = innerPaddings.roundToPx()
        val maxContentWidthPx = maxContentWidth.roundToPx().coerceAtMost(constraints.maxWidth)
        val minSlidebarWidthPx = minSlidebarWidth.roundToPx()
        val availableSlidebarWidth = constraints.maxWidth - maxContentWidthPx
        val maxSlidebarWidthPx = maxSlidebarWidth.roundToPx().coerceAtMost(availableSlidebarWidth)
        val isSlidebarVisible = maxSlidebarWidthPx >= minSlidebarWidthPx

        val toolbarResult = measureToolbar(
            constraints = constraints,
            maxContentWidthPx = maxContentWidthPx,
            innerPaddingsPx = innerPaddingsPx,
            toolbarHeight = toolbarHeight
        ) { toolbarContent(isSlidebarVisible) }

        val contentPlaceables =
            measureContent(constraints, innerPaddingsPx, maxContentWidthPx, toolbarResult.height) { content() }
        val contentWidthPx = calculateContentWidth(contentPlaceables, toolbarResult.placeable)

        val slidebarPlaceable = measureSlidebar(
            constraints = constraints,
            isSlidebarVisible = isSlidebarVisible,
            minSlidebarWidthPx = minSlidebarWidthPx,
            maxSlidebarWidthPx = maxSlidebarWidthPx,
            innerPaddingsPx = innerPaddingsPx
        ) { slidebarContent(isSlidebarVisible) }

        val floatingSlidebarPlaceable = measureFloatingSlidebar(
            constraints = constraints,
            isFloatingSlidebarVisible = !isSlidebarVisible,
            maxSlidebarWidth = maxSlidebarWidth,
            innerPaddingsPx = innerPaddingsPx
        ) { floatingSlidebarContent(!isSlidebarVisible) }

        val contentX = calculateContentX(
            isSlidebarVisible = isSlidebarVisible,
            slidebarWidth = slidebarPlaceable?.width ?: 0,
            contentWidth = contentWidthPx,
            maxWidth = constraints.maxWidth,
            minSlidebarWidth = minSlidebarWidthPx,
            maxContentWidth = maxContentWidthPx,
            padding = innerPaddingsPx,
        )

        layout(constraints.maxWidth, constraints.maxHeight) {
            contentPlaceables.forEach { it.placeRelative(contentX, toolbarResult.height + innerPaddingsPx) }
            toolbarResult.placeable?.placeRelative(contentX, innerPaddingsPx)
            slidebarPlaceable?.placeRelative(innerPaddingsPx, innerPaddingsPx)
            floatingSlidebarPlaceable?.placeRelative(innerPaddingsPx, innerPaddingsPx)
        }
    }
}

private fun SubcomposeMeasureScope.measureToolbar(
    constraints: Constraints,
    maxContentWidthPx: Int,
    innerPaddingsPx: Int,
    toolbarHeight: Dp,
    content: @Composable () -> Unit
): ToolbarMeasureResult {
    val toolbarMeasurable = subcompose("toolbar", content).firstOrNull()
    val placeable = toolbarMeasurable?.measure(
        constraints.copy(
            minWidth = 0,
            maxWidth = (maxContentWidthPx - 2 * innerPaddingsPx).coerceAtLeast(0),
            minHeight = 0,
            maxHeight = toolbarHeight.roundToPx()
        )
    )
    return ToolbarMeasureResult(placeable, placeable?.height ?: 0)
}

private fun SubcomposeMeasureScope.measureContent(
    constraints: Constraints,
    innerPaddingsPx: Int,
    maxContentWidthPx: Int,
    toolbarHeightPx: Int,
    content: @Composable () -> Unit
): List<Placeable> {
    val contentMeasurables = subcompose("content", content)
    val contentConstraint = constraints.copy(
        minWidth = 0,
        maxWidth = (maxContentWidthPx - 2 * innerPaddingsPx).coerceAtLeast(0),
        minHeight = 0,
        maxHeight = (constraints.maxHeight - toolbarHeightPx - 2 * innerPaddingsPx).coerceAtLeast(0)
    )
    return contentMeasurables.map { it.measure(contentConstraint) }
}

private fun calculateContentWidth(contentPlaceables: List<Placeable>, toolbarPlaceable: Placeable?): Int {
    val maxContentPlaceableWidth = contentPlaceables.maxOfOrNull { it.width } ?: 0
    return if (toolbarPlaceable != null) {
        maxContentPlaceableWidth.coerceAtLeast(toolbarPlaceable.width)
    } else {
        maxContentPlaceableWidth
    }
}

private fun SubcomposeMeasureScope.measureSlidebar(
    constraints: Constraints,
    isSlidebarVisible: Boolean,
    minSlidebarWidthPx: Int,
    maxSlidebarWidthPx: Int,
    innerPaddingsPx: Int,
    content: @Composable () -> Unit
): Placeable? {
    val minWidth = minSlidebarWidthPx - innerPaddingsPx
    val slidebarConstraints = constraints.copy(
        minWidth = minWidth,
        maxWidth = (maxSlidebarWidthPx - innerPaddingsPx).coerceAtLeast(minWidth),
        minHeight = 0,
        maxHeight = (constraints.maxHeight - 2 * innerPaddingsPx)
    )
    return subcompose("slidebar") { content() }.firstOrNull()?.measure(slidebarConstraints)
}

private fun SubcomposeMeasureScope.measureFloatingSlidebar(
    constraints: Constraints,
    isFloatingSlidebarVisible: Boolean,
    maxSlidebarWidth: Dp,
    innerPaddingsPx: Int,
    content: @Composable () -> Unit
): Placeable? {
    val floatingSlidebarConstraints = constraints.copy(
        minWidth = 0,
        maxWidth = (maxSlidebarWidth.roundToPx() - 2 * innerPaddingsPx).coerceIn(
            minimumValue = 0,
            maximumValue = constraints.maxWidth - 2 * innerPaddingsPx
        ),
        minHeight = 0,
        maxHeight = (constraints.maxHeight - 2 * innerPaddingsPx)
    )
    return subcompose("floatingSlidebar") { content() }.firstOrNull()?.measure(floatingSlidebarConstraints)
}

private fun calculateContentX(
    isSlidebarVisible: Boolean,
    slidebarWidth: Int,
    contentWidth: Int,
    maxWidth: Int,
    minSlidebarWidth: Int,
    maxContentWidth: Int,
    padding: Int,
): Int {
    return padding + if (isSlidebarVisible) {
        val slidebarWidthWithPadding = slidebarWidth + padding
        val contentWidthWithPadding = contentWidth + 2 * padding
        val canCenterContent = contentWidthWithPadding + 2 * slidebarWidthWithPadding < maxWidth

        if (canCenterContent) {
            (maxWidth - contentWidthWithPadding) / 2
        } else {
            slidebarWidthWithPadding
        }
    } else {
        val contentWidthWithMargins = 2 * padding + contentWidth
        val minSlidebarWidthWithPadding = minSlidebarWidth + padding
        val isContentOverflowing = contentWidthWithMargins + minSlidebarWidthWithPadding >= maxWidth

        if (isContentOverflowing) {
            maxWidth - maxContentWidth
        } else {
            0
        }
    }
}

private data class ToolbarMeasureResult(val placeable: Placeable?, val height: Int)
