data class PreviewItem(
    val id: String,
    var path: String = "",
    var size: Long = 0L,
    val mediaId: String = "",
    val data: IData? = null,
) {
    var intrinsicSize: IntSize = IntSize.Zero
    var rotation: Int = -1
    fun isVideo(): Boolean {
        if (path.isVideoFast()) return true
        return when (val d = data) {
            is DMessageFile -> d.fileName.isVideoFast()
            is DVideo -> true
            else -> false
        }
    }
    fun getMimeType(): String {
       return path.getMimeType().ifEmpty {
            if (data is DMessageFile) data.fileName.getMimeType() else ""
        }
    }
    fun isImage(): Boolean {
        if (path.isImageFast()) return true
        return when (val d = data) {
            is DMessageFile -> d.fileName.isImageFast()
            is DImage -> true
            else -> false
        }
    }
    fun initAsync(m: DImage) {
        rotation = m.rotation
        intrinsicSize = m.getRotatedSize()
        if (intrinsicSize == IntSize.Zero) {
            initImageAsync()
        }
    }
    fun initImageAsync() {
        rotation = ImageHelper.getRotation(path)
        intrinsicSize = ImageHelper.getIntrinsicSize(path, rotation)
    }
    fun initAsync(m: DVideo) {
        rotation = m.rotation
        intrinsicSize = m.getRotatedSize()
        if (intrinsicSize == IntSize.Zero) {
            val meta = VideoHelper.getMeta(path) ?: return
            rotation = meta.rotation
            if (rotation == 90 || rotation == 270) {
                intrinsicSize = IntSize(meta.height, meta.width)
            } else {
                intrinsicSize = IntSize(meta.width, meta.height)
            }
        }
    }
    fun initAsync(item: DMessageFile) {
        if (item.fileName.isImageFast()) {
            rotation = ImageHelper.getRotation(path)
            if (item.width > 0 && item.height > 0) {
                intrinsicSize = IntSize(item.width, item.height)
            } else {
                intrinsicSize = ImageHelper.getIntrinsicSize(path, rotation)
            }
        } else {
            val meta = VideoHelper.getMeta(path) ?: return
            rotation = meta.rotation
            if (item.width > 0 && item.height > 0) {
                intrinsicSize = IntSize(item.width, item.height)
            } else {
                val w = meta.width
                val h = meta.height
                intrinsicSize = if (rotation == 90 || rotation == 270) {
                    IntSize(h, w)
                } else {
                    IntSize(w, h)
                }
            }
        }
    }
}
object ItemType {
    const val UNKNOWN = -1
    const val IMAGE = 2
    const val VIDEO = 3
}
