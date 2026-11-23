
 






class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive: ${intent?.action}")
        if (intent == null || context == null) {
            return
        }
        val canScheduleExactAlarms = context.canScheduleAlarms()
        if (intent.action == null) {
            if (!canScheduleExactAlarms) {
                return
            }
            val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
            val noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1L)
            notify(context, noteId, reminderId)
        } else {
            when {
                canScheduleExactAlarms && intent.action == Intent.ACTION_BOOT_COMPLETED ->
                    rescheduleAlarms(context)

                intent.action ==
                    AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED -> {
                    if (canScheduleExactAlarms) {
                        rescheduleAlarms(context)
                    } else {
                        cancelAlarms(context)
                    }
                }
            }
        }
    }

    private fun notify(context: Context, noteId: Long, reminderId: Long) {
        Log.d(TAG, "notify: noteId: $noteId reminderId: $reminderId")
        CoroutineScope(Dispatchers.IO).launch {
            val database =
                NotallyDatabase.getDatabase(context.applicationContext as Application, false).value
            val manager = context.getSystemService<NotificationManager>()!!
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                manager.createChannelIfNotExists(
                    NOTIFICATION_CHANNEL_ID,
                    importance = NotificationManager.IMPORTANCE_HIGH,
                )
            }
            database.getBaseNoteDao().get(noteId)?.let { note ->
                val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val notification =
                    NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.notebook)
                        .setContentTitle(note.title.ifBlank { context.getString(R.string.note) })
                        .setContentText(note.body.truncate(200))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setSound(defaultSoundUri)
                        .setVibrate(longArrayOf(0, 500, 250, 500))
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setAutoCancel(true)
                        .addAction(
                            R.drawable.visibility,
                            context.getString(R.string.open_note),
                            context.getOpenNotePendingIntent(note),
                        )
                        .build()
                note.reminders
                    .find { it.id == reminderId }
                    ?.let { reminder: Reminder ->
                        manager.notify(note.id.toString(), reminderId.toInt(), notification)
                        context.scheduleReminder(note.id, reminder, forceRepetition = true)
                    }
            }
        }
    }

    private fun rescheduleAlarms(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val database =
                NotallyDatabase.getDatabase(context.applicationContext as Application, false).value
            val now = Date()
            val noteReminders = database.getBaseNoteDao().getAllReminders()
            val noteRemindersWithFutureNotify =
                noteReminders.flatMap { (noteId, reminders) ->
                    reminders
                        .filter { reminder ->
                            reminder.repetition != null || reminder.dateTime.after(now)
                        }
                        .map { reminder -> Pair(noteId, reminder) }
                }
            Log.d(TAG, "rescheduleAlarms: ${noteRemindersWithFutureNotify.size} alarms")
            noteRemindersWithFutureNotify.forEach { (noteId, reminder) ->
                context.scheduleReminder(noteId, reminder)
            }
        }
    }

    private fun cancelAlarms(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val database =
                NotallyDatabase.getDatabase(context.applicationContext as Application, false).value
            val noteReminders = database.getBaseNoteDao().getAllReminders()
            val noteRemindersWithFutureNotify =
                noteReminders.flatMap { (noteId, reminders) ->
                    reminders.map { reminder -> Pair(noteId, reminder.id) }
                }
            Log.d(TAG, "cancelAlarms: ${noteRemindersWithFutureNotify.size} alarms")
            noteRemindersWithFutureNotify.forEach { (noteId, reminderId) ->
                context.cancelReminder(noteId, reminderId)
            }
        }
    }

    companion object {
        private const val TAG = "ReminderReceiver"

        private const val NOTIFICATION_CHANNEL_ID = "Reminders"

        const val EXTRA_REMINDER_ID = "notes.intent.extra.REMINDER_ID"
        const val EXTRA_NOTE_ID = "notes.intent.extra.NOTE_ID"
    }
}
