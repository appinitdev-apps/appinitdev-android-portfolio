

val Context.dataStore by preferencesDataStore(name = "settings")

object BackgroundPrefs {
    private val KEY_BACKGROUND = stringPreferencesKey("background_path")
    private val KEY_COLOR = stringPreferencesKey("background_color")
    suspend fun saveImage(context: Context, uri: Uri) {
        val dir = context.filesDir

        
        dir.listFiles()?.forEach { file ->
            if (file.name.startsWith("background_") && file.isFile) {
                file.delete()
            }
        }
        val file = File(context.filesDir, "background_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }
        context.dataStore.edit { prefs ->
            prefs[KEY_BACKGROUND] = file.absolutePath
        }
    }
    suspend fun deleteImage(context: Context) {

        
        context.dataStore.edit { prefsEdit ->
            prefsEdit.remove(KEY_BACKGROUND)
            prefsEdit.remove(KEY_COLOR)
        }
    }


    fun getImagePath(context: Context): Flow<String?> =
        context.dataStore.data.map { prefs -> prefs[KEY_BACKGROUND] }



    suspend fun setColor(context: Context, color: Color) {
        val hex = try {
            
            String.format("#%08X", color.toArgb())
        } catch (e: Exception) {
            Log.e("setColor", e.toString())
            "#FF888888" 
        }

        context.dataStore.edit { prefs ->
            Log.e("save", hex)
            prefs[KEY_COLOR] = hex
        }
    }




    fun getColor(context: Context): Flow<Color?> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_COLOR]?.let { hex ->
                try {
                    Color(android.graphics.Color.parseColor(hex))
                } catch (e: Exception) {
                    Log.e("getColor", "Error parsing color: $hex", e)
                    null
                }
            }
        }

}
