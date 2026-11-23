class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    companion object {
        private const val REMOVE_ADS_KEY = "remove_ads"
    }
    fun isAdsRemoved(): Boolean {
        return sharedPreferences.getBoolean(REMOVE_ADS_KEY, false)
    }
    fun setAdsRemoved(value: Boolean) {
        sharedPreferences.edit().putBoolean(REMOVE_ADS_KEY, value).apply()
    }
}
