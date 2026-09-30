package com.example.notificationpin.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.example.notificationpin.data.AppDatabase
import com.example.notificationpin.data.NotificationRepository

val PinnedIdParamKey = ActionParameters.Key<Long>("pinned_notification_id")

class UnpinActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val id = parameters[PinnedIdParamKey] ?: return
        val db = AppDatabase.getInstance(context)
        val repository = NotificationRepository(db.pinnedNotificationDao(), context)
        repository.unpinNotification(id)
    }
}

class RefreshWidgetActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        NotificationWidget().update(context, glanceId)
    }
}
