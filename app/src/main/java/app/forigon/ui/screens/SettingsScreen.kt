package app.forigon.ui.screens

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.forigon.LauncherViewModel
import app.forigon.settings.AppDrawerStyle
import app.forigon.settings.SearchType
import app.forigon.settings.SortOrder
import app.forigon.ui.theme.LauncherColors

@Composable
fun SettingsScreen(
    viewModel: LauncherViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()

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
            // Appearance Section
            item { SectionHeader("Appearance") }

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

            // Sorting Section
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

            // Info
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, color = Color.White, fontSize = 16.sp)
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