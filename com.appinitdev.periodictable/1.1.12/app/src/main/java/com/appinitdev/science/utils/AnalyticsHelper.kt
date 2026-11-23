object AnalyticsHelper {
    fun initialize(context: Context) {
        updateAnalyticsCollection(context)
    }
    fun updateAnalyticsCollection(context: Context) {
        val analyticsPreference = AnalyticsPreference(context)
        val isEnabled = analyticsPreference.getValue()
    }
    fun logScreenView(context: Context, screenName: String, screenClass: String) {
        val analyticsPreference = AnalyticsPreference(context)
        if (!analyticsPreference.getValue()) return
    }
    fun logEvent(context: Context, eventName: String, params: Bundle? = null) {
        val analyticsPreference = AnalyticsPreference(context)
        if (!analyticsPreference.getValue()) return
    }
    fun logFeatureUsage(context: Context, featureName: String) {
        val analyticsPreference = AnalyticsPreference(context)
        if (!analyticsPreference.getValue()) return
    }
}
