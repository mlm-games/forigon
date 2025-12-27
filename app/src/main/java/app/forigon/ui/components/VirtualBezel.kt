package app.forigon.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Detects circular motion along the edge of the screen (like Samsung rotating bezel).
 * Only activates when touch is near the screen edge.
 *
 * @param edgeThresholdFraction How close to edge to activate (0.15 = outer 15% of radius)
 * @param onRotated Callback with rotation delta (positive = clockwise, negative = counter-clockwise)
 */
fun Modifier.virtualRotaryInput(
    edgeThresholdFraction: Float = 0.20f,
    onRotated: (Float) -> Unit
): Modifier = this.pointerInput(Unit) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val maxRadius = minOf(centerX, centerY)
    val minEdgeRadius = maxRadius * (1f - edgeThresholdFraction)

    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)

        // Check if touch started near edge
        val downVec = down.position - Offset(centerX, centerY)
        val downRadius = sqrt(downVec.x * downVec.x + downVec.y * downVec.y)

        // Only handle if touch is in the edge zone
        if (downRadius < minEdgeRadius) {
            return@awaitEachGesture // Not on edge, let other handlers deal with it
        }

        var previousAngle = atan2(downVec.y, downVec.x)
        var isBezelGesture = true

        while (isBezelGesture) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull() ?: break

            if (!change.pressed) break

            val position = change.position
            val vec = position - Offset(centerX, centerY)
            val radius = sqrt(vec.x * vec.x + vec.y * vec.y)

            // If finger moved too far from edge, stop bezel gesture
            if (radius < minEdgeRadius * 0.8f) {
                isBezelGesture = false
                break
            }

            val currentAngle = atan2(vec.y, vec.x)
            var angleDiff = currentAngle - previousAngle

            // Handle wrap-around at PI/-PI boundary
            if (angleDiff > Math.PI) {
                angleDiff -= (2 * Math.PI).toFloat()
            } else if (angleDiff < -Math.PI) {
                angleDiff += (2 * Math.PI).toFloat()
            }

            // Only process if there's meaningful rotation
            if (kotlin.math.abs(angleDiff) > 0.01f) {
                // Convert radians to a scroll-friendly value
                // Positive angleDiff = counter-clockwise, so negate for intuitive scroll
                onRotated(-angleDiff * 150f)
                previousAngle = currentAngle
                change.consume()
            }
        }
    }
}