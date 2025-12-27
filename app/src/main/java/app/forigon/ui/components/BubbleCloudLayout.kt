package app.forigon.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Honeycomb hex-grid layout with pan and zoom support.
 * Items are arranged in concentric hex rings around center.
 *
 * Supports:
 * - Drag to pan
 * - Pinch to zoom
 * - Double-tap on empty space to cycle zoom levels
 * - External zoom delta from rotary input
 */
@Composable
fun BubbleCloudLayout(
    modifier: Modifier = Modifier,
    itemSizeDp: Int = 70,
    externalZoomDelta: Int = 0,
    onZoomDeltaConsumed: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    // Pan state
    var offset by remember { mutableStateOf(Offset.Zero) }
    val animatedOffsetX = remember { Animatable(0f) }
    val animatedOffsetY = remember { Animatable(0f) }

    // Zoom state
    val minScale = 0.4f
    val maxScale = 2.5f
    val zoomLevels = floatArrayOf(0.5f, 0.7f, 1.0f, 1.4f, 1.9f, 2.5f)
    var zoomIndex by remember { mutableIntStateOf(2) } // Start at 1.0f
    val animatedScale = remember { Animatable(1f) }

    // Sync animated offset with raw offset during gestures
    LaunchedEffect(offset) {
        animatedOffsetX.snapTo(offset.x)
        animatedOffsetY.snapTo(offset.y)
    }

    // Handle external zoom delta from rotary
    LaunchedEffect(externalZoomDelta) {
        if (externalZoomDelta != 0) {
            val newIndex = (zoomIndex + externalZoomDelta).coerceIn(0, zoomLevels.lastIndex)
            if (newIndex != zoomIndex) {
                zoomIndex = newIndex
                animatedScale.animateTo(
                    zoomLevels[zoomIndex],
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            onZoomDeltaConsumed()
        }
    }

    // Double-tap cycles through zoom levels
    val cycleZoom: (Offset) -> Unit = { tapOffset ->
        val oldScale = animatedScale.value
        zoomIndex = (zoomIndex + 1) % zoomLevels.size
        val newScale = zoomLevels[zoomIndex]

        // Adjust offset to zoom toward tap point
        val scaleChange = newScale / oldScale
        val newOffset = Offset(
            offset.x * scaleChange + tapOffset.x * (1 - scaleChange),
            offset.y * scaleChange + tapOffset.y * (1 - scaleChange)
        )
        offset = newOffset

        scope.launch {
            animatedScale.animateTo(
                newScale,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    // Reset to center with animation
    val resetView: () -> Unit = {
        scope.launch {
            launch { animatedOffsetX.animateTo(0f, spring()) }
            launch { animatedOffsetY.animateTo(0f, spring()) }
            launch {
                zoomIndex = 2
                animatedScale.animateTo(1f, spring())
            }
        }
        offset = Offset.Zero
    }

    Box(modifier = modifier) {
        Layout(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        // Pan
                        offset += pan

                        // Pinch zoom toward centroid
                        if (zoom != 1f) {
                            val oldScale = animatedScale.value
                            val newScale = (oldScale * zoom).coerceIn(minScale, maxScale)

                            // Adjust offset to zoom toward pinch center
                            val scaleChange = newScale / oldScale
                            offset = Offset(
                                offset.x * scaleChange + centroid.x * (1 - scaleChange),
                                offset.y * scaleChange + centroid.y * (1 - scaleChange)
                            )

                            scope.launch { animatedScale.snapTo(newScale) }

                            // Update zoom index to nearest level
                            zoomIndex = zoomLevels.indices.minBy {
                                abs(zoomLevels[it] - newScale)
                            }
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { tapOffset ->
                            cycleZoom(tapOffset)
                        }
                    )
                }
                .graphicsLayer {
                    scaleX = animatedScale.value
                    scaleY = animatedScale.value
                    translationX = animatedOffsetX.value
                    translationY = animatedOffsetY.value
                    // Transform origin at center
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
                },
            content = content
        ) { measurables, constraints ->
            val itemSizePx = itemSizeDp.dp.roundToPx()

            val placeables = measurables.map {
                it.measure(Constraints.fixed(itemSizePx, itemSizePx))
            }

            val centerX = constraints.maxWidth / 2
            val centerY = constraints.maxHeight / 2

            // Hex grid spacing
            val radius = itemSizeDp.dp.toPx() * 0.55f
            val sqrt3 = sqrt(3f)

            layout(constraints.maxWidth, constraints.maxHeight) {
                placeables.forEachIndexed { index, placeable ->
                    val (hx, hy) = indexToHexCoord(index)

                    // Convert hex coords to pixel coords (pointy-top orientation)
                    val px = radius * sqrt3 * (hx + hy / 2f)
                    val py = radius * 1.5f * hy

                    placeable.place(
                        (centerX + px - placeable.width / 2f).roundToInt(),
                        (centerY + py - placeable.height / 2f).roundToInt()
                    )
                }
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
        Pair(1, 0),    // E
        Pair(0, 1),    // SE
        Pair(-1, 1),   // SW
        Pair(-1, 0),   // W
        Pair(0, -1),   // NW
        Pair(1, -1)    // NE
    )

    // Start position for this ring (top-right, going clockwise)
    var q = ring
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