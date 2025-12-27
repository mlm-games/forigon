package app.forigon.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.forigon.ui.theme.LauncherAnimation
import app.forigon.ui.theme.LauncherColors

/**
 * Premium focus indicator with glow effect
 */
@Composable
fun Modifier.focusHighlight(
    isFocused: Boolean,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    glowColor: Color = LauncherColors.AccentBlue,
    borderWidth: Dp = 3.dp
): Modifier {
    val animatedBorderAlpha by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0f,
        animationSpec = tween(LauncherAnimation.FastDuration),
        label = "border_alpha"
    )
    
    val animatedGlowAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.3f else 0f,
        animationSpec = tween(LauncherAnimation.NormalDuration),
        label = "glow_alpha"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "glow_pulse")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse_value"
    )
    
    return this
        .drawBehind {
            if (animatedGlowAlpha > 0f) {
                // Outer glow
                drawRoundRect(
                    color = glowColor.copy(alpha = animatedGlowAlpha * glowPulse),
                    cornerRadius = CornerRadius(20.dp.toPx()),
                    size = size.copy(
                        width = size.width + 16.dp.toPx(),
                        height = size.height + 16.dp.toPx()
                    ),
                    topLeft = androidx.compose.ui.geometry.Offset(-8.dp.toPx(), -8.dp.toPx())
                )
            }
        }
        .then(
            if (animatedBorderAlpha > 0f) {
                Modifier.border(
                    width = borderWidth,
                    color = Color.White.copy(alpha = animatedBorderAlpha),
                    shape = shape
                )
            } else Modifier
        )
}

/**
 * Scale animation for focus/press states
 */
@Composable
fun Modifier.scaleOnFocus(
    isFocused: Boolean,
    isPressed: Boolean = false
): Modifier {
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> LauncherAnimation.PressScale
            isFocused -> LauncherAnimation.FocusScale
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "focus_scale"
    )
    
    return this.scale(scale)
}

/**
 * Shimmer loading effect
 */
@Composable
fun Modifier.shimmer(
    isLoading: Boolean,
    shimmerColor: Color = Color.White.copy(alpha = 0.1f)
): Modifier {
    if (!isLoading) return this
    
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_progress"
    )
    
    return this.drawBehind {
        val shimmerWidth = size.width * 0.4f
        val startX = -shimmerWidth + (size.width + shimmerWidth * 2) * shimmerProgress
        
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    shimmerColor,
                    Color.Transparent
                ),
                startX = startX,
                endX = startX + shimmerWidth
            )
        )
    }
}

/**
 * Staggered entrance animation for lists
 */
@Composable
fun StaggeredAnimatedVisibility(
    visible: Boolean,
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    val delay = (index * LauncherAnimation.StaggerDelayMs).toInt()
    
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = LauncherAnimation.NormalDuration,
                delayMillis = delay
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = LauncherAnimation.NormalDuration,
                delayMillis = delay
            ),
            initialOffsetY = { it / 4 }
        ),
        exit = fadeOut(animationSpec = tween(LauncherAnimation.FastDuration)),
        content = content
    )
}

/**
 * Parallax background effect
 */
@Composable
fun ParallaxBackground(
    scrollOffset: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val parallaxOffset = scrollOffset * 0.3f
    
    Box(
        modifier = modifier.offset(y = (-parallaxOffset).dp)
    ) {
        content()
    }
}

/**
 * Gradient overlay for cards
 */
@Composable
fun Modifier.cardGradientOverlay(
    startColor: Color = Color.Transparent,
    endColor: Color = Color.Black.copy(alpha = 0.7f)
): Modifier = this.background(
    Brush.verticalGradient(
        colors = listOf(startColor, endColor),
        startY = 0f,
        endY = Float.POSITIVE_INFINITY
    )
)

/**
 * Pulsing dot indicator
 */
@Composable
fun PulsingDot(
    color: Color = LauncherColors.AccentBlue,
    size: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

@Composable
fun AnimatedVisibility(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(LauncherAnimation.FastDuration)) +
                expandVertically(tween(LauncherAnimation.FastDuration)),
        exit = fadeOut(tween(LauncherAnimation.FastDuration)) +
                shrinkVertically(tween(LauncherAnimation.FastDuration))
    ) {
        content()
    }
}