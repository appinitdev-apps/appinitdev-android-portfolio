object TranslationManager {
    suspend fun translateText(text: String, sourceLang: String): String {
        val targetLang = TranslateLanguage.fromLanguageTag(Locale.getDefault().language) ?: TranslateLanguage.SPANISH
        if (sourceLang == targetLang) return text
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()
        val translator = Translation.getClient(options)
        return try {
            translator.downloadModelIfNeeded().await()
            translator.translate(text).await()
        } catch (e: Exception) {
            text
        }
    }
    suspend fun detectLanguage(text: String): String? = suspendCoroutine { continuation ->
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                if (languageCode == "und") {
                    continuation.resume(null)
                } else {
                    continuation.resume(languageCode)
                }
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }
    suspend fun autoTranslate(text: String): String {
        val detectedLangCode = detectLanguage(text) ?: return text
        val sourceLang = TranslateLanguage.fromLanguageTag(detectedLangCode) ?: return text
        return translateText(text, sourceLang)
    }
}
