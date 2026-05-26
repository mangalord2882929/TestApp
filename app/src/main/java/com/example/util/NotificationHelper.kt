package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object NotificationHelper {
    const val CHANNEL_HIGH_SOUND_VIB = "channel_high_sound_vib"
    const val CHANNEL_DEFAULT_SOUND_VIB = "channel_default_sound_vib"
    const val CHANNEL_SOUND_ONLY = "channel_sound_only"
    const val CHANNEL_VIB_ONLY = "channel_vib_only"
    const val CHANNEL_SILENT = "channel_silent"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channels = listOf(
                NotificationChannel(
                    CHANNEL_HIGH_SOUND_VIB,
                    "High Priority (Sound & Vibrate)",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Used for important reminders with sound and vibration"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 250, 250, 250)
                },
                NotificationChannel(
                    CHANNEL_DEFAULT_SOUND_VIB,
                    "Default Priority (Sound & Vibrate)",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Standard reminders with sound and vibration"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_SOUND_ONLY,
                    "Sound Only",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Reminders with sound but no vibration"
                    enableVibration(false)
                },
                NotificationChannel(
                    CHANNEL_VIB_ONLY,
                    "Vibration Only",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Reminders that vibrate only"
                    enableVibration(true)
                    setSound(null, null)
                },
                NotificationChannel(
                    CHANNEL_SILENT,
                    "Silent Reminders",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Silent reminders with no sound or vibration"
                    enableVibration(false)
                    setSound(null, null)
                }
            )

            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    fun showNotification(
        context: Context,
        id: Int,
        title: String,
        description: String,
        priority: String,
        soundEnabled: Boolean,
        vibrateEnabled: Boolean
    ) {
        // Choose the correct channel based on preferences
        val channelId = when {
            priority == "HIGH" && soundEnabled && vibrateEnabled -> CHANNEL_HIGH_SOUND_VIB
            soundEnabled && vibrateEnabled -> CHANNEL_DEFAULT_SOUND_VIB
            soundEnabled -> CHANNEL_SOUND_ONLY
            vibrateEnabled -> CHANNEL_VIB_ONLY
            else -> CHANNEL_SILENT
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(description.ifEmpty { "You have a task reminder!" })
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setPriority(
                when (priority) {
                    "HIGH" -> NotificationCompat.PRIORITY_HIGH
                    "LOW" -> NotificationCompat.PRIORITY_LOW
                    else -> NotificationCompat.PRIORITY_DEFAULT
                }
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (soundEnabled && channelId != CHANNEL_SILENT && channelId != CHANNEL_VIB_ONLY) {
            builder.setSound(soundUri)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, builder.build())
    }
}
