package com.example.kinet.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.kinet.data.local.KinetDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Re-schedules all habit reminder alarms after device reboot.
 * AlarmManager alarms are cleared on reboot; this receiver restores them.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = KinetDatabase.getInstance(context).habitDao()
                val habits = dao.getHabitsWithReminders()
                habits.forEach { habit ->
                    HabitReminderScheduler.schedule(
                        context = context,
                        habitId = habit.habitId,
                        timeHHmm = habit.reminderTime!!
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
