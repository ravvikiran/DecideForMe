package com.decideforme.domain

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.decideforme.data.repository.DecisionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Reschedules the daily reminder alarm after device reboot.
 * Only reschedules if the user has daily reminders enabled.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val repository = DecisionRepository(context)
            CoroutineScope(Dispatchers.IO).launch {
                repository.initialize()
                if (repository.currentData.settings.dailyReminderEnabled) {
                    val scheduler = NotificationScheduler(context)
                    scheduler.createNotificationChannel()
                    scheduler.scheduleDailyReminder()
                }
            }
        }
    }
}
