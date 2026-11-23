class ForceVideoDecoder(
    private val source: ImageSource,
    private val options: Options,
) : Decoder {
    override suspend fun decode(): DecodeResult {
        val filePath = source.file().toString()
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(filePath)
            val bitmap = retriever.getFrameAtTime(-1L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: throw kotlinx.io.IOException("ForceVideoDecoder: failed to extract video frame from $filePath")
            return DecodeResult(
                image = bitmap.toDrawable(options.context.resources).asImage(),
                isSampled = false,
            )
        } finally {
            retriever.release()
        }
    }
    class Factory : Decoder.Factory {
        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader,
        ): Decoder? {
            return ForceVideoDecoder(result.source, options)
        }
    }
}
