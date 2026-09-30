package com.example.notificationpin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.notificationpin.ui.MainScreen
import com.example.notificationpin.ui.NotificationViewModel
import com.example.notificationpin.ui.NotificationViewModelFactory
import com.example.notificationpin.ui.theme.NotificationPinTheme

class MainActivity : ComponentActivity() {

    private val viewModel: NotificationViewModel by viewModels {
        val app = application as NotificationPinApp
        NotificationViewModelFactory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotificationPinTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshActiveNotifications()
    }
}
