
internal fun MainActivity.handlePickFileEvent(event: PickFileEvent) {
    try {
        pickFileType = event.type
        pickFileTag = event.tag
        var type: ActivityResultContracts.PickVisualMedia.VisualMediaType? = null
        when (event.type) {
            PickFileType.IMAGE_VIDEO -> type = ActivityResultContracts.PickVisualMedia.ImageAndVideo
            PickFileType.IMAGE -> type = ActivityResultContracts.PickVisualMedia.ImageOnly
            else -> {}
        }
        if (type != null) {
            try {
                if (event.multiple) pickMultipleMedia.launch(PickVisualMediaRequest(type))
                else pickMedia.launch(PickVisualMediaRequest(type))
            } catch (e: ActivityNotFoundException) {
                LogCat.e("Photo picker not available, falling back to file picker")
                doPickFile(event)
            }
        } else {
            doPickFile(event)
        }
    } catch (e: IllegalStateException) {
        LogCat.e("Error launching pick file: ${e.message}")
    }
}

internal fun MainActivity.handleExportFileEvent(event: ExportFileEvent) {
    try {
        exportFileType = event.type
        exportFileActivityLauncher.launch(
            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                type = when (event.type) {
                    ExportFileType.BACKUP -> "application/zip"
                    else -> "text/*"
                }
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(Intent.EXTRA_TITLE, event.fileName)
            },
        )
    } catch (e: ActivityNotFoundException) {
        LogCat.e("No document creation app available")
        DialogHelper.showMessage(Res.string.file_picker_not_available)
    } catch (e: IllegalStateException) {
        LogCat.e("Error launching export file: ${e.message}")
    }
}

internal fun MainActivity.doPickFile(event: PickFileEvent) {
    try {
        val intent = when (event.type) {
            PickFileType.FOLDER -> FilePickHelper.getPickFolderIntent()
            else if (isQPlus()) -> FilePickHelper.getPickFileIntent(event.multiple)
            else -> FilePickHelper.getFallbackPickFileIntent(event.multiple)
        }
        pickFileActivityLauncher.launch(intent)
    } catch (e: ActivityNotFoundException) {
        LogCat.e("No file picker available on this device")
        DialogHelper.showErrorMessage(LocaleHelper.getString(Res.string.file_picker_not_available))
    } catch (e: IllegalStateException) {
        LogCat.e("Error launching pick file activity: ${e.message}")
    }
}
