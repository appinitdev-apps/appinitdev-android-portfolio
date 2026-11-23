fun File.setLastModified(localFileHeader: LocalFileHeader) {
    setLastModified(localFileHeader.lastModifiedOrCurrentTimeMillis)
}
private val LocalFileHeader.lastModifiedOrCurrentTimeMillis
    get() = if (lastModifiedTimeEpoch == 0L) {
        System.currentTimeMillis()
    } else {
        lastModifiedTimeEpoch
    }
