package app.forigon.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * Honeycomb hex-grid layout with pan support.
 * Items are arranged in concentric hex rings around center.
 */
@Composable
fun BubbleCloudLayout(
    modifier: Modifier = Modifier,
    itemSizeDp: Int = 70, // Size of each bubble including spacing
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Layout(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            },
        content = content
    ) { measurables, constraints ->
        val placeables = measurables.map {
            it.measure(Constraints.fixed(itemSizeDp.dp.roundToPx(), itemSizeDp.dp.roundToPx()))
        }

        val centerX = constraints.maxWidth / 2f
        val centerY = constraints.maxHeight / 2f

        // Hex grid spacing
        val radius = itemSizeDp.dp.toPx() * 0.55f // Slight overlap for honeycomb feel
        val sqrt3 = sqrt(3f)

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { index, placeable ->
                val (hx, hy) = indexToHexCoord(index)

                // Convert hex coords to pixel coords (pointy-top orientation)
                val px = radius * sqrt3 * (hx + hy / 2f)
                val py = radius * 1.5f * hy

                val finalX = centerX + px + offsetX - placeable.width / 2f
                val finalY = centerY + py + offsetY - placeable.height / 2f

                placeable.place(finalX.roundToInt(), finalY.roundToInt())
            }
        }
    }
}

/**
 * Maps linear index to hex axial coordinates (q, r) in spiral order.
 * Ring 0: center (0,0)
 * Ring 1: 6 tiles around center
 * Ring 2: 12 tiles, etc.
 */
private fun indexToHexCoord(index: Int): Pair<Int, Int> {
    if (index == 0) return Pair(0, 0)

    // Find which ring this index is in
    var ring = 1
    var ringStart = 1
    while (ringStart + ring * 6 <= index) {
        ringStart += ring * 6
        ring++
    }

    val posInRing = index - ringStart
    val sideLength = ring
    val side = posInRing / sideLength
    val posOnSide = posInRing % sideLength

    // Hex directions for each side of the ring (pointy-top)
    val directions = listOf(
        Pair(1, -1),   // SE
        Pair(0, 1),    // S  (actually SW in pointy)
        Pair(-1, 1),   // SW (actually W)
        Pair(-1, 0),   // NW
        Pair(0, -1),   // N (actually NE)
        Pair(1, -1)    // NE (actually E) - wraps
    )

    // Start position for this ring (top of ring)
    var q = 0
    var r = -ring

    // Walk to the starting corner of current side
    for (s in 0 until side) {
        q += directions[s].first * sideLength
        r += directions[s].second * sideLength
    }

    // Walk along current side
    q += directions[side].first * posOnSide
    r += directions[side].second * posOnSide

    return Pair(q, r)
}