
 


class ShortCommandWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray) {

        
        val widgetIds = appWidgetManager.getAppWidgetIds( ComponentName(context, ShortCommandWidget::class.java))
        for (appWidgetId in widgetIds) {

            
            val remoteViews = RemoteViews(context.packageName, R.layout.short_command_widget)

            
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            remoteViews.setOnClickPendingIntent(R.id.widget_search_bar,
                PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), flags))

            
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    override fun onEnabled(context: Context) {
        
    }

    override fun onDisabled(context: Context) {
        
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int) {

    val widgetIds = appWidgetManager.getAppWidgetIds((ComponentName(context, Short::class.java)))
    for (appWidgetId in widgetIds) {

        val views = RemoteViews(context.packageName, R.layout.short_command_widget)


    }
}