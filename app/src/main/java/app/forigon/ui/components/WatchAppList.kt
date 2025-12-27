package app.forigon.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.forigon.data.AppModel
import app.forigon.settings.AppOptionsGesture
import app.forigon.settings.BezelScrollMode
import app.forigon.ui.theme.LauncherColors
import app.forigon.ui.theme.WatchSizes
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WatchAppList(
    apps: List<AppModel>,
    onAppClick: (AppModel) -> Unit,
    onAppLongClick: (AppModel) -> Unit,
    modifier: Modifier = Modifier,

    // Detents from bezel
    externalDetents: Int = 0,
    onDetentsConsumed: () -> Unit = {},

    // How detents map to scrolling
    bezelScrollMode: BezelScrollMode = BezelScrollMode.Items,
    bezelScrollPixelsPerDetent: Float = 28f,
    bezelScrollItemsPerDetent: Int = 1,

    // Gesture for options
    optionsGesture: AppOptionsGesture = AppOptionsGesture.LongPress,
) {
    val listState = rememberLazyListState()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val scope = rememberCoroutineScope()

    LaunchedEffect(externalDetents, apps.size, bezelScrollMode, bezelScrollItemsPerDetent, bezelScrollPixelsPerDetent) {
        if (externalDetents == 0 || apps.isEmpty()) return@LaunchedEffect

        scope.launch {
            when (bezelScrollMode) {
                BezelScrollMode.Items -> {
                    val deltaItems = externalDetents * bezelScrollItemsPerDetent
                    val target = (listState.firstVisibleItemIndex + deltaItems).coerceIn(0, apps.lastIndex)
                    listState.animateScrollToItem(target)
                }
                BezelScrollMode.Pixels -> {
                    // negative sign so "positive steps" can be tuned by invertDirection outside
                    listState.scrollBy(-externalDetents * bezelScrollPixelsPerDetent)
                }
            }
        }
        onDetentsConsumed()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = screenHeight / 3,
                bottom = screenHeight / 3,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(
                items = apps,
                key = { index, app -> "${app.getKey()}_$index" }
            ) { index, app ->
                WatchListItem(
                    app = app,
                    listState = listState,
                    index = index,
                    optionsGesture = optionsGesture,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick(app) }
                )
            }
        }

        if (apps.isNotEmpty()) {
            ScrollPositionIndicator(
                currentIndex = listState.firstVisibleItemIndex,
                totalItems = apps.size,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WatchListItem(
    app: AppModel,
    listState: androidx.compose.foundation.lazy.LazyListState,
    index: Int,
    optionsGesture: AppOptionsGesture,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val itemInfo = listState.layoutInfo.visibleItemsInfo.find { it.index == index }
    val centerOffset = if (itemInfo != null) {
        val itemCenter = itemInfo.offset + itemInfo.size / 2
        val screenCenter = listState.layoutInfo.viewportEndOffset / 2
        (itemCenter - screenCenter).toFloat() / screenCenter.coerceAtLeast(1)
    } else 1f

    val scale = (1f - abs(centerOffset) * 0.3f).coerceIn(0.7f, 1f)
    val alpha = (1f - abs(centerOffset) * 0.5f).coerceIn(0.4f, 1f)

    val clickMod = when (optionsGesture) {
        AppOptionsGesture.LongPress -> {
            Modifier.combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
        }
        AppOptionsGesture.DoubleTap -> {
            Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onDoubleTap = { onLongClick() }
                )
            }
        }
    }

    DisableSelection {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
                .clip(RoundedCornerShape(24.dp))
                .background(LauncherColors.DarkSurface.copy(alpha = 0.6f))
                .then(clickMod)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(WatchSizes.listIconSize)
                    .clip(CircleShape)
                    .background(LauncherColors.DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (app.appIcon != null) {
                    Image(
                        bitmap = app.appIcon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(WatchSizes.listIconSize - 4.dp)
                            .clip(CircleShape)
                    )
                } else {
                    androidx.compose.material3.Text(
                        text = app.appLabel.take(1).uppercase(),
                        color = Color.White,
                        fontSize = WatchSizes.bodySize,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            androidx.compose.material3.Text(
                text = app.appLabel,
                color = Color.White,
                fontSize = WatchSizes.bodySize,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (app.isNew) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(LauncherColors.AccentBlue)
                )
            }
        }
    }
}

@Composable
private fun ScrollPositionIndicator(
    currentIndex: Int,
    totalItems: Int,
    modifier: Modifier = Modifier
) {
    if (totalItems <= 1) return
    val progress = currentIndex.toFloat() / (totalItems - 1).coerceAtLeast(1)

    Box(
        modifier = modifier
            .width(4.dp)
            .height(60.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(LauncherColors.DarkSurfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .offset(y = (40.dp * progress))
                .clip(RoundedCornerShape(2.dp))
                .background(LauncherColors.AccentBlue)
        )
    }
}