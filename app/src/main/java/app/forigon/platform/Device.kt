package app.forigon.platform

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.view.WindowManager
import androidx.annotation.RequiresApi
import kotlin.math.min

object Device {

    fun isWatch(context: Context): Boolean {
        // Check for watch feature flags
        val pm = context.packageManager
        return pm.hasSystemFeature(PackageManager.FEATURE_WATCH) ||
                pm.hasSystemFeature("android.hardware.type.watch") ||
                isSmallRoundScreen(context)
    }

    fun isRoundScreen(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.resources.configuration.isScreenRound
        } else {
            // Fallback heuristic: check if width ≈ height
            val config = context.resources.configuration
            val ratio = config.screenWidthDp.toFloat() / config.screenHeightDp
            ratio in 0.95f..1.05f
        }
    }

    private fun isSmallRoundScreen(context: Context): Boolean {
        val config = context.resources.configuration
        val smallestWidth = min(config.screenWidthDp, config.screenHeightDp)
        return smallestWidth < 300 && isRoundScreen(context)
    }

    fun getScreenDiagonalDp(context: Context): Float {
        val config = context.resources.configuration
        val w = config.screenWidthDp.toFloat()
        val h = config.screenHeightDp.toFloat()
        return kotlin.math.sqrt(w * w + h * h)
    }

    fun isTV(context: Context): Boolean {
        val pm = context.packageManager
        return pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
    }
}