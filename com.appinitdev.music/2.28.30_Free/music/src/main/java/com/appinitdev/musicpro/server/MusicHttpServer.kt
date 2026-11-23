class MusicHttpServer(
        private val context: Context,
        port: Int,
        private val songStatRepository: SongStatRepository
) : NanoHTTPD(port) {
    private val gson = GsonBuilder().create()
    private val audioDatabase: AudioDatabase by lazy {
        AudioDatabase.getInstance(context)
    }
    private var audioCache: Map<Long, Audio> = emptyMap()
    private var iconBytes: ByteArray? = null
    override fun serve(session: IHTTPSession): Response {
        return try {
            val response = when (session.method) {
                Method.POST -> handlePost(session)
                Method.DELETE -> handleDelete(session)
                Method.OPTIONS -> newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "")
                else -> handleGet(session)
            }
            response.apply {
                addHeader("Access-Control-Allow-Origin", "*")
                addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS")
                addHeader("Access-Control-Allow-Headers", "Content-Type, Range")
                addHeader("Access-Control-Expose-Headers", "Content-Range, Content-Length, Accept-Ranges")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling ${session.method} ${session.uri}", e)
            newFixedLengthResponse(
                    Response.Status.INTERNAL_ERROR,
                    MIME_PLAINTEXT,
                    "Internal server error: ${e.message}"
            ).apply {
                addHeader("Access-Control-Allow-Origin", "*")
            }
        }
    }
    @Suppress("DEPRECATION")
    private fun handleGet(session: IHTTPSession): Response {
        val uri = session.uri
        return when {
            uri == "/" || uri == "/index.html" -> serveAsset("server/index.html", MIME_HTML)
            uri == "/app-icon" -> serveAppIcon()
            uri == "/api/theme" -> serveTheme()
            uri == "/api/songs" -> serveSongList()
            uri == "/api/albums" -> serveAlbums()
            uri == "/api/albums/songs" -> serveAlbumSongs(session.parms["name"] ?: "")
            uri == "/api/artists" -> serveArtists()
            uri == "/api/artists/songs" -> serveArtistSongs(session.parms["name"] ?: "")
            uri == "/api/genres" -> serveGenres()
            uri == "/api/genres/songs" -> serveGenreSongs(session.parms["name"] ?: "")
            STREAM_REGEX.matches(uri) -> {
                val id = STREAM_REGEX.find(uri)!!.groupValues[1].toLong()
                streamAudio(id, session)
            }
            ART_REGEX.matches(uri) -> {
                val id = ART_REGEX.find(uri)!!.groupValues[1].toLong()
                serveAlbumArt(id)
            }
            STATIC_REGEX.matches(uri) -> {
                val filename = uri.removePrefix("/")
                val mimeType = if (filename.endsWith(".css")) MIME_CSS else MIME_JS
                try {
                    serveAsset("server/$filename", mimeType)
                } catch (_: Exception) {
                    newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not found")
                }
            }
            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not found")
        }
    }
    private fun handlePost(session: IHTTPSession): Response {
        val uri = session.uri
        return if (PLAYED_REGEX.matches(uri)) {
            val id = PLAYED_REGEX.find(uri)!!.groupValues[1].toLongOrNull()
                ?: return badRequest("Invalid song ID")
            val audio = findAudio(id) ?: return notFound("Song not found")
            runBlocking { songStatRepository.recordPlay(audio.hash) }
            newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "OK")
        } else {
            newFixedLengthResponse(
                    Response.Status.METHOD_NOT_ALLOWED, MIME_PLAINTEXT, "Method not allowed"
            )
        }
    }
    private fun handleDelete(session: IHTTPSession): Response {
        val uri = session.uri
        if (!DELETE_REGEX.matches(uri)) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not found")
        }
        val id = DELETE_REGEX.find(uri)!!.groupValues[1].toLongOrNull()
            ?: return badRequest("Invalid song ID")
        val audio = findAudio(id) ?: return notFound("Song not found")
        val audioUri = audio.uri.toUri()
        return try {
            val authority = audioUri.authority ?: ""
            val scheme = audioUri.scheme ?: ""
            val isMediaStore = authority == "media"
            val isDocumentProvider = authority.contains("documents") || scheme == "content"
            var deleted = false
            if (isMediaStore) {
                try {
                    deleted = context.contentResolver.delete(audioUri, null, null) > 0
                } catch (e: Exception) {
                    Log.w(TAG, "MediaStore delete failed: ${e.message}")
                }
            }
            if (!deleted && isDocumentProvider) {
                try {
                    deleted = DocumentsContract.deleteDocument(context.contentResolver, audioUri)
                    if (!deleted) {
                        deleted = true 
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "DocumentsContract delete failed: ${e.message}")
                }
            }
            if (!deleted && (scheme == "file" || audio.path != null)) {
                val filePath = audio.path ?: audioUri.path
                if (filePath != null) {
                    try {
                        deleted = File(filePath).delete()
                    } catch (e: Exception) {
                        Log.w(TAG, "File delete failed: ${e.message}")
                    }
                }
            }
            if (deleted) {
                runBlocking {
                    audioDatabase.audioDao()?.delete(audio)
                }
                audioCache = audioCache - id
                Log.i(TAG, "Successfully deleted audio item: ${audio.title} (ID: $id)")
                newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "Deleted")
            } else {
                Log.e(TAG, "All deletion attempts failed for: ${audio.title} (URI: ${audio.uri})")
                newFixedLengthResponse(
                        Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Could not delete file - permission denied or file not found"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Critical error during handleDelete", e)
            newFixedLengthResponse(
                    Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Internal error: ${e.message}"
            )
        }
    }
    private fun serveAsset(path: String, mimeType: String): Response {
        val text = context.assets.open(path).bufferedReader().use { it.readText() }
        return newFixedLengthResponse(Response.Status.OK, mimeType, text)
    }
    private fun serveAppIcon(): Response {
        if (iconBytes == null) {
            iconBytes = try {
                context.assets.open("server/app_icon.png").use { it.readBytes() }
            } catch (_: Exception) {
                val src = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_round)
                val out = ByteArrayOutputStream()
                src.scale(128, 128)  
                    .compress(Bitmap.CompressFormat.PNG, 100, out)
                out.toByteArray()
            }
        }
        val bytes = iconBytes!!
        return newFixedLengthResponse(
                Response.Status.OK, "image/png",
                ByteArrayInputStream(bytes), bytes.size.toLong()
        )
    }
    private fun serveSongList(): Response {
        audioCache = emptyMap()
        val songs = audioDatabase.audioDao()?.getAllAudioList() ?: emptyList()
        audioCache = songs.associateBy { it.id }
        return jsonOk(songs.map { it.toMap() })
    }
    private fun serveAlbums(): Response {
        val albums = cachedSongs()
            .filter { !it.album.isNullOrBlank() }
            .groupBy { it.album!! }
            .map { (name, group) ->
                mapOf(
                        "name" to name,
                        "artist" to (group.firstOrNull()?.artist ?: ""),
                        "songCount" to group.size,
                        "coverSongId" to (group.firstOrNull()?.id ?: 0L)
                )
            }
            .sortedBy { (it["name"] as? String ?: "").lowercase() }
        return jsonOk(albums)
    }
    private fun serveArtists(): Response {
        val artists = cachedSongs()
            .filter { !it.artist.isNullOrBlank() }
            .groupBy { it.artist!! }
            .map { (name, group) ->
                mapOf(
                        "name" to name,
                        "songCount" to group.size,
                        "albumCount" to group.mapNotNull { it.album?.takeIf { a -> a.isNotBlank() } }.distinct().size,
                        "coverSongId" to (group.firstOrNull()?.id ?: 0L)
                )
            }
            .sortedBy { (it["name"] as? String ?: "").lowercase() }
        return jsonOk(artists)
    }
    private fun serveGenres(): Response {
        val genres = cachedSongs()
            .groupBy { it.genre?.takeIf { g -> g.isNotBlank() } ?: UNKNOWN_GENRE }
            .map { (name, group) ->
                mapOf(
                        "name" to name,
                        "songCount" to group.size,
                        "coverSongId" to (group.firstOrNull()?.id ?: 0L)
                )
            }
            .sortedBy { (it["name"] as? String ?: "").lowercase() }
        return jsonOk(genres)
    }
    private fun serveAlbumSongs(name: String): Response {
        if (name.isBlank()) return badRequest("Missing 'name' parameter")
        return jsonOk(cachedSongs().filter { it.album == name }.map { it.toMap() })
    }
    private fun serveArtistSongs(name: String): Response {
        if (name.isBlank()) return badRequest("Missing 'name' parameter")
        return jsonOk(cachedSongs().filter { it.artist == name }.map { it.toMap() })
    }
    private fun serveGenreSongs(name: String): Response {
        if (name.isBlank()) return badRequest("Missing 'name' parameter")
        val target = if (name == UNKNOWN_GENRE) null else name
        val songs = cachedSongs()
            .filter { (it.genre?.takeIf { g -> g.isNotBlank() }) == target }
            .map { it.toMap() }
        return jsonOk(songs)
    }
    private fun streamAudio(id: Long, session: IHTTPSession): Response {
        val audio = findAudio(id) ?: return notFound("Song not found")
        val audioUri = audio.uri.toUri()
        val mimeType = audio.mimeType?.takeIf { it.isNotBlank() } ?: "audio/*"
        val range = session.headers["range"]
        val fileLength = try {
            context.contentResolver.openFileDescriptor(audioUri, "r")?.use { it.statSize } ?: -1L
        } catch (e: Exception) {
            Log.w(TAG, "Could not stat file descriptor for URI: ${audio.uri}", e)
            -1L
        }
        return if (range != null && range.startsWith("bytes=")) {
            val parts = range.removePrefix("bytes=").split("-")
            val start = parts[0].toLongOrNull() ?: 0L
            val end = if (fileLength > 0 && parts.size > 1 && parts[1].isNotBlank()) {
                parts[1].toLong()
            } else if (fileLength > 0) {
                fileLength - 1
            } else {
                Long.MAX_VALUE / 2
            }
            val length = end - start + 1
            val stream: InputStream = context.contentResolver.openInputStream(audioUri)
                ?: return notFound("Could not open audio stream")
            stream.skip(start)
            newFixedLengthResponse(Response.Status.PARTIAL_CONTENT, mimeType, stream, length).apply {
                addHeader("Content-Range", "bytes $start-$end/$fileLength")
                addHeader("Accept-Ranges", "bytes")
                if (fileLength > 0) addHeader("Content-Length", length.toString())
            }
        } else {
            val stream: InputStream = context.contentResolver.openInputStream(audioUri)
                ?: return notFound("Could not open audio stream")
            newFixedLengthResponse(Response.Status.OK, mimeType, stream, fileLength).apply {
                addHeader("Accept-Ranges", "bytes")
                if (fileLength > 0) addHeader("Content-Length", fileLength.toString())
            }
        }
    }
    private fun serveAlbumArt(id: Long): Response {
        val audio = findAudio(id) ?: return notFound("Song not found")
        val bitmap = try {
            AudioCover.load(context, audio)
        } catch (e: Exception) {
            Log.w(TAG, "Album art load failed for id=$id", e)
            null
        }
        return if (bitmap != null && !bitmap.isRecycled) {
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            val bytes = out.toByteArray()
            newFixedLengthResponse(
                    Response.Status.OK, "image/jpeg",
                    ByteArrayInputStream(bytes), bytes.size.toLong()
            )
        } else {
            notFound("No artwork available")
        }
    }
    private fun jsonOk(data: Any): Response {
        val json = gson.toJson(data)
        return newFixedLengthResponse(Response.Status.OK, MIME_JSON, json)
    }
    private fun serveTheme(): Response {
        val accent = ThemeManager.accent
        val primaryHex = String.format("#%06X", 0xFFFFFF and accent.primaryAccentColor)
        val secondaryHex = String.format("#%06X", 0xFFFFFF and accent.secondaryAccentColor)
        val isDark = when (AppearancePreferences.getTheme()) {
            ThemeConstants.LIGHT_THEME,
            ThemeConstants.HIGH_CONTRAST_LIGHT,
            ThemeConstants.SOAPSTONE -> false
            ThemeConstants.FOLLOW_SYSTEM,
            ThemeConstants.DAY_NIGHT -> {
                val nightMask = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                nightMask == Configuration.UI_MODE_NIGHT_YES
            }
            else -> true
        }
        val json = """{"accent":"$primaryHex","accentSecondary":"$secondaryHex","isDark":$isDark}"""
        return newFixedLengthResponse(Response.Status.OK, MIME_JSON, json)
    }
    private fun cachedSongs(): List<Audio> {
        if (audioCache.isEmpty()) {
            audioCache = (audioDatabase.audioDao()?.getAllAudioList() ?: emptyList())
                .associateBy { it.id }
        }
        return audioCache.values.toList()
    }
    private fun findAudio(id: Long): Audio? {
        return audioCache[id] ?: run {
            val audio = runBlocking { audioDatabase.audioDao()?.getAudioById(id) }
            if (audio != null) {
                audioCache = audioCache + (id to audio)
            }
            audio
        }
    }
    private fun Audio.toMap(): Map<String, Any?> = mapOf(
            "id" to id,
            "title" to (title?.takeIf { it.isNotBlank() } ?: name),
            "name" to name,
            "artist" to artist,
            "album" to album,
            "genre" to genre,
            "duration" to duration,
            "mimeType" to mimeType
    )
    private fun badRequest(message: String): Response =
        newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, message)
    private fun notFound(message: String): Response =
        newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, message)
    @Throws(IOException::class)
    fun startServer() {
        start(SOCKET_READ_TIMEOUT, false)
    }
    companion object {
        private const val TAG = "MusicHttpServer"
        private const val MIME_HTML = "text/html; charset=utf-8"
        private const val MIME_CSS = "text/css; charset=utf-8"
        private const val MIME_JS = "application/javascript; charset=utf-8"
        private const val MIME_JSON = "application/json; charset=utf-8"
        private const val JPEG_QUALITY = 85
        private const val UNKNOWN_GENRE = "Unknown"
        private val STREAM_REGEX = Regex("/api/songs/(\\d+)/stream")
        private val ART_REGEX = Regex("/api/songs/(\\d+)/art")
        private val PLAYED_REGEX = Regex("/api/songs/(\\d+)/played")
        private val DELETE_REGEX = Regex("/api/songs/(\\d+)")
        private val STATIC_REGEX = Regex("/[\\w-]+\\.(?:css|js)")
    }
}
