class WidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val id = intent.getLongExtra(EXTRA_SELECTED_BASE_NOTE, 0)
        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)
        return WidgetFactory(application as NotallyXApplication, id, widgetId)
    }
}
