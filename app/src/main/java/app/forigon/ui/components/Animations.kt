package app.forigon.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import app.forigon.ui.theme.LocalLauncherMotion


@Composable
fun LauncherAnimatedVisibility(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    val motion = LocalLauncherMotion.current
    val d = motion.fastMs

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(d)) + expandVertically(tween(d)),
        exit = fadeOut(tween(d)) + shrinkVertically(tween(d))
    ) {
        content()
    }
}