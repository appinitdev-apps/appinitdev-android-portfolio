
object MetadataHelper {

    fun getAudioMetadata(context: Context, songUri: Uri): AudioMetaData {
        runCatching {
            val audioMetadata = AudioMetaData()
            val mediaMetadataRetriever = MediaMetadataRetriever()

            if (URLUtil.isValidUrl(songUri.toString()) &&
                    (songUri.toString().startsWith("http")
                            || songUri.toString().startsWith("https")
                            || songUri.toString().startsWith("ftp"))
            ) {
                mediaMetadataRetriever.setDataSource(songUri.toString(), hashMapOf())
            } else {
                mediaMetadataRetriever.setDataSource(context, songUri)
            }

            audioMetadata.title = getSongTitleMeta(context, songUri, mediaMetadataRetriever)
            audioMetadata.artists = getSongArtistMeta(context, mediaMetadataRetriever)
            audioMetadata.album = getSongAlbumMeta(context, mediaMetadataRetriever)
            audioMetadata.format = getFileExtension(context, songUri)
            audioMetadata.bitrate = getBitrate(mediaMetadataRetriever)
            audioMetadata.sampling = AudioUtils.getSampling(context, songUri)
            audioMetadata.art = getOriginalAlbumArt(mediaMetadataRetriever)

            mediaMetadataRetriever.close()

            return audioMetadata
        }.getOrElse {
            throw IOException("Failed to get audio metadata")
        }
    }

     







    private fun getSongTitleMeta(
            context: Context,
            songUri: Uri,
            mediaMetadataRetriever: MediaMetadataRetriever
    ): String {
        return try {
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)!!
        } catch (e: NullPointerException) {
            DocumentFile.fromSingleUri(context, songUri)?.name
                ?: context.getString(R.string.unknown)
        }
    }

     







    private fun getSongArtistMeta(
            context: Context,
            mediaMetadataRetriever: MediaMetadataRetriever
    ): String {
        return try {
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)!!
        } catch (e: NullPointerException) {
            context.getString(R.string.unknown)
        }
    }

     







    private fun getSongAlbumMeta(
            context: Context,
            mediaMetadataRetriever: MediaMetadataRetriever
    ): String {
        return try {
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)!!
        } catch (e: NullPointerException) {
            context.getString(R.string.unknown)
        }
    }

     








    private fun getBitrate(mediaMetadataRetriever: MediaMetadataRetriever): String {
        return mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)!!
            .toInt().toBitrate()
    }

     


    private fun getFileExtension(context: Context, uri: Uri?): String {
        val e = "." + MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(context.contentResolver.getType(uri!!))
        return if (e.contains("null")) {
            context.getString(R.string.not_available)
        } else {
            e
        }
    }

    private fun getOriginalAlbumArt(mediaMetadataRetriever: MediaMetadataRetriever): Bitmap? {
        var inputStream: InputStream? = null
        if (mediaMetadataRetriever.embeddedPicture != null) {
            inputStream = ByteArrayInputStream(mediaMetadataRetriever.embeddedPicture)
        }
        return BitmapFactory.decodeStream(inputStream)
    }
}