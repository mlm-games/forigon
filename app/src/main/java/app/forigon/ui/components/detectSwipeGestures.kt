package app.forigon.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs

/**
 * Adds swipe gesture detection to a composable
 */
fun Modifier.detectSwipeGestures(
    sensitivity: Float = 1.0f,
    onSwipeUp: () -> Unit = {},
    onSwipeDown: () -> Unit = {},
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
): Modifier = this.then(
    Modifier.pointerInput(sensitivity) {
        val baseMinSwipeDistance = 50f
        val minSwipeDistance = baseMinSwipeDistance * sensitivity

        var totalDrag = Offset.Zero
        var swipeTriggered = false

        detectDragGestures(
            onDragStart = {
                totalDrag = Offset.Zero
                swipeTriggered = false
            },
            onDragEnd = {
                if (!swipeTriggered) {
                    val x = totalDrag.x
                    val y = totalDrag.y

                    when {
                        abs(x) > abs(y) && abs(x) > minSwipeDistance -> {
                            if (x > 0) onSwipeRight() else onSwipeLeft()
                        }
                        abs(y) > abs(x) && abs(y) > minSwipeDistance -> {
                            if (y > 0) onSwipeDown() else onSwipeUp()
                        }
                    }
                }
            }
        ) { change, dragAmount ->
            change.consume()
            totalDrag += dragAmount

            if (!swipeTriggered) {
                val x = totalDrag.x
                val y = totalDrag.y

                when {
                    abs(x) > abs(y) && abs(x) > minSwipeDistance -> {
                        swipeTriggered = true
                        if (x > 0) onSwipeRight() else onSwipeLeft()
                    }
                    abs(y) > abs(x) && abs(y) > minSwipeDistance -> {
                        swipeTriggered = true
                        if (y > 0) onSwipeDown() else onSwipeUp()
                    }
                }
            }
        }
    }
)