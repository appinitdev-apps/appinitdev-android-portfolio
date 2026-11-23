class ElementOfTheDayWidget : AppWidgetProvider() {
    companion object {
        private val ELEMENT_KEYS = listOf(
            "hydrogen", "helium", "lithium", "beryllium", "boron", "carbon", "nitrogen", "oxygen",
            "fluorine", "neon", "sodium", "magnesium", "aluminium", "silicon", "phosphorus", "sulfur",
            "chlorine", "argon", "potassium", "calcium", "scandium", "titanium", "vanadium", "chromium",
            "manganese", "iron", "cobalt", "nickel", "copper", "zinc", "gallium", "germanium",
            "arsenic", "selenium", "bromine", "krypton", "rubidium", "strontium", "yttrium", "zirconium",
            "niobium", "molybdenum", "technetium", "ruthenium", "rhodium", "palladium", "silver", "cadmium",
            "indium", "tin", "antimony", "tellurium", "iodine", "xenon", "caesium", "barium",
            "lanthanum", "cerium", "praseodymium", "neodymium", "promethium", "samarium", "europium", "gadolinium",
            "terbium", "dysprosium", "holmium", "erbium", "thulium", "ytterbium", "lutetium", "hafnium",
            "tantalum", "tungsten", "rhenium", "osmium", "iridium", "platinum", "gold", "mercury",
            "thallium", "lead", "bismuth", "polonium", "astatine", "radon", "francium", "radium",
            "actinium", "thorium", "protactinium", "uranium", "neptunium", "plutonium", "americium", "curium",
            "berkelium", "californium", "einsteinium", "fermium", "mendelevium", "nobelium", "lawrencium", "rutherfordium",
            "dubnium", "seaborgium", "bohrium", "hassium", "meitnerium", "darmstadtium", "roentgenium", "copernicium",
            "nihonium", "flerovium", "moscovium", "livermorium", "tennessine", "oganesson"
        )
        fun getElementOfTheDay(): String {
            val calendar = Calendar.getInstance()
            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            val index = (dayOfYear - 1) % ELEMENT_KEYS.size
            return ELEMENT_KEYS[index]
        }
    }
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val widgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, ElementOfTheDayWidget::class.java)
        )
        for (appWidgetId in widgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }
    override fun onEnabled(context: Context) {
    }
    override fun onDisabled(context: Context) {
    }
    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val elementKey = getElementOfTheDay()
        val views = RemoteViews(context.packageName, R.layout.element_of_the_day_widget)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("widget_element_key", elementKey)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, flags)
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        scope.launch {
            try {
                val jsonObject = ElementDataLoader.loadElementData(context, elementKey)
                if (jsonObject != null) {
                    val elementName = jsonObject.optString("element", "---")
                    val symbol = jsonObject.optString("short", "---")
                    val atomicNumber = jsonObject.optString("element_atomic_number", "---")
                    val description = jsonObject.optString("description", "---")
                    val imageUrl = jsonObject.optString("link", "")
                    views.setTextViewText(R.id.widget_element_name, elementName)
                    views.setTextViewText(R.id.widget_element_symbol, symbol)
                    views.setTextViewText(R.id.widget_atomic_number, atomicNumber)
                    views.setTextViewText(R.id.widget_element_description, description)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } else {
                    views.setTextViewText(R.id.widget_element_name, "Element of the Day")
                    views.setTextViewText(R.id.widget_element_symbol, "?")
                    views.setTextViewText(R.id.widget_atomic_number, "---")
                    views.setTextViewText(R.id.widget_element_description, "Unable to load element data")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                views.setTextViewText(R.id.widget_element_name, "Element of the Day")
                views.setTextViewText(R.id.widget_element_symbol, "?")
                views.setTextViewText(R.id.widget_atomic_number, "---")
                views.setTextViewText(R.id.widget_element_description, "Error loading element")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
        views.setTextViewText(R.id.widget_element_name, "Loading...")
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
