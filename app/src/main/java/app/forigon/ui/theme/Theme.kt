package app.forigon.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.forigon.settings.LauncherSettings
import app.forigon.settings.MotionMode
import kotlin.math.roundToInt

object LauncherColors {
    val DarkBackground = Color(0xFF0D0D0D)
    val DarkSurface = Color(0xFF1A1A1A)
    val DarkSurfaceVariant = Color(0xFF2D2D2D)
    val DarkCardBackground = Color(0xFF1E1E1E)

    val AccentBlue = Color(0xFF4A9EFF)
    val AccentPurple = Color(0xFF9D4EDD)
    val AccentTeal = Color(0xFF00BFA5)
    val AccentOrange = Color(0xFFFF6D00)

    val FocusRing = Color(0xFFFFFFFF)
    val FocusGlow = Color(0x40FFFFFF)

    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB3B3B3)
    val TextTertiary = Color(0xFF666666)

    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
    val Error = Color(0xFFE53935)
}

object LauncherSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val xxxl = 64.dp

    val screenPadding = 48.dp
    val sidebarWidth = 80.dp
    val sidebarExpandedWidth = 280.dp
    val cardGap = 16.dp
    val rowGap = 32.dp
    val sectionGap = 48.dp
}

object LauncherCardSizes {
    val appCardWidth = 160.dp
    val appCardHeight = 200.dp
    val bannerCardWidth = 320.dp
    val bannerCardHeight = 180.dp
    val wideCardWidth = 340.dp
    val wideCardHeight = 120.dp
    val smallCardSize = 80.dp

    val appIconLarge = 64.dp
    val appIconMedium = 48.dp
    val appIconSmall = 32.dp
}

data class LauncherMotion(
    val fastMs: Int,
    val normalMs: Int,
    val slowMs: Int,
    val enableInfiniteEffects: Boolean
)

val LocalLauncherMotion = staticCompositionLocalOf {
    LauncherMotion(fastMs = 150, normalMs = 250, slowMs = 400, enableInfiniteEffects = true)
}

private fun defaultTypography() = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 48.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 36.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 28.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 24.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 20.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, letterSpacing = 0.25.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, letterSpacing = 0.1.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.5.sp),
)

data class LauncherDimens(
    val screenPadding: Dp = LauncherSpacing.screenPadding,
    val sidebarWidth: Dp = LauncherSpacing.sidebarWidth,
    val sidebarExpandedWidth: Dp = LauncherSpacing.sidebarExpandedWidth,
    val cardWidth: Dp = LauncherCardSizes.appCardWidth,
    val cardHeight: Dp = LauncherCardSizes.appCardHeight
)

val LocalLauncherDimens = staticCompositionLocalOf { LauncherDimens() }

private val DarkColorScheme = darkColorScheme(
    primary = LauncherColors.AccentBlue,
    onPrimary = Color.White,
    primaryContainer = LauncherColors.AccentBlue.copy(alpha = 0.2f),
    onPrimaryContainer = LauncherColors.AccentBlue,
    secondary = LauncherColors.AccentPurple,
    onSecondary = Color.White,
    secondaryContainer = LauncherColors.AccentPurple.copy(alpha = 0.2f),
    onSecondaryContainer = LauncherColors.AccentPurple,
    tertiary = LauncherColors.AccentTeal,
    onTertiary = Color.White,
    background = LauncherColors.DarkBackground,
    onBackground = LauncherColors.TextPrimary,
    surface = LauncherColors.DarkSurface,
    onSurface = LauncherColors.TextPrimary,
    surfaceVariant = LauncherColors.DarkSurfaceVariant,
    onSurfaceVariant = LauncherColors.TextSecondary,
    error = LauncherColors.Error,
    onError = Color.White
)

@Composable
fun LauncherTheme(
    settings: LauncherSettings,
    content: @Composable () -> Unit
) {
    val base = LocalDensity.current

    val uiScale = settings.uiScale.coerceIn(0.75f, 2.0f)
    val touchBoost = if (settings.touchTargetBoost) 1.15f else 1.0f

    // dpScale affects dp sizes; fontScale affects sp sizes.
    val dpScale = uiScale * touchBoost
    val spScale = uiScale

    val scaledDensity = remember(base, dpScale, spScale) {
        Density(
            density = base.density * dpScale,
            fontScale = base.fontScale * spScale
        )
    }

    val motion = remember(settings.motionMode, settings.animationSpeed) {
        when (settings.motionMode) {
            MotionMode.Off -> LauncherMotion(0, 0, 0, enableInfiniteEffects = false)
            MotionMode.Reduced -> LauncherMotion(100, 150, 220, enableInfiniteEffects = false)
            MotionMode.Full -> {
                val speed = settings.animationSpeed.coerceIn(0.5f, 2.0f)
                fun scale(ms: Int): Int = (ms / speed).roundToInt().coerceAtLeast(1)
                LauncherMotion(scale(150), scale(250), scale(400), enableInfiniteEffects = true)
            }
        }
    }

    CompositionLocalProvider(
        LocalDensity provides scaledDensity,
        LocalLauncherMotion provides motion,
        LocalLauncherDimens provides LauncherDimens()
    ) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = defaultTypography(),
            content = content
        )
    }
}