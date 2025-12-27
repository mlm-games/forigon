package app.forigon.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.forigon.LauncherViewModel
import app.forigon.helper.getScreenDimensions
import app.forigon.settings.*
import app.forigon.ui.theme.LauncherColors
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    viewModel: LauncherViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    val uiScaleOptions = remember { listOf(0.90f, 1.00f, 1.10f, 1.20f, 1.30f, 1.40f, 1.50f, 1.60f) }
    val animSpeedOptions = remember { listOf(0.75f, 1.0f, 1.25f, 1.5f) }
    val edgeOptions = remember { listOf(0.20f, 0.25f, 0.30f, 0.35f, 0.40f) }
    val stickyOptions = remember { listOf(0.55f, 0.60f, 0.70f, 0.80f) }
    val detentOptions = remember { listOf(10f, 15f, 20f, 30f) }
    val pxPerDetentOptions = remember { listOf(18f, 24f, 28f, 36f, 48f, 64f) }
    val itemsPerDetentOptions = remember { listOf(1, 2, 3, 5) }

    val (wPx, hPx) = remember { getScreenDimensions(context) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    "Settings",
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        containerColor = Color.Black
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp)
        ) {
            item { SectionHeader("Display") }

            item {
                val current = uiScaleOptions.minBy { abs(it - settings.uiScale) }
                val next = uiScaleOptions[(uiScaleOptions.indexOf(current) + 1) % uiScaleOptions.size]
                SettingsActionItem(
                    title = "UI Scale",
                    subtitle = "${(current * 100).roundToInt()}%",
                    onClick = { viewModel.updateUiScale(next) }
                )
            }

            item {
                SettingsToggleItem(
                    title = "Touch Target Boost",
                    checked = settings.touchTargetBoost,
                    onCheckedChange = { viewModel.updateTouchTargetBoost(it) }
                )
            }

            item { SectionHeader("Motion") }

            item {
                val label = when (settings.motionMode) {
                    MotionMode.Full -> "Full"
                    MotionMode.Reduced -> "Reduced"
                    MotionMode.Off -> "Off"
                }
                SettingsActionItem(
                    title = "Motion Mode",
                    subtitle = label,
                    onClick = {
                        val next = when (settings.motionMode) {
                            MotionMode.Full -> MotionMode.Reduced
                            MotionMode.Reduced -> MotionMode.Off
                            MotionMode.Off -> MotionMode.Full
                        }
                        viewModel.updateMotionMode(next)
                    }
                )
            }

            item {
                val current = animSpeedOptions.minBy { abs(it - settings.animationSpeed) }
                val next = animSpeedOptions[(animSpeedOptions.indexOf(current) + 1) % animSpeedOptions.size]
                SettingsActionItem(
                    title = "Animation Speed",
                    subtitle = if (settings.motionMode == MotionMode.Full) "${current}x" else "Disabled in ${settings.motionMode}",
                    onClick = {
                        if (settings.motionMode == MotionMode.Full) viewModel.updateAnimationSpeed(next)
                    }
                )
            }

            item { SectionHeader("Apps") }

            item {
                SettingsToggleItem(
                    title = "Show Icons",
                    checked = settings.showAppIcons,
                    onCheckedChange = { viewModel.updateShowAppIcons(it) }
                )
            }

            item {
                val styleLabel = when (settings.appDrawerStyle) {
                    AppDrawerStyle.List -> "List"
                    AppDrawerStyle.Bubble -> "Bubble Cloud"
                }
                SettingsActionItem(
                    title = "App Drawer Style",
                    subtitle = styleLabel,
                    onClick = {
                        val next = when (settings.appDrawerStyle) {
                            AppDrawerStyle.List -> AppDrawerStyle.Bubble
                            AppDrawerStyle.Bubble -> AppDrawerStyle.List
                        }
                        viewModel.updateAppDrawerStyle(next)
                    }
                )
            }

            item { SectionHeader("Input") }

            item {
                val label = when (settings.appOptionsGesture) {
                    AppOptionsGesture.LongPress -> "Long-press"
                    AppOptionsGesture.DoubleTap -> "Double-tap"
                }
                SettingsActionItem(
                    title = "App Options Gesture",
                    subtitle = label,
                    onClick = {
                        val next = when (settings.appOptionsGesture) {
                            AppOptionsGesture.LongPress -> AppOptionsGesture.DoubleTap
                            AppOptionsGesture.DoubleTap -> AppOptionsGesture.LongPress
                        }
                        viewModel.updateAppOptionsGesture(next)
                    }
                )
            }

            item { SectionHeader("Virtual Bezel") }

            item {
                SettingsToggleItem(
                    title = "Enable Virtual Bezel",
                    checked = settings.enableVirtualBezel,
                    onCheckedChange = { viewModel.updateVirtualBezelEnabled(it) }
                )
            }

            item {
                SettingsToggleItem(
                    title = "Invert Direction",
                    checked = settings.bezelInvertDirection,
                    onCheckedChange = { viewModel.updateBezelInvertDirection(it) }
                )
            }

            item {
                SettingsToggleItem(
                    title = "Haptics on Detent",
                    checked = settings.bezelHaptics,
                    onCheckedChange = { viewModel.updateBezelHaptics(it) }
                )
            }

            item {
                val current = edgeOptions.minBy { abs(it - settings.bezelEdgeThresholdFraction) }
                val next = edgeOptions[(edgeOptions.indexOf(current) + 1) % edgeOptions.size]
                SettingsActionItem(
                    title = "Edge Ring Thickness",
                    subtitle = "${(current * 100).roundToInt()}% outer radius",
                    onClick = { viewModel.updateBezelEdgeThresholdFraction(next) }
                )
            }

            item {
                val current = stickyOptions.minBy { abs(it - settings.bezelStickyInnerFraction) }
                val next = stickyOptions[(stickyOptions.indexOf(current) + 1) % stickyOptions.size]
                SettingsActionItem(
                    title = "Sticky Inner Radius",
                    subtitle = "${(current * 100).roundToInt()}% radius",
                    onClick = { viewModel.updateBezelStickyInnerFraction(next) }
                )
            }

            item {
                val current = detentOptions.minBy { abs(it - settings.bezelDetentDegrees) }
                val next = detentOptions[(detentOptions.indexOf(current) + 1) % detentOptions.size]
                SettingsActionItem(
                    title = "Detent Degrees",
                    subtitle = "${current}°",
                    onClick = { viewModel.updateBezelDetentDegrees(next) }
                )
            }

            item {
                val label = when (settings.bezelScrollMode) {
                    BezelScrollMode.Items -> "Items"
                    BezelScrollMode.Pixels -> "Pixels"
                }
                SettingsActionItem(
                    title = "Scroll Mode",
                    subtitle = label,
                    onClick = {
                        val next = when (settings.bezelScrollMode) {
                            BezelScrollMode.Items -> BezelScrollMode.Pixels
                            BezelScrollMode.Pixels -> BezelScrollMode.Items
                        }
                        viewModel.updateBezelScrollMode(next)
                    }
                )
            }

            item {
                when (settings.bezelScrollMode) {
                    BezelScrollMode.Items -> {
                        val current = itemsPerDetentOptions.minBy { abs(it - settings.bezelScrollItemsPerDetent) }
                        val next = itemsPerDetentOptions[(itemsPerDetentOptions.indexOf(current) + 1) % itemsPerDetentOptions.size]
                        SettingsActionItem(
                            title = "Items per Detent",
                            subtitle = "$current",
                            onClick = { viewModel.updateBezelScrollItemsPerDetent(next) }
                        )
                    }
                    BezelScrollMode.Pixels -> {
                        val current = pxPerDetentOptions.minBy { abs(it - settings.bezelScrollPixelsPerDetent) }
                        val next = pxPerDetentOptions[(pxPerDetentOptions.indexOf(current) + 1) % pxPerDetentOptions.size]
                        SettingsActionItem(
                            title = "Pixels per Detent",
                            subtitle = "${current.roundToInt()} px",
                            onClick = { viewModel.updateBezelScrollPixelsPerDetent(next) }
                        )
                    }
                }
            }

            item {
                SettingsToggleItem(
                    title = "Bezel Click Sound",
                    checked = settings.bezelSound,
                    onCheckedChange = { viewModel.updateBezelSound(it) }
                )
            }

            item { SectionHeader("Sorting") }

            item {
                val sortLabel = when (settings.sortOrder) {
                    SortOrder.AZ -> "A-Z"
                    SortOrder.ZA -> "Z-A"
                    SortOrder.Recent -> "Recent"
                }
                SettingsActionItem(
                    title = "Sort Order",
                    subtitle = sortLabel,
                    onClick = {
                        val next = when (settings.sortOrder) {
                            SortOrder.AZ -> SortOrder.Recent
                            SortOrder.Recent -> SortOrder.ZA
                            SortOrder.ZA -> SortOrder.AZ
                        }
                        viewModel.updateSortOrder(next)
                    }
                )
            }

            item { SectionHeader("Device") }

            item {
                DeviceInfoItem("Brand", Build.BRAND)
                DeviceInfoItem("Model", Build.MODEL)
                DeviceInfoItem("Android", "${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
                DeviceInfoItem("Resolution", "${wPx}x${hPx} px")
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Forigon v1.0",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = LauncherColors.AccentBlue,
        fontSize = 12.sp,
        modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsToggleItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = LauncherColors.AccentBlue,
                checkedTrackColor = LauncherColors.AccentBlue.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun SettingsActionItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = title, color = Color.White, fontSize = 16.sp)
        Text(text = subtitle, color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
private fun DeviceInfoItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 13.sp)
        Text(text = value, color = Color.White, fontSize = 13.sp)
    }
}