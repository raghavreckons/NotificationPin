# NotificationPin Android App

A native Android application with Jetpack Compose and Jetpack Glance widget that allows users to manually pin status bar notifications to an interactive Home Screen Widget.

## Key Features

1. **NotificationListenerService (`NotificationMonitorService`)**:
   - Safely listens to status bar notifications.
   - Extracts notification app name, title, text/bigText, and post timestamp.
   - Provides live updates to the UI via Kotlin StateFlow.

2. **Room Database (`AppDatabase`, `PinnedNotificationDao`, `NotificationRepository`)**:
   - Stores pinned notifications persistently.
   - Automatically synchronizes with the home screen widget whenever items are pinned or unpinned.

3. **Jetpack Glance Widget (`NotificationWidget`)**:
   - Native modern home screen widget using declarative Compose APIs (`GlanceAppWidget`).
   - Scrollable `LazyColumn` of pinned notifications.
   - Interactive unpin button (`actionRunCallback<UnpinActionCallback>`) on each card.
   - Direct launch action to open the main app.

4. **App UI (`MainActivity`, `MainScreen`, `NotificationViewModel`)**:
   - **Active Notifications Tab**: Lists all live status bar notifications with a 1-tap "📌 Pin to Widget" button.
   - **Pinned Notifications Tab**: Displays all currently pinned notifications, allows unpinning, clearing all, or launching the corresponding app.
   - **Permission Guard**: Detects if notification access is granted and prompts user directly to system settings.

## How to Run in Android Studio

1. Open Android Studio.
2. Select **File > Open** and choose the `NotificationPin` directory.
3. Allow Gradle to sync.
4. Run the project on an Android device or emulator running Android 8.0 (API 26) or higher.
5. On first launch, tap **"Open Notification Access Settings"** and toggle ON access for **Notification Pin**.
6. Return to the app. Any active status bar notification will appear in the "Active" tab. Tap **"Pin to Widget"**.
7. Go to your Home Screen, long press, select **Widgets > Notification Pin**, and drag the widget to your home screen!
