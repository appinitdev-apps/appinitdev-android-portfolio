object ReviewOcrScanner {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    suspend fun verifyReview(context: Context, imageUri: Uri, appName: String): Result<Boolean> = suspendCancellableCoroutine { continuation ->
        try {
            val image = InputImage.fromFilePath(context, imageUri)
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    val text = result.text.lowercase()
                    val appNameWords = appName.lowercase().split(" ").filter { it.length > 2 }
                    val foundWords = appNameWords.count { text.contains(it) }
                    val isMatch = if (appNameWords.isEmpty()) text.contains(appName.lowercase()) 
                                 else (foundWords.toFloat() / appNameWords.size) >= 0.6f
                    if (isMatch) {
                        continuation.resume(Result.success(true))
                    } else {
                        continuation.resume(Result.failure(Exception("No se detectó el nombre de la app '$appName' en la captura. Intenta que el texto sea más claro.")))
                    }
                }
                .addOnFailureListener { e ->
                    continuation.resume(Result.failure(e))
                }
        } catch (e: Exception) {
            continuation.resume(Result.failure(e))
        }
    }
}
