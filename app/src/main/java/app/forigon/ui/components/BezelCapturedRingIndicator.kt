package app.forigon.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import app.forigon.ui.theme.LauncherColors

/**
 * Overlay ring indicator shown when virtual bezel is "captured"
 */
@Composable
fun BezelCapturedRingIndicator(
    active: Boolean,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = tween(150),
        label = "bezel_ring_alpha"
    )

    if (alpha <= 0.01f) return

    Canvas(modifier = modifier) {
        val stroke = 4.dp.toPx()
        val r = (size.minDimension / 2f) - stroke

        // Outer ring glow
        drawCircle(
            color = LauncherColors.AccentBlue.copy(alpha = 0.25f * alpha),
            radius = r,
            style = Stroke(width = stroke)
        )

        // Inner ring
        drawCircle(
            color = Color.White.copy(alpha = 0.20f * alpha),
            radius = r - stroke * 1.2f,
            style = Stroke(width = stroke * 0.6f)
        )
    }
}