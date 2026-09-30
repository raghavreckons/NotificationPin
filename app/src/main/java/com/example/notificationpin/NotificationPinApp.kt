package com.example.notificationpin

import android.app.Application
import com.example.notificationpin.data.AppDatabase
import com.example.notificationpin.data.NotificationRepository

class NotificationPinApp : Application() {
    lateinit var repository: NotificationRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        repository = NotificationRepository(database.pinnedNotificationDao(), this)
    }
}
