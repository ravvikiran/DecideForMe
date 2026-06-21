package com.decideforme.domain

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Reschedules the daily reminder alarm after device reboot.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val scheduler = NotificationScheduler(context)
            scheduler.createNotificationChannel()
            scheduler.scheduleDailyReminder()
        }
    }
}
