
class GoodtimeWidgetProvider : AppWidgetProvider(), KoinComponent {

    private val timerManager: TimerManager by inject()
    private val timeProvider: TimeProvider by inject()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_TOGGLE_TIMER -> {
                val data = timerManager.timerData.value
                if (data.state == TimerState.RESET) {
                    timerManager.start()
                } else {
                    timerManager.toggle()
                }
                updateAllWidgets(context)
            }
            ACTION_RESET_TIMER -> {
                timerManager.reset()
                updateAllWidgets(context)
            }
            ACTION_UPDATE_WIDGET -> {
                updateAllWidgets(context)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, GoodtimeWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_goodtime)

        // Open app when tapping root layout or title
        val mainIntent = Intent(context, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, mainPendingIntent)

        // Toggle action intent
        val toggleIntent = Intent(context, GoodtimeWidgetProvider::class.java).apply {
            action = ACTION_TOGGLE_TIMER
        }
        val togglePendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_toggle, togglePendingIntent)

        // Reset action intent
        val resetIntent = Intent(context, GoodtimeWidgetProvider::class.java).apply {
            action = ACTION_RESET_TIMER
        }
        val resetPendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            resetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_reset, resetPendingIntent)

        try {
            val data = timerManager.timerData.value
            val labelName = data.getLabelName().ifBlank { "Focus" }
            views.setTextViewText(R.id.widget_title, labelName)

            val remainingMs = data.getBaseTime(timeProvider)
            val remainingSec = (remainingMs / 1000).coerceAtLeast(0)
            val mins = remainingSec / 60
            val secs = remainingSec % 60
            val timeText = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
            views.setTextViewText(R.id.widget_timer_text, timeText)

            when (data.state) {
                TimerState.RUNNING -> {
                    views.setTextViewText(R.id.widget_status_text, "Running")
                    views.setTextViewText(R.id.widget_btn_toggle, "Pause")
                }
                TimerState.PAUSED -> {
                    views.setTextViewText(R.id.widget_status_text, "Paused")
                    views.setTextViewText(R.id.widget_btn_toggle, "Resume")
                }
                else -> {
                    views.setTextViewText(R.id.widget_status_text, "Ready")
                    views.setTextViewText(R.id.widget_btn_toggle, "Start")
                }
            }
        } catch (_: Exception) {
            views.setTextViewText(R.id.widget_title, "Goodtime Focus")
            views.setTextViewText(R.id.widget_timer_text, "25:00")
            views.setTextViewText(R.id.widget_status_text, "Ready")
            views.setTextViewText(R.id.widget_btn_toggle, "Start")
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        const val ACTION_TOGGLE_TIMER = "com.apps.adrcotfas.goodtime.main.ACTION_TOGGLE_TIMER"
        const val ACTION_RESET_TIMER = "com.apps.adrcotfas.goodtime.main.ACTION_RESET_TIMER"
        const val ACTION_UPDATE_WIDGET = "com.apps.adrcotfas.goodtime.main.ACTION_UPDATE_WIDGET"

        fun pushWidgetUpdate(context: Context) {
            val intent = Intent(context, GoodtimeWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            context.sendBroadcast(intent)
        }
    }
}
