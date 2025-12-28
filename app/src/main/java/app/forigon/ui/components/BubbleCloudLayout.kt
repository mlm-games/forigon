package app.forigon.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Physics state for a single bubble node.
 */
private class BubbleNode(
    initialX: Float,
    initialY: Float
) {
    var x by mutableFloatStateOf(initialX)
    var y by mutableFloatStateOf(initialY)
    var vx by mutableFloatStateOf(0f)
    var vy by mutableFloatStateOf(0f)
    val homeX = initialX
    val homeY = initialY
    var isDragging by mutableStateOf(false)
}


@Composable
fun <T> BubbleCloudLayout(
    items: List<T>,
    modifier: Modifier = Modifier,
    itemSizeDp: Int = 70,
    externalZoomDelta: Int = 0,
    onZoomDeltaConsumed: () -> Unit = {},
    key: (T) -> Any = { it.hashCode() },
    onItemClick: (T) -> Unit = {},
    onItemLongClick: (T) -> Unit = {},
    useDoubleTapForOptions: Boolean = false,
    itemContent: @Composable BoxScope.(item: T) -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val viewConfiguration = LocalViewConfiguration.current

    val itemSizePx = with(density) { itemSizeDp.dp.toPx() }
    val nodeRadius = itemSizePx / 2f
    val touchSlop = viewConfiguration.touchSlop
    val longPressTimeout = viewConfiguration.longPressTimeoutMillis

    // Pan and zoom state
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    val animatedScale = remember { Animatable(1f) }
    val zoomLevels = floatArrayOf(0.5f, 0.7f, 1.0f, 1.4f, 1.9f, 2.5f)
    var zoomIndex by remember { mutableIntStateOf(2) }
    val minScale = 0.4f
    val maxScale = 2.5f

    // Physics parameters
    val repulsionStrength = 8000f
    val homeAttractionStrength = 0.012f
    val damping = 0.92f
    val minSeparation = itemSizePx * 1.15f
    val velocityThreshold = 0.1f

    // Node states
    val nodeStates = remember { mutableStateMapOf<Any, BubbleNode>() }
    var physicsActive by remember { mutableStateOf(true) }

    // Drag tracking at parent level
    var draggedNodeKey by remember { mutableStateOf<Any?>(null) }

    BoxWithConstraints(modifier = modifier) {
        val centerX = constraints.maxWidth / 2f
        val centerY = constraints.maxHeight / 2f

        val hexRadius = itemSizePx * 0.58f
        val sqrt3 = sqrt(3f)

        // Initialize nodes
        LaunchedEffect(items, centerX, centerY) {
            val currentKeys = items.map { key(it) }.toSet()
            nodeStates.keys.filter { it !in currentKeys }.forEach { nodeStates.remove(it) }

            items.forEachIndexed { index, item ->
                val nodeKey = key(item)
                if (nodeKey !in nodeStates) {
                    val (hx, hy) = indexToHexCoord(index)
                    val px = centerX + hexRadius * sqrt3 * (hx + hy / 2f)
                    val py = centerY + hexRadius * 1.5f * hy
                    nodeStates[nodeKey] = BubbleNode(px, py)
                }
            }
            physicsActive = true
        }

        // Physics simulation
        LaunchedEffect(Unit) {
            while (isActive) {
                withFrameMillis { _ ->
                    if (!physicsActive && nodeStates.values.none { it.isDragging }) {
                        return@withFrameMillis
                    }

                    val nodes = nodeStates.values.toList()
                    val dt = 0.016f
                    var maxVelocity = 0f
                    var hasOverlap = false

                    for (node in nodes) {
                        if (node.isDragging) continue

                        var fx = 0f
                        var fy = 0f

                        for (other in nodes) {
                            if (node === other) continue

                            val dx = node.x - other.x
                            val dy = node.y - other.y
                            val distSq = dx * dx + dy * dy
                            val dist = sqrt(distSq).coerceAtLeast(1f)

                            if (dist < minSeparation) {
                                hasOverlap = true
                                val overlap = minSeparation - dist
                                val force = repulsionStrength * (overlap / minSeparation) / dist
                                fx += (dx / dist) * force
                                fy += (dy / dist) * force
                            }
                        }

                        val homeDistX = node.homeX - node.x
                        val homeDistY = node.homeY - node.y
                        val homeDist = sqrt(homeDistX * homeDistX + homeDistY * homeDistY)

                        if (homeDist > 5f) {
                            fx += homeDistX * homeAttractionStrength
                            fy += homeDistY * homeAttractionStrength
                        }

                        node.vx = (node.vx + fx * dt) * damping
                        node.vy = (node.vy + fy * dt) * damping

                        maxVelocity = maxOf(maxVelocity, sqrt(node.vx * node.vx + node.vy * node.vy))

                        node.x += node.vx
                        node.y += node.vy
                    }

                    if (maxVelocity < velocityThreshold && !hasOverlap) {
                        physicsActive = false
                    }
                }
            }
        }

        // Handle external zoom
        LaunchedEffect(externalZoomDelta) {
            if (externalZoomDelta != 0) {
                val newIndex = (zoomIndex + externalZoomDelta).coerceIn(0, zoomLevels.lastIndex)
                if (newIndex != zoomIndex) {
                    zoomIndex = newIndex
                    animatedScale.animateTo(
                        zoomLevels[zoomIndex],
                        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                    )
                }
                onZoomDeltaConsumed()
            }
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                // Handle all item gestures at parent level (stable coordinates)
                .pointerInput(items, nodeStates) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val downTime = System.currentTimeMillis()

                        // Transform touch point to content coordinates (account for pan/zoom)
                        val currentScale = animatedScale.value
                        val contentX = (down.position.x - panOffset.x - centerX) / currentScale + centerX
                        val contentY = (down.position.y - panOffset.y - centerY) / currentScale + centerY

                        // Hit test nodes
                        val hitEntry = nodeStates.entries.firstOrNull { (_, node) ->
                            val dx = contentX - node.x
                            val dy = contentY - node.y
                            dx * dx + dy * dy <= nodeRadius * nodeRadius
                        }

                        if (hitEntry != null) {
                            val (nodeKey, node) = hitEntry
                            val item = items.find { key(it) == nodeKey }

                            var wasDrag = false
                            var wasLongPress = false
                            var lastTapTime by mutableLongStateOf(0L)

                            // Anchor: offset from touch to node center (in content coords)
                            var anchorX = node.x - contentX
                            var anchorY = node.y - contentY

                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Main)
                                val change = event.changes.find { it.id == down.id }

                                if (change == null || !change.pressed) {
                                    // Released
                                    node.isDragging = false
                                    draggedNodeKey = null

                                    if (!wasDrag && !wasLongPress && item != null) {
                                        val now = System.currentTimeMillis()
                                        if (useDoubleTapForOptions && now - lastTapTime < 300L) {
                                            onItemLongClick(item) // Double-tap -> options
                                            lastTapTime = 0L
                                        } else {
                                            lastTapTime = now
                                            onItemClick(item)
                                        }
                                    }
                                    break
                                }

                                val dist = (change.position - down.position).getDistance()
                                val elapsed = System.currentTimeMillis() - downTime

                                // Long press detection
                                if (!wasLongPress && !wasDrag && elapsed > longPressTimeout && dist < touchSlop) {
                                    wasLongPress = true
                                    if (!useDoubleTapForOptions) {
                                        item?.let { onItemLongClick(it) }
                                    }
                                }

                                // Drag start detection
                                if (!wasDrag && dist > touchSlop) {
                                    wasDrag = true
                                    node.isDragging = true
                                    node.vx = 0f
                                    node.vy = 0f
                                    draggedNodeKey = nodeKey
                                    physicsActive = true

                                    // Recalculate anchor at drag start for accuracy
                                    val startScale = animatedScale.value
                                    val startContentX = (change.position.x - panOffset.x - centerX) / startScale + centerX
                                    val startContentY = (change.position.y - panOffset.y - centerY) / startScale + centerY
                                    anchorX = node.x - startContentX
                                    anchorY = node.y - startContentY
                                }

                                // Update position while dragging
                                if (wasDrag) {
                                    val scale = animatedScale.value
                                    val newContentX = (change.position.x - panOffset.x - centerX) / scale + centerX
                                    val newContentY = (change.position.y - panOffset.y - centerY) / scale + centerY
                                    node.x = newContentX + anchorX
                                    node.y = newContentY + anchorY
                                    change.consume()
                                }
                            }
                        }
                    }
                }
                // Pan and zoom gestures (only when not dragging an item)
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        if (draggedNodeKey != null) return@detectTransformGestures

                        panOffset += pan

                        if (zoom != 1f) {
                            val oldScale = animatedScale.value
                            val newScale = (oldScale * zoom).coerceIn(minScale, maxScale)
                            val scaleChange = newScale / oldScale
                            panOffset = Offset(
                                panOffset.x * scaleChange + centroid.x * (1 - scaleChange),
                                panOffset.y * scaleChange + centroid.y * (1 - scaleChange)
                            )
                            scope.launch { animatedScale.snapTo(newScale) }
                            zoomIndex = zoomLevels.indices.minBy { abs(zoomLevels[it] - newScale) }
                        }
                    }
                }
                // Double-tap to cycle zoom (on background)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { tapOffset ->
                            if (draggedNodeKey != null) return@detectTapGestures

                            val oldScale = animatedScale.value
                            zoomIndex = (zoomIndex + 1) % zoomLevels.size
                            val newScale = zoomLevels[zoomIndex]
                            val scaleChange = newScale / oldScale
                            panOffset = Offset(
                                panOffset.x * scaleChange + tapOffset.x * (1 - scaleChange),
                                panOffset.y * scaleChange + tapOffset.y * (1 - scaleChange)
                            )
                            scope.launch {
                                animatedScale.animateTo(
                                    newScale,
                                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                                )
                            }
                        }
                    )
                }
                .graphicsLayer {
                    scaleX = animatedScale.value
                    scaleY = animatedScale.value
                    translationX = panOffset.x
                    translationY = panOffset.y
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
                }
        ) {
            // Render items (no individual gesture handling)
            items.forEach { item ->
                val nodeKey = key(item)
                val node = nodeStates[nodeKey] ?: return@forEach

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (node.x - nodeRadius).roundToInt(),
                                (node.y - nodeRadius).roundToInt()
                            )
                        }
                        .size(itemSizeDp.dp),
                    contentAlignment = Alignment.Center
                ) {
                    itemContent(item)
                }
            }
        }
    }
}

private fun indexToHexCoord(index: Int): Pair<Int, Int> {
    if (index == 0) return Pair(0, 0)

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

    val directions = listOf(
        Pair(1, 0), Pair(0, 1), Pair(-1, 1),
        Pair(-1, 0), Pair(0, -1), Pair(1, -1)
    )

    var q = ring
    var r = -ring

    for (s in 0 until side) {
        q += directions[s].first * sideLength
        r += directions[s].second * sideLength
    }

    q += directions[side].first * posOnSide
    r += directions[side].second * posOnSide

    return Pair(q, r)
}