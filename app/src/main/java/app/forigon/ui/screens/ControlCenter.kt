package app.forigon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.BrightnessHigh
import androidx.compose.material.icons.outlined.DoNotDisturb
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.forigon.platform.SettingsShortcuts
import app.forigon.ui.theme.LauncherColors

@Composable
fun ControlCenter(
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Quick Settings", color = Color.White, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { QuickTile(Icons.Filled.Wifi, "Wi-Fi", true) { SettingsShortcuts.openWifi(context) } }
            item { QuickTile(Icons.Filled.Bluetooth, "BT", false) { SettingsShortcuts.openBluetooth(context) } }
            item { QuickTile(Icons.Outlined.BrightnessHigh, "Bright", false) {} }
            item { QuickTile(Icons.Outlined.DoNotDisturb, "DND", false) {} }
            item { QuickTile(Icons.Outlined.FlashlightOn, "Torch", false) {} }
            item { QuickTile(Icons.Filled.Settings, "Settings", false) { onSettingsClick() } }
        }
    }
}

@Composable
fun QuickTile(icon: ImageVector, label: String, isActive: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(if (isActive) LauncherColors.AccentBlue else LauncherColors.DarkSurfaceVariant)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.White else Color.Gray
            )
        }
    }
}