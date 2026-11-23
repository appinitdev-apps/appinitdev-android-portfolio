 

















class AlarmManagerHandler(
    private val context: Context,
    private val timeProvider: TimeProvider,
    private val log: Logger,
) : EventListener {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private var isForeground = true

    override fun onEvent(event: Event) {
        if (event == Event.BringToForeground) {
            isForeground = true
            cancelAlarm()
            return
        }
        if (isForeground) {
            when (event) {
                is Event.SendToBackground -> {
                    isForeground = false
                    if (event.endTime != 0L && event.isTimerRunning) {
                        setAlarm(event.endTime)
                    }
                }

                else -> {
                    
                }
            }
        } else {
            cancelAlarm()
            when (event) {
                is Event.Start -> {
                    if (event.endTime != 0L) {
                        setAlarm(event.endTime)
                    }
                }

                is Event.AddOneMinute -> {
                    if (event.endTime != 0L) {
                        setAlarm(event.endTime)
                    }
                }

                else -> {
                    
                }
            }
        }
    }

    private fun setAlarm(triggerAtMillis: Long) {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAtMillis,
            getAlarmPendingIntent(),
        )
    }

    private fun cancelAlarm() {
        val pendingIntent = getAlarmPendingIntent()
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun getAlarmPendingIntent(): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
