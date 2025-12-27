package app.forigon.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Virtual rotary bezel detector.
 * Tracks circular gestures starting near screen edge and fires detent callbacks.
 *
 * @param enabled Whether the detector is active
 * @param edgeThresholdFraction Outer ring thickness as fraction of radius (0.30 = outer 30%)
 * @param stickyInnerFraction How far finger can drift inward before releasing (0.60 = 60% of radius)
 * @param detentDegrees Degrees per "click" / detent
 * @param onActiveChanged Called when bezel capture state changes (true = captured, false = released)
 * @param onDetent Called with step count per detent (-1 or +1 per step, batched)
 */
fun Modifier.virtualRotaryDetents(
    enabled: Boolean = true,
    edgeThresholdFraction: Float = 0.30f,
    stickyInnerFraction: Float = 0.60f,
    detentDegrees: Float = 15f,
    onActiveChanged: (Boolean) -> Unit = {},
    onDetent: (steps: Int) -> Unit
): Modifier = if (!enabled) this else this.pointerInput(
    edgeThresholdFraction,
    stickyInnerFraction,
    detentDegrees
) {
    fun angleWrapDiff(cur: Float, prev: Float): Float {
        var d = cur - prev
        val pi = Math.PI.toFloat()
        if (d > pi) d -= (2f * pi)
        if (d < -pi) d += (2f * pi)
        return d
    }

    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)

        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = minOf(center.x, center.y)
        val activationRadius = maxRadius * (1f - edgeThresholdFraction)
        val stickyRadius = maxRadius * stickyInnerFraction

        val downVec = down.position - center
        val downR = sqrt(downVec.x * downVec.x + downVec.y * downVec.y)

        // Must start near edge
        if (downR < activationRadius) return@awaitEachGesture

        onActiveChanged(true)
        try {
            var prevAngle = atan2(downVec.y, downVec.x)
            var accum = 0f
            val detentRad = Math.toRadians(detentDegrees.toDouble()).toFloat()

            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull() ?: break
                if (!change.pressed) break

                val vec = change.position - center
                val r = sqrt(vec.x * vec.x + vec.y * vec.y)

                // Sticky: allow drift inward, but bail if too far inside
                if (r < stickyRadius) break

                val curAngle = atan2(vec.y, vec.x)
                accum += angleWrapDiff(curAngle, prevAngle)
                prevAngle = curAngle

                var steps = 0
                while (accum >= detentRad) { steps += 1; accum -= detentRad }
                while (accum <= -detentRad) { steps -= 1; accum += detentRad }

                if (steps != 0) {
                    // Negate so clockwise feels like "down"
                    onDetent(-steps)
                    change.consume()
                }
            }
        } finally {
            onActiveChanged(false)
        }
    }
}