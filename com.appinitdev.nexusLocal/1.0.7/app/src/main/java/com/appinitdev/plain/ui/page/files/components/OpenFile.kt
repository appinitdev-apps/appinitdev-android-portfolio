fun openFile(
    context: Context,
    files: List<DFile>,
    file: DFile,
    navController: NavHostController,
    previewerState: MediaPreviewerState,
    itemState: TransformItemState,
    audioPlaylistVM: AudioPlaylistViewModel? = null,
) {
    if (ZipBrowserHelper.isZipPath(file.path)) {
        coMain {
            DialogHelper.showLoading()
            val tempFile = withIO { ZipBrowserHelper.extractEntryToCache(context, file.path) }
            DialogHelper.hideLoading()
            if (tempFile == null) {
                DialogHelper.showMessage(Res.string.error)
                return@coMain
            }
            val tempPath = tempFile.absolutePath
            val extracted = file.copy(path = tempPath)
            when {
                tempPath.isImageFast() || tempPath.isVideoFast() -> {
                    withIO {
                        MediaPreviewData.setDataAsync(
                            context, itemState,
                            listOf(extracted).map { it.toPreviewItem() },
                            extracted.toPreviewItem(),
                        )
                    }
                    previewerState.openTransform(
                        index = 0,
                        itemState = itemState,
                    )
                }
                else -> {
                    openFile(context, listOf(extracted), extracted, navController, previewerState, itemState, audioPlaylistVM)
                }
            }
        }
        return
    }
    val path = file.path
    when {
        path.isImageFast() || path.isVideoFast() -> {
            coMain {
                withIO {
                    MediaPreviewData.setDataAsync(
                        context, itemState,
                        files.filter { it.path.isImageFast() || it.path.isVideoFast() }.map { it.toPreviewItem() },
                        file.toPreviewItem(),
                    )
                }
                previewerState.openTransform(
                    index = MediaPreviewData.items.indexOfFirst { it.id == file.path },
                    itemState = itemState,
                )
            }
        }
        path.isAudioFast() -> {
            try {
                Permissions.checkNotification(context, Res.string.audio_notification_prompt) {
                    val audio = DPlaylistAudio.fromPath(context, path)
                    if (audioPlaylistVM != null) {
                        coMain {
                            withIO { audioPlaylistVM.playlistItems.value = listOf(audio) }
                            audioPlaylistVM.selectedPath.value = path
                            AudioPlayer.play(context, audio)
                        }
                    } else {
                        AudioPlayer.play(context, audio)
                    }
                }
            } catch (ex: Exception) {
                DialogHelper.showMessage(Res.string.audio_play_error)
            }
        }
        path.isTextFile() -> {
            if (file.size <= Constants.MAX_READABLE_TEXT_FILE_SIZE) {
                navController.navigateTextFile(path)
            } else {
                DialogHelper.showMessage(Res.string.text_file_size_limit)
            }
        }
        path.isPdfFile() -> {
            try {
                navController.navigatePdf(File(path).toUri())
            } catch (ex: Exception) {
                DialogHelper.showMessage(Res.string.pdf_open_error)
            }
        }
        else -> {
            ShareHelper.openPathWith(context, path)
        }
    }
}
