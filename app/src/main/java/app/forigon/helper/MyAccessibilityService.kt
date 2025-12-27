package app.forigon.helper

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi

class MyAccessibilityService : AccessibilityService() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "LOCK_SCREEN") {
            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        }
        return START_NOT_STICKY
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // no-op
    }

    override fun onInterrupt() { /* no-op */ }
}