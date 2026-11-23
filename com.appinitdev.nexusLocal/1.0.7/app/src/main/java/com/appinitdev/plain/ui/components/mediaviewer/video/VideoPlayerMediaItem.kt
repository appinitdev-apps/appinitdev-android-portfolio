
interface BaseVideoPlayerMediaItem {
    val mediaMetadata: MediaMetadata
    val mimeType: String
}

sealed interface VideoPlayerMediaItem : BaseVideoPlayerMediaItem {

     





    data class RawResourceMediaItem(
        @RawRes val resourceId: Int,
        override val mediaMetadata: MediaMetadata = MediaMetadata.EMPTY,
        override val mimeType: String = "",
    ) : VideoPlayerMediaItem

     






    data class AssetFileMediaItem(
        val assetPath: String,
        override val mediaMetadata: MediaMetadata = MediaMetadata.EMPTY,
        override val mimeType: String = "",
    ) : VideoPlayerMediaItem

     






    data class StorageMediaItem(
        val storageUri: Uri,
        override val mediaMetadata: MediaMetadata = MediaMetadata.EMPTY,
        override val mimeType: String = "",
    ) : VideoPlayerMediaItem

     






    data class NetworkMediaItem(
        val url: String,
        override val mediaMetadata: MediaMetadata = MediaMetadata.EMPTY,
        override val mimeType: String = "",
        val drmConfiguration: DrmConfiguration? = null,
    ) : VideoPlayerMediaItem
}
