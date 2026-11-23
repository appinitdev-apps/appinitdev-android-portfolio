
val Context.dataStore by preferencesDataStore(name = "tester_prefs")

public class PrefsManager(private val context: Context) {
    companion object {
        private val SHOW_FEATURES_KEY = booleanPreferencesKey("show_tester_features")
    }

    val shouldShowFeatures: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SHOW_FEATURES_KEY] ?: true
    }

    
    suspend fun markFeaturesAsRead() {
        context.dataStore.edit { prefs ->
            prefs[SHOW_FEATURES_KEY] = false
        }
    }
}