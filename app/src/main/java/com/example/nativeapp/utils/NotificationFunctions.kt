package com.example.nativeapp.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

fun createNotificationChannel(context: Context) {
        val channelId = "network_status_channel"
        val channelName = "Network Status"
        val channelDescription = "Notifications for network status changes"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescription
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
}

@SuppressLint("MissingPermission")
fun showNotification(context: Context, title: String, content: String) {
    Log.i("Notification", "Entered show notification with $title and $context")
    val channelId = "network_status_channel" // Use the same ID as the channel

    val notificationId = (System.currentTimeMillis() % 10000).toInt() // Unique ID
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(content)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    with(NotificationManagerCompat.from(context)) {
        notify(notificationId, builder.build())
    }
}
