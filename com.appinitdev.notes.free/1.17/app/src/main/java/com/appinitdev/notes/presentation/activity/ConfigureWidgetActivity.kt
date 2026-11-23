class ConfigureWidgetActivity : PickNoteActivity() {
    private val id by lazy {
        intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val result = Intent()
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
        setResult(RESULT_CANCELED, result)
    }
    override fun onClick(position: Int) {
        if (position != -1) {
            val preferences = NotallyXPreferences.getInstance(application)
            val baseNote = adapter.getItem(position) as BaseNote
            preferences.updateWidget(id, baseNote.id, baseNote.type)
            val manager = AppWidgetManager.getInstance(this)
            WidgetProvider.updateWidget(application, manager, id, baseNote.id, baseNote.type)
            val success = Intent()
            success.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            setResult(RESULT_OK, success)
            finish()
        }
    }
}
