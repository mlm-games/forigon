package app.forigon.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.forigon.LauncherViewModel
import app.forigon.data.AppModel
import app.forigon.settings.AppDrawerStyle
import app.forigon.ui.AppOptionsDialog
import app.forigon.ui.components.BubbleCloudLayout
import app.forigon.ui.components.WatchAppList
import app.forigon.ui.theme.LauncherColors
import app.forigon.ui.theme.WatchSizes

@Composable
fun WatchAppDrawer(
    viewModel: LauncherViewModel,
    externalScrollDelta: Float = 0f,
    onScrollConsumed: () -> Unit = {}
) {
    val apps by viewModel.apps.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    // Dialog state
    var selectedApp by remember { mutableStateOf<AppModel?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
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
                // Default: List style
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
                    externalScrollDelta = externalScrollDelta,
                    onScrollConsumed = onScrollConsumed
                )
            }
        }
    }

    // App options dialog
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
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .size(WatchSizes.bubbleSize + 14.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
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