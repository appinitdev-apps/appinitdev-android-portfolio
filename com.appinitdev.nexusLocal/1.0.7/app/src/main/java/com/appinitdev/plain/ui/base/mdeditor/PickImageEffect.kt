
@Composable
internal fun PickImageEffect(
    context: Context,
    imageUrl: MutableState<String>,
) {
    val sharedFlow = Channel.sharedFlow

    LaunchedEffect(sharedFlow) {
        sharedFlow.collect { event ->
            when (event) {
                is PickFileResultEvent -> {
                    if (event.tag != PickFileTag.EDITOR) {
                        return@collect
                    }
                    val uri = event.uris.firstOrNull() ?: return@collect
                    try {
                        val fileName = uri.getFileName(context).ifEmpty {
                            "image_${System.currentTimeMillis()}.jpg"
                        }.replace("/", "_")
                        if (fileName.isNotEmpty()) {
                            val noteImagesDir = File(context.appDir(), "note-images")
                            if (!noteImagesDir.exists()) {
                                noteImagesDir.mkdirs()
                            }
                            val dstFile = File(noteImagesDir, fileName)
                            val path =
                                if (dstFile.exists()) {
                                    dstFile.newPath()
                                } else {
                                    dstFile.absolutePath
                                }
                            FileHelper.copyFile(context, uri, path)
                            imageUrl.value = "app://note-images/${path.getFilenameFromPath()}"
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }
        }
    }
}
