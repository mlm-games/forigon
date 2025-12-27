package app.forigon.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.forigon.LauncherViewModel
import app.forigon.data.AppModel
import app.forigon.settings.AppDrawerStyle
import app.forigon.settings.AppOptionsGesture
import app.forigon.ui.AppOptionsDialog
import app.forigon.ui.components.BubbleCloudLayout
import app.forigon.ui.components.WatchAppList
import app.forigon.ui.components.virtualRotaryDetents
import app.forigon.ui.theme.LauncherColors
import app.forigon.ui.theme.WatchSizes

@Composable
fun WatchAppDrawer(
    viewModel: LauncherViewModel,
) {
    val apps by viewModel.apps.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    var selectedApp by remember { mutableStateOf<AppModel?>(null) }

    // Detent accumulator from virtual bezel
    var pendingDetents by remember { mutableIntStateOf(0) }

    val bezelEnabled = settings.enableVirtualBezel
    val invert = settings.bezelInvertDirection

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .virtualRotaryDetents(
                enabled = bezelEnabled,
                edgeThresholdFraction = settings.bezelEdgeThresholdFraction,
                stickyInnerFraction = settings.bezelStickyInnerFraction,
                detentDegrees = settings.bezelDetentDegrees
            ) { steps ->
                val s = if (invert) -steps else steps
                pendingDetents += s

                if (settings.bezelHaptics) {
                    // one haptic "tick" per event batch (not per step) to avoid spam
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
    ) {
        when {
            apps.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading...", color = Color.Gray)
                }
            }

            settings.appDrawerStyle == AppDrawerStyle.Bubble -> {
                BubbleCloudLayout(
                    modifier = Modifier.fillMaxSize(),
                    itemSizeDp = 70
                ) {
                    apps.forEach { app ->
                        WatchBubbleItem(
                            app = app,
                            optionsGesture = settings.appOptionsGesture,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.launch(app)
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedApp = app
                            }
                        )
                    }
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
                    externalDetents = pendingDetents,
                    onDetentsConsumed = { pendingDetents = 0 },
                    bezelScrollMode = settings.bezelScrollMode,
                    bezelScrollPixelsPerDetent = settings.bezelScrollPixelsPerDetent,
                    bezelScrollItemsPerDetent = settings.bezelScrollItemsPerDetent,
                    optionsGesture = settings.appOptionsGesture
                )
            }
        }
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

@Composable
private fun WatchBubbleItem(
    app: AppModel,
    optionsGesture: AppOptionsGesture,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val clickMod = when (optionsGesture) {
        AppOptionsGesture.LongPress -> {
            Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            }
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .size(WatchSizes.bubbleSize + 14.dp)
                .then(clickMod)
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