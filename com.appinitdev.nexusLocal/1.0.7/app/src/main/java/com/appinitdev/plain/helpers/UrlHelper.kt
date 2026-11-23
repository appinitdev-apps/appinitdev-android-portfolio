object UrlHelper {
    private val mediaPathMap = mutableMapOf<String, String>() 
    fun getMediaHttpUrl(path: String): String {
        val id = System.currentTimeMillis().toString()
        mediaPathMap[id] = path
        val extension = path.getFilenameExtension()
        return "http://${NetworkHelper.getDeviceIP4()}:${TempData.httpPort.value}/media/$id.$extension"
    }
    fun getAlbumArtHttpUrl(albumUri: Uri): String {
        val id = "art_${System.currentTimeMillis()}"
        mediaPathMap[id] = albumUri.toString()
        return "http://${NetworkHelper.getDeviceIP4()}:${TempData.httpPort.value}/media/$id.jpg"
    }
    fun getCastCallbackUrl(): String {
        return "http://${NetworkHelper.getDeviceIP4()}:${TempData.httpPort.value}/callback/cast"
    }
    fun getHealthCheckUrl(): String {
        return "http://127.0.0.1:${TempData.httpPort.value}/health"
    }
    fun getShutdownUrl(): String {
        return "http://127.0.0.1:${TempData.httpPort.value}/shutdown"
    }
    fun getMediaPath(id: String): String {
        return mediaPathMap[id] ?: ""
    }
    fun decrypt(id: String): String {
        val bytes = Base64.decode(id, Base64.NO_WRAP)
        return CryptoHelper.chaCha20Decrypt(TempData.urlToken, bytes)?.decodeToString() ?: ""
    }
    fun getPolicyUrl(): String {
        return "https://plainhub.github.io/plain-app/policy.html"
    }
    fun getTermsUrl(): String {
        return "https://plainhub.github.io/plain-app/terms.html"
    }
}
