package app.forigon.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Detent-based "virtual bezel" (touch rotary) for one-finger watches.
 *
 * - Requires gesture start near the edge ring.
 * - Once captured, allows drifting inward until stickyInnerFraction.
 * - Emits discrete step "detents" rather than continuous deltas.
 */
fun Modifier.virtualRotaryDetents(
    enabled: Boolean = true,
    edgeThresholdFraction: Float = 0.30f,
    detentDegrees: Float = 15f,
    stickyInnerFraction: Float = 0.60f,
    onActiveChanged: (Boolean) -> Unit = {},
    onDetents: (steps: Int) -> Unit
): Modifier = if (!enabled) this else this.pointerInput(edgeThresholdFraction, detentDegrees, stickyInnerFraction) {

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

                // Allow drift inward, but bail if too far inside
                if (r < stickyRadius) break

                val curAngle = atan2(vec.y, vec.x)
                accum += angleWrapDiff(curAngle, prevAngle)
                prevAngle = curAngle

                var steps = 0
                while (accum >= detentRad) { steps += 1; accum -= detentRad }
                while (accum <= -detentRad) { steps -= 1; accum += detentRad }

                if (steps != 0) {
                    // Negate so clockwise feels like "down"
                    onDetents(-steps)
                    change.consume()
                }
            }
        } finally {
            onActiveChanged(false)
        }
    }
}