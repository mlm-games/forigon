package app.forigon.ui.screens

import android.view.SoundEffectConstants
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.forigon.LauncherViewModel
import app.forigon.data.AppModel
import app.forigon.settings.AppDrawerStyle
import app.forigon.settings.AppOptionsGesture
import app.forigon.ui.AppOptionsDialog
import app.forigon.ui.components.BezelCapturedRingIndicator
import app.forigon.ui.components.BubbleCloudLayout
import app.forigon.ui.components.WatchAppList
import app.forigon.ui.components.virtualRotaryDetents
import app.forigon.ui.theme.LauncherColors
import app.forigon.ui.theme.WatchSizes
import kotlin.math.abs

@Composable
fun WatchAppDrawer(
    viewModel: LauncherViewModel,
) {
    val apps by viewModel.apps.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val view = LocalView.current

    var selectedApp by remember { mutableStateOf<AppModel?>(null) }

    // Detent accumulator from virtual bezel
    var pendingScrollDetents by remember { mutableIntStateOf(0) }
    var pendingZoomDetents by remember { mutableIntStateOf(0) }

    // Bezel capture state for ring indicator
    var bezelActive by remember { mutableStateOf(false) }

    val bezelEnabled = settings.enableVirtualBezel
    val invert = settings.bezelInvertDirection
    val isBubbleMode = settings.appDrawerStyle == AppDrawerStyle.Bubble

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .virtualRotaryDetents(
                enabled = bezelEnabled,
                edgeThresholdFraction = settings.bezelEdgeThresholdFraction,
                stickyInnerFraction = settings.bezelStickyInnerFraction,
                detentDegrees = settings.bezelDetentDegrees,
                onActiveChanged = { active -> bezelActive = active }
            ) { steps ->
                val s = if (invert) -steps else steps

                if (isBubbleMode) {
                    pendingZoomDetents += s
                } else {
                    pendingScrollDetents += s
                }

                val n = abs(steps)
                repeat(n) {
                    if (settings.bezelHaptics) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    if (settings.bezelSound) {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                    }
                }
            }
    ) {
        when {
            apps.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading...", color = Color.Gray)
                }
            }

            isBubbleMode -> {
                BubbleCloudLayout(
                    items = apps,
                    modifier = Modifier.fillMaxSize(),
                    itemSizeDp = 70,
                    externalZoomDelta = pendingZoomDetents,
                    onZoomDeltaConsumed = { pendingZoomDetents = 0 },
                    key = { it.appPackage },
                    onItemClick = { app ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.launch(app)
                    },
                    onItemLongClick = { app ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedApp = app
                    },
                    useDoubleTapForOptions = settings.appOptionsGesture == AppOptionsGesture.DoubleTap
                ) { app ->
                    WatchBubbleItemContent(app = app)
                }
            }

            else -> {
                WatchAppList(
                    apps = apps,
                    onAppClick = { app ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.launch(app)
                    },
                    onAppLongClick = { app ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedApp = app
                    },
                    externalDetents = pendingScrollDetents,
                    onDetentsConsumed = { pendingScrollDetents = 0 },
                    bezelScrollMode = settings.bezelScrollMode,
                    bezelScrollPixelsPerDetent = settings.bezelScrollPixelsPerDetent,
                    bezelScrollItemsPerDetent = settings.bezelScrollItemsPerDetent,
                    optionsGesture = settings.appOptionsGesture
                )
            }
        }

        BezelCapturedRingIndicator(
            active = bezelActive,
            modifier = Modifier.fillMaxSize()
        )
    }

    selectedApp?.let { app ->
        AppOptionsDialog(
            context = context,
            app = app,
            isHidden = app.isHidden,
            onDismiss = { selectedApp = null },
            onOpen = { viewModel.launch(app) },
            onToggleHidden = { viewModel.toggleHidden(app) }
        )
    }
}

/**
 * Content-only composable for bubble items (no gesture handling - that's done by the layout)
 */
@Composable
private fun BoxScope.WatchBubbleItemContent(app: AppModel) {
    DisableSelection {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.matchParentSize()
        ) {
            Box(
                modifier = Modifier
                    .size(WatchSizes.bubbleSize)
                    .clip(CircleShape)
                    .background(LauncherColors.DarkSurface),
                contentAlignment = Alignment.Center
            ) {
                if (app.appIcon != null) {
                    Image(
                        bitmap = app.appIcon,
                        contentDescription = app.appLabel,
                        modifier = Modifier
                            .size(WatchSizes.bubbleIconSize)
                            .clip(CircleShape)
                    )
                } else {
                    Text(
                        text = app.appLabel.take(1).uppercase(),
                        color = Color.White,
                        fontSize = WatchSizes.titleSize
                    )
                }
            }

            Text(
                text = app.appLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = WatchSizes.bubbleLabelSize,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .widthIn(max = WatchSizes.bubbleSize + 8.dp)
            )
        }
    }
}