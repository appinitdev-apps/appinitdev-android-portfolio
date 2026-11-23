class BmiAppWidgetProvider : AppWidgetProvider() {
    companion object {
        const val ACTION_ADD_WATER = "com.appinitdev.imc.ACTION_ADD_WATER"
    }
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_ADD_WATER) {
            val dbHelper = BmiDatabaseHelper(context)
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            dbHelper.addWaterIntake(todayStr, 250, 2000)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, BmiAppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_bmi)
        val familyManager = FamilyProfileManager(context)
        val activeMember = familyManager.getActiveMember()
        val dbHelper = BmiDatabaseHelper(context)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val waterPair = dbHelper.getWaterIntakeForDate(todayStr)
        val memberName = activeMember?.name ?: "Mi Perfil"
        val records = activeMember?.id?.let { dbHelper.getGrowthRecordsForMember(it) }
        val latestRecord = records?.lastOrNull()
        val statusText = if (latestRecord != null) {
            "${latestRecord.statusText} (${String.format(Locale.US, "%.1f", latestRecord.bmiValue)})"
        } else {
            "Sin mediciones"
        }
        views.setTextViewText(R.id.widget_member_name, memberName)
        views.setTextViewText(R.id.widget_bmi_status, statusText)
        views.setTextViewText(R.id.widget_water_text, "${waterPair.first} ml / ${waterPair.second} ml")
        val intent = Intent(context, BmiAppWidgetProvider::class.java).apply {
            action = ACTION_ADD_WATER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_add_water, pendingIntent)
        val mainIntent = Intent(context, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            1,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_member_name, mainPendingIntent)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
