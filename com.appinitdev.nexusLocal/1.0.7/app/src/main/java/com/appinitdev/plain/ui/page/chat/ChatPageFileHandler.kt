
internal fun handleFileSelection(
    event: PickFileResultEvent,
    context: Context,
    chatVM: ChatViewModel,
    scrollState: LazyListState,
    focusManager: FocusManager,
) {
    coMain {
        val placeholderItems = mutableListOf<DMessageFile>()
        event.uris.forEach { uri ->
            val file = context.contentResolver.queryOpenableFile(uri)
            if (file != null) {
                var fileName = file.displayName
                val mimeType = context.contentResolver.getType(uri) ?: ""
                if (event.type == PickFileType.IMAGE_VIDEO) {
                    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: ""
                    if (extension.isNotEmpty()) {
                        fileName = fileName.getFilenameWithoutExtension() + "." + extension
                    }
                }
                placeholderItems.add(
                    DMessageFile(id = StringHelper.shortUUID(), uri = uri.toString(), size = file.size, fileName = fileName)
                )
            }
        }
        if (placeholderItems.isEmpty()) return@coMain

        val isImageVideo = event.type == PickFileType.IMAGE_VIDEO
        val messageId = withIO { chatVM.sendFilesImmediate(placeholderItems, isImageVideo) }
        scrollToLatest(chatVM, scrollState, null)
        delay(200)
        focusManager.clearFocus()

        withIO {
            val finalItems = mutableListOf<DMessageFile>()
            event.uris.forEachIndexed { index, uri ->
                try {
                    val placeholder = placeholderItems[index]
                    val mimeType = context.contentResolver.getType(uri) ?: ""
                    val fidUri = ChatFileSaveHelper.importFromUri(context, uri, mimeType)
                    val realPath = AppFileStore.resolveUri(context, fidUri)
                    val intrinsicSize = if (placeholder.fileName.isImageFast())
                        ImageHelper.getIntrinsicSize(realPath, ImageHelper.getRotation(realPath))
                    else if (placeholder.fileName.isVideoFast())
                        VideoHelper.getIntrinsicSize(realPath)
                    else IntSize.Zero
                    finalItems.add(
                        DMessageFile(
                            id = placeholder.id, uri = fidUri, size = placeholder.size,
                            duration = java.io.File(realPath).getDuration(context),
                            width = intrinsicSize.width, height = intrinsicSize.height,
                            summary = placeholder.summary, fileName = placeholder.fileName,
                        )
                    )
                } catch (ex: Exception) {
                    DialogHelper.showMessage(ex)
                    ex.printStackTrace()
                    finalItems.add(placeholderItems[index])
                }
            }
            chatVM.updateFilesMessage(messageId, finalItems, isImageVideo)
        }
    }
}

internal suspend fun scrollToLatest(
    chatVM: ChatViewModel,
    scrollState: LazyListState,
    previousTopMessageId: String?,
) {
    repeat(20) {
        val currentTopMessageId = chatVM.itemsFlow.value.firstOrNull()?.id
        if (currentTopMessageId != null && currentTopMessageId != previousTopMessageId) {
            scrollState.scrollToItem(0)
            return
        }
        delay(50)
    }
    scrollState.scrollToItem(0)
}
