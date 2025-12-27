package app.forigon.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.forigon.data.AppModel
import app.forigon.helper.getUserHandleFromString
import app.forigon.helper.openAppInfo
import app.forigon.helper.uninstall
import app.forigon.ui.theme.*

data class AppOption(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val iconTint: Color = LauncherColors.TextPrimary,
    val action: () -> Unit
)

@Composable
fun AppOptionsSheet(
    app: AppModel,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onToggleHidden: () -> Unit,
    onToggleFavorite: () -> Unit = {},
    isFavorite: Boolean = false,
    isHidden: Boolean = false
) {
    val context = LocalContext.current
    
    val options = remember(app, isFavorite, isHidden) {
        buildList {
            add(AppOption(
                id = "open",
                label = "Open",
                icon = Icons.Filled.PlayArrow,
                iconTint = LauncherColors.AccentBlue,
                action = onOpen
            ))
            
            add(AppOption(
                id = "favorite",
                label = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                icon = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                iconTint = if (isFavorite) LauncherColors.Error else LauncherColors.AccentOrange,
                action = onToggleFavorite
            ))
            
            add(AppOption(
                id = "info",
                label = "App Info",
                icon = Icons.Outlined.Info,
                action = {
                    val user = getUserHandleFromString(context, app.userString)
                    openAppInfo(context, user, app.appPackage)
                }
            ))
            
            add(AppOption(
                id = "hide",
                label = if (isHidden) "Unhide" else "Hide",
                icon = if (isHidden) Icons.Filled.Visibility else Icons.Outlined.VisibilityOff,
                action = onToggleHidden
            ))
            
            add(AppOption(
                id = "uninstall",
                label = "Uninstall",
                icon = Icons.Outlined.Delete,
                iconTint = LauncherColors.Error,
                action = { context.uninstall(app.appPackage) }
            ))
        }
    }
    
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                AppOptionsContent(
                    app = app,
                    options = options,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun AppOptionsContent(
    app: AppModel,
    options: List<AppOption>,
    onDismiss: () -> Unit
) {
    val focusRequesters = remember { options.map { FocusRequester() } }
    
    // Request focus on first item
    LaunchedEffect(Unit) {
        focusRequesters.firstOrNull()?.requestFocus()
    }
    
    Column(
        modifier = Modifier
            .width(400.dp)
            .shadow(24.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        LauncherColors.DarkSurface,
                        LauncherColors.DarkBackground
                    )
                )
            )
            .padding(LauncherSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = LauncherSpacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(
                app = app,
                size = LauncherCardSizes.appIconLarge,
                showShadow = true
            )
            
            Spacer(modifier = Modifier.width(LauncherSpacing.md))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appLabel,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    text = app.appPackage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LauncherColors.TextSecondary
                )
            }
        }
        
        Divider(
            color = LauncherColors.DarkSurfaceVariant,
            modifier = Modifier.padding(bottom = LauncherSpacing.md)
        )
        
        // Options
        Column(
            verticalArrangement = Arrangement.spacedBy(LauncherSpacing.xs)
        ) {
            options.forEachIndexed { index, option ->
                OptionItem(
                    option = option,
                    focusRequester = focusRequesters[index],
                    onAction = {
                        option.action()
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun OptionItem(
    option: AppOption,
    focusRequester: FocusRequester,
    onAction: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isFocused) 
            LauncherColors.AccentBlue.copy(alpha = 0.2f) 
        else 
            Color.Transparent,
        label = "option_bg"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "option_scale"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .then(
                if (isFocused) Modifier.border(
                    width = 2.dp,
                    color = LauncherColors.AccentBlue.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ) else Modifier
            )
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown &&
                    (event.key == Key.DirectionCenter || event.key == Key.Enter)
                ) {
                    onAction()
                    true
                } else false
            }
            .focusable()
            .padding(LauncherSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(option.iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = option.label,
                tint = option.iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(LauncherSpacing.md))
        
        Text(
            text = option.label,
            style = MaterialTheme.typography.titleMedium,
            color = if (isFocused) Color.White else LauncherColors.TextPrimary
        )
    }
}

@Composable
private fun Divider(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}