object DownloadHelper {
    suspend fun downloadAsync(url: String, dir: String): DownloadResult {
        val httpClient = HttpClientManager.browserClient()
        return try {
            val r = httpClient.get(url)
            if (r.isOk()) {
                File(dir).mkdirs()
                var path = "$dir/${CryptoHelper.sha1(url.toByteArray())}"
                val extension = url.getFilenameExtension()
                if (extension.isNotEmpty()) {
                    path += ".$extension"
                }
                val file = File(path)
                file.createNewFile()
                r.bodyAsChannel().copyAndClose(file.writeChannel())
                MainApp.instance.scanFileByConnection(file, null)
                DownloadResult(path, true)
            } else {
                DownloadResult("", false, r.toString())
            }
        } catch (ex: Exception) {
            LogCat.e(ex.toString())
            ex.printStackTrace()
            DownloadResult("", false, ex.toString())
        }
    }
    suspend fun downloadToTempAsync(url: String, tempFile: File): DownloadResult {
        val httpClient = HttpClientManager.browserClient()
        return try {
            val r = httpClient.get(url)
            if (r.isOk()) {
                r.bodyAsChannel().copyAndClose(tempFile.writeChannel())
                DownloadResult(tempFile.absolutePath, true)
            } else {
                DownloadResult("", false, r.toString())
            }
        } catch (ex: Exception) {
            LogCat.e(ex.toString())
            ex.printStackTrace()
            DownloadResult("", false, ex.toString())
        }
    }
}