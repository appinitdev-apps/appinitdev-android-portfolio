
 



object ElementDataLoader {
    private const val DEFAULT_LANGUAGE = "en"
    
    
    private val languageCache = mutableMapOf<String, JSONObject>()
    
     







    fun loadElementData(
        context: Context, 
        elementName: String, 
        language: String? = null
    ): JSONObject? {
        val lang = language ?: getAppLanguage(context)
        
        
        val elementData = loadFromLanguage(context.assets, elementName, lang)
        if (elementData != null) {
            return elementData
        }
        
        
        if (lang != DEFAULT_LANGUAGE) {
            Pasteur.info("ElementDataLoader", "Element '$elementName' not found in language '$lang', falling back to English")
            return loadFromLanguage(context.assets, elementName, DEFAULT_LANGUAGE)
        }
        
        return null
    }
    
     


    private fun loadFromLanguage(
        assets: AssetManager,
        elementName: String,
        language: String
    ): JSONObject? {
        try {
            val elementsData = getLanguageData(assets, language)
            if (elementsData.has(elementName)) {
                return elementsData.getJSONObject(elementName)
            }
        } catch (e: IOException) {
            Pasteur.error("ElementDataLoader", "Error loading element data for language '$language': ${e.message}")
        }
        return null
    }
    
     


    private fun getLanguageData(assets: AssetManager, language: String): JSONObject {
        
        if (languageCache.containsKey(language)) {
            return languageCache[language]!!
        }
        
        
        val fileName = "elements_$language.json"
        val inputStream = assets.open(fileName)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        
        
        languageCache[language] = jsonObject
        
        return jsonObject
    }
    
     



    private fun getSystemLanguage(context: Context): String {
        
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0].language
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale.language
        }
    }
    
     



    fun getAppLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("app_language", null)
        return if (lang.isNullOrBlank()) {
            
            
            getSystemLanguage(context)
        } else {
            lang
        }
    }
    
     


    fun clearCache() {
        languageCache.clear()
    }
    
     


    fun isLanguageAvailable(assets: AssetManager, language: String): Boolean {
        return try {
            val fileName = "elements_$language.json"
            assets.open(fileName).close()
            true
        } catch (e: IOException) {
            false
        }
    }
    
     


    fun getAvailableLanguages(assets: AssetManager): List<String> {
        val languages = mutableListOf<String>()
        try {
            val files = assets.list("") ?: emptyArray()
            for (file in files) {
                if (file.startsWith("elements_") && file.endsWith(".json")) {
                    val lang = file.substring(9, file.length - 5) 
                    languages.add(lang)
                }
            }
        } catch (e: IOException) {
            Pasteur.error("ElementDataLoader", "Error listing available languages: ${e.message}")
        }
        return languages.sorted()
    }
}
