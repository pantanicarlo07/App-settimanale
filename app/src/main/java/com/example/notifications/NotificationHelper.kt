package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.R
import com.example.data.RoutineItem

object NotificationHelper {
    const val CHANNEL_ID = "weekly_routine_reminders"
    const val CHANNEL_NAME = "Promemoria Routine Settimanale"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                importance
            ).apply {
                description = "Notifiche per eventi e promemoria della tua routine settimanale"
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showRoutineNotification(context: Context, item: RoutineItem) {
        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(item.title)
                .setContentText("${item.dayEnum.fullName}: ${if (item.formattedTime.isNotEmpty()) item.formattedTime else item.notes.ifEmpty { "Promemoria attivo" }}")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                if (areNotificationsEnabled()) {
                    notify(item.id.toInt().takeIf { it != 0 } ?: (System.currentTimeMillis() % 10000).toInt(), builder.build())
                }
            }
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}
