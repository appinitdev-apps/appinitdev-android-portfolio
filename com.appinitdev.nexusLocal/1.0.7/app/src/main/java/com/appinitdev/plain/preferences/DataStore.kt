val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = {
            LogCat.e("DataStore preferences corrupted, creating new empty preferences")
            emptyPreferences()
        }
    )
)
suspend fun <T> BasePreference<T>.getAsync(context: Context): T = getAsync()
@Suppress("UNCHECKED_CAST")
suspend fun <T> BasePreference<T>.putAsync(context: Context, value: T) = putAsync(value)
