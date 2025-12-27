package app.forigon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Provides safe insets for round watch screens.
 * Content is padded to stay within the visible circular area.
 */
@Composable
fun RoundScreenScaffold(
    modifier: Modifier = Modifier,
    isRound: Boolean = true, // Set based on device detection
    backgroundColor: Color = Color.Black,
    content: @Composable BoxScope.() -> Unit
) {
    val config = LocalConfiguration.current
    val screenSize = min(config.screenWidthDp, config.screenHeightDp).dp
    
    // For round screens, content at corners gets clipped
    // Safe inset = radius - (radius / sqrt(2)) ≈ 0.293 * radius
    val safeInset: Dp = if (isRound) {
        (screenSize / 2) * (1f - (1f / sqrt(2f)))
    } else {
        16.dp
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .then(
                if (isRound) Modifier.clip(CircleShape) else Modifier
            )
            .padding(safeInset),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * For content that should fill to edges (like watch face)
 * but needs to know about round clipping
 */
@Composable
fun WatchScreenContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
        content = content
    )
}