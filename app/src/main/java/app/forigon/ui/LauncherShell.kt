package app.forigon.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.forigon.LauncherViewModel
import app.forigon.ui.components.virtualRotaryInput
import app.forigon.ui.screens.*
import app.forigon.ui.theme.LauncherTheme
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

enum class VerticalScreenState {
    CONTROL_CENTER,
    HOME,
    APP_DRAWER
}

@Composable
fun LauncherShell(viewModel: LauncherViewModel) {
    LauncherTheme {
        val density = LocalDensity.current
        val haptic = LocalHapticFeedback.current
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        val screenHeightPx = with(density) { screenHeight.toPx() }

        var verticalState by remember { mutableStateOf(VerticalScreenState.HOME) }
        var dragOffset by remember { mutableFloatStateOf(0f) }
        var showSettings by remember { mutableStateOf(false) }

        // Accumulated bezel scroll delta for app drawer
        var bezelScrollDelta by remember { mutableFloatStateOf(0f) }

        val targetOffset = when (verticalState) {
            VerticalScreenState.CONTROL_CENTER -> screenHeightPx
            VerticalScreenState.HOME -> 0f
            VerticalScreenState.APP_DRAWER -> -screenHeightPx
        }

        val animatedOffset by animateFloatAsState(
            targetValue = if (dragOffset != 0f) dragOffset else targetOffset,
            animationSpec = tween(250),
            label = "vertical_nav"
        )

        BackHandler {
            when {
                showSettings -> showSettings = false
                verticalState != VerticalScreenState.HOME -> {
                    verticalState = VerticalScreenState.HOME
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                // Virtual bezel - Test once, and
                .virtualRotaryInput(
                    edgeThresholdFraction = 0.18f
                ) { delta ->
                    when (verticalState) {
                        VerticalScreenState.APP_DRAWER -> {
                            bezelScrollDelta += delta
                            if (delta.absoluteValue > 3f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                        VerticalScreenState.HOME -> {
                            // Could use bezel to switch screens
                            // For now, accumulate and switch if threshold met
                            bezelScrollDelta += delta
                            if (bezelScrollDelta > 200f) {
                                verticalState = VerticalScreenState.CONTROL_CENTER
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                bezelScrollDelta = 0f
                            } else if (bezelScrollDelta < -200f) {
                                verticalState = VerticalScreenState.APP_DRAWER
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                bezelScrollDelta = 0f
                            }
                        }
                        VerticalScreenState.CONTROL_CENTER -> {
                            // Scroll down to go back to home
                            if (delta < -50f) {
                                verticalState = VerticalScreenState.HOME
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    }
                }
                // Vertical swipe - handles center screen drags
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            dragOffset = 0f
                        },
                        onDragEnd = {
                            val threshold = screenHeightPx * 0.15f
                            val previousState = verticalState

                            when (verticalState) {
                                VerticalScreenState.HOME -> {
                                    if (dragOffset > threshold) {
                                        verticalState = VerticalScreenState.CONTROL_CENTER
                                    } else if (dragOffset < -threshold) {
                                        verticalState = VerticalScreenState.APP_DRAWER
                                    }
                                }
                                VerticalScreenState.CONTROL_CENTER -> {
                                    if (dragOffset < -threshold) {
                                        verticalState = VerticalScreenState.HOME
                                    }
                                }
                                VerticalScreenState.APP_DRAWER -> {
                                    if (dragOffset > threshold) {
                                        verticalState = VerticalScreenState.HOME
                                    }
                                }
                            }

                            if (verticalState != previousState) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            dragOffset = 0f
                        },
                        onDragCancel = {
                            dragOffset = 0f
                        }
                    ) { change, dragAmount ->
                        change.consume()

                        // Apply drag based on current state
                        val newOffset = when (verticalState) {
                            VerticalScreenState.HOME -> dragOffset + dragAmount
                            VerticalScreenState.CONTROL_CENTER -> {
                                // Only allow dragging down (negative) to go back to home
                                (dragOffset + dragAmount).coerceAtMost(0f)
                            }
                            VerticalScreenState.APP_DRAWER -> {
                                // Only allow dragging up (positive) to go back to home
                                (dragOffset + dragAmount).coerceAtLeast(0f)
                            }
                        }
                        dragOffset = newOffset
                    }
                }
        ) {
            // Control Center (above home)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(0, (animatedOffset - screenHeightPx).roundToInt()) }
            ) {
                ControlCenter(onSettingsClick = { showSettings = true })
            }

            // Home / Watch Face (center)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(0, animatedOffset.roundToInt()) }
            ) {
                WatchFaceScreen(
                    onAppDrawerClick = {
                        verticalState = VerticalScreenState.APP_DRAWER
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onSettingsClick = {
                        verticalState = VerticalScreenState.CONTROL_CENTER
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                )
            }

            // App Drawer (below home)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(0, (animatedOffset + screenHeightPx).roundToInt()) }
            ) {
                WatchAppDrawer(
                    viewModel = viewModel,
                    externalScrollDelta = bezelScrollDelta,
                    onScrollConsumed = { bezelScrollDelta = 0f }
                )
            }

            // Settings overlay
            AnimatedVisibility(
                visible = showSettings,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.fillMaxSize()
            ) {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { showSettings = false }
                )
            }
        }
    }
}