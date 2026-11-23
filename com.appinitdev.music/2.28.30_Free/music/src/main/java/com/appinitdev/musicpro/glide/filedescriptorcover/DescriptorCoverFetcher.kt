
class DescriptorCoverFetcher(private val descriptorCoverModel: DescriptorCoverModel) : DataFetcher<InputStream> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        var file: AssetFileDescriptor? = null

        try {
            if (URLUtil.isValidUrl(descriptorCoverModel.fileUri.toString()) &&
                (descriptorCoverModel.fileUri.toString().startsWith("http")
                        || descriptorCoverModel.fileUri.toString().startsWith("https")
                        || descriptorCoverModel.fileUri.toString().startsWith("ftp"))) {
                mediaMetadataRetriever.setDataSource(descriptorCoverModel.fileUri.toString(), hashMapOf())
            } else {
                file = descriptorCoverModel.context.contentResolver.openAssetFileDescriptor(descriptorCoverModel.fileUri, "r")
                mediaMetadataRetriever.setDataSource(file?.fileDescriptor)
            }

            callback.onDataReady(ByteArrayInputStream(mediaMetadataRetriever.embeddedPicture))
        } catch (e: IOException) {
            callback.onLoadFailed(e)
        } catch (e: FileNotFoundException) {
            callback.onLoadFailed(e)
        } catch (e: IllegalArgumentException) {
            callback.onLoadFailed(e)
        } catch (e: NullPointerException) {
            callback.onLoadFailed(e)
        } finally {
            mediaMetadataRetriever.release()
            mediaMetadataRetriever.close()
            file?.close()
        }
    }

    override fun cleanup() {
        /* no-op */
    }

    override fun cancel() {
        /* no-op */
    }

    override fun getDataClass(): Class<InputStream> {
        return InputStream::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }
}