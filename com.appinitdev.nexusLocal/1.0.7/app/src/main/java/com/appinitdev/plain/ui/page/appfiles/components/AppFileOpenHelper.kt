fun openAppFile(
    context: Context,
    files: List<VAppFile>,
    file: VAppFile,
    navController: NavHostController,
    previewerState: MediaPreviewerState,
    itemState: TransformItemState,
) {
    val path = file.appFile.realPath
    val fileName = file.fileName
    when {
        fileName.isImageFast() || fileName.isVideoFast() -> {
            coMain {
                val previewItems = withIO {
                    files.filter { it.fileName.isImageFast() || it.fileName.isVideoFast() }.map {
                        PreviewItem(
                            it.appFile.id,
                            it.appFile.realPath,
                            it.appFile.size,
                            data = DMessageFile(uri = it.appFile.realPath, size = it.appFile.size, fileName = it.fileName),
                        )
                    }
                }
                withIO {
                    MediaPreviewData.setDataAsync(
                        context,
                        itemState,
                        previewItems,
                        PreviewItem(file.appFile.id, path, file.appFile.size, data = DMessageFile(uri = path, size = file.appFile.size, fileName = fileName)),
                    )
                }
                previewerState.openTransform(
                    index = MediaPreviewData.items.indexOfFirst { it.id == file.appFile.id },
                    itemState = itemState,
                )
            }
        }
        fileName.isAudioFast() -> ShareHelper.openPathWith(context, path)
        fileName.isTextFile() -> {
            if (file.appFile.size <= Constants.MAX_READABLE_TEXT_FILE_SIZE) {
                navController.navigateTextFile(path, fileName)
            } else {
                DialogHelper.showMessage(Res.string.text_file_size_limit)
            }
        }
        fileName.isPdfFile() -> {
            try {
                navController.navigatePdf(File(path).toUri())
            } catch (ex: Exception) {
                DialogHelper.showMessage(Res.string.pdf_open_error)
            }
        }
        else -> navController.navigateOtherFile(path, fileName)
    }
}