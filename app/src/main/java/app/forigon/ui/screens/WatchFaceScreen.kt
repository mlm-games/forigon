package app.forigon.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.forigon.ui.theme.LauncherColors
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WatchFaceScreen(
    onAppDrawerClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var time by remember { mutableStateOf(System.currentTimeMillis()) }
    var batteryLevel by remember { mutableIntStateOf(100) }
    val context = LocalContext.current

    // Update time
    LaunchedEffect(Unit) {
        while (true) {
            time = System.currentTimeMillis()
            delay(1000)
        }
    }

    // Battery Monitor
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                if (level >= 0 && scale > 0) {
                    batteryLevel = (level * 100) / scale
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose { context.unregisterReceiver(receiver) }
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEE, MMM d", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(onClick = onAppDrawerClick), // Tap anywhere to open apps (or make specific button)
        contentAlignment = Alignment.Center
    ) {
        // Analog Seconds Indicator (Outer Ring)
        val infiniteTransition = rememberInfiniteTransition(label = "seconds")
        val angle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(60000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "seconds_angle"
        )
        
        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            drawCircle(
                color = LauncherColors.DarkSurfaceVariant,
                style = Stroke(width = 4.dp.toPx())
            )
            // Seconds dot
            val rad = Math.toRadians(angle.toDouble() - 90)
            val r = size.minDimension / 2
            drawCircle(
                color = LauncherColors.AccentBlue,
                radius = 6.dp.toPx(),
                center = center + Offset(
                    (r * cos(rad)).toFloat(),
                    (r * sin(rad)).toFloat()
                )
            )
        }

        // Digital Time & Date
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timeFormat.format(Date(time)),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Thin,
                    letterSpacing = (-2).sp
                ),
                color = Color.White
            )
            Text(
                text = dateFormat.format(Date(time)).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = LauncherColors.AccentBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Battery Pill
            Surface(
                color = LauncherColors.DarkSurfaceVariant,
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BatteryStd,
                        contentDescription = null,
                        tint = if(batteryLevel < 20) LauncherColors.Error else LauncherColors.Success,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "$batteryLevel%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}