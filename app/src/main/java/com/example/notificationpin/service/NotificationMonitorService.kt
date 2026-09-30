package com.example.notificationpin.service

import android.app.Notification
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActiveNotificationItem(
    val key: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val postTime: Long
)

class NotificationMonitorService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        _isServiceConnected.value = true
        refreshActiveNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        _isServiceConnected.value = false
        _activeNotifications.value = emptyList()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        refreshActiveNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        refreshActiveNotifications()
    }

    fun refreshActiveNotifications() {
        serviceScope.launch {
            try {
                val activeSbns = activeNotifications ?: return@launch
                val items = activeSbns.mapNotNull { sbn ->
                    parseStatusBarNotification(sbn)
                }
                _activeNotifications.value = items
            } catch (_: Exception) {
                // Ignore transient security / binder exceptions
            }
        }
    }

    private fun parseStatusBarNotification(sbn: StatusBarNotification): ActiveNotificationItem? {
        val notification = sbn.notification ?: return null
        val extras = notification.extras ?: return null

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim()
        val text = bigText ?: extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim() ?: ""

        // Filter out completely empty system notifications
        if (title.isEmpty() && text.isEmpty()) return null

        val pkgName = sbn.packageName
        val appName = try {
            val appInfo = packageManager.getApplicationInfo(pkgName, PackageManager.GET_META_DATA)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            pkgName
        }

        return ActiveNotificationItem(
            key = sbn.key,
            packageName = pkgName,
            appName = appName,
            title = title,
            text = text,
            postTime = sbn.postTime
        )
    }

    companion object {
        @Volatile
        var instance: NotificationMonitorService? = null
            private set

        private val _isServiceConnected = MutableStateFlow(false)
        val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

        private val _activeNotifications = MutableStateFlow<List<ActiveNotificationItem>>(emptyList())
        val activeNotificationsFlow: StateFlow<List<ActiveNotificationItem>> = _activeNotifications.asStateFlow()
    }
}
