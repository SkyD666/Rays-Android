package com.skyd.rays.ui.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.get
import com.skyd.rays.model.preference.AutoShareIgnoreStrategyPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RaysAccessibilityService : AccessibilityService() {
    companion object {
        var topActivityFullName: String = ""
            private set
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private val mutex = Mutex()

    override fun onServiceConnected() {
        super.onServiceConnected()

        val config = AccessibilityServiceInfo()
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        serviceInfo = config
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.packageName != null && event.className != null) {
                val componentName =
                    ComponentName(event.packageName.toString(), event.className.toString())
                val activityInfo = tryGetActivity(componentName)
                scope.launch {
                    val strategy = dataStore.get(AutoShareIgnoreStrategyPreference.key)
                        ?: AutoShareIgnoreStrategyPreference.default
                    val name = activityInfo?.name ?: return@launch
                    if (!name.startsWith(packageName.substringBeforeLast(".debug")) &&
                        runCatching { !name.matches(Regex(strategy)) }.getOrElse { true }
                    ) {
                        Log.i("topActivityFullName", "$name $strategy")
                        mutex.withLock {
                            topActivityFullName = activityInfo.name
                        }
                    }
                }
            }
        }
    }

    private fun tryGetActivity(componentName: ComponentName): ActivityInfo? {
        return try {
            packageManager.getActivityInfo(componentName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

fun isAccessibilityServiceRunning(context: Context): Boolean {
    val prefString: String? = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )

    return prefString != null && prefString.contains(RaysAccessibilityService::class.java.name)
}
