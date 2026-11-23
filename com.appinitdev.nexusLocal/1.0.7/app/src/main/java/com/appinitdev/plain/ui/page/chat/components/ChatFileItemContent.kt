@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ChatFileItemContent(
    context: Context,
    items: List<VChat>,
    navController: NavHostController,
    item: DMessageFile,
    itemState: TransformItemState,
    previewerState: MediaPreviewerState,
    fileName: String,
    path: String,
    previewPath: String,
    isActive: Boolean,
    downloadProgress: Float,
    downloadTask: DownloadTask?,
    isCurrentlyPlaying: Boolean,
    showContextMenu: MutableState<Boolean>,
    index: Int,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isActive) return@combinedClickable
                    if (fileName.isImageFast() || fileName.isVideoFast()) {
                        coMain {
                            keyboardController?.hide()
                            withIO { MediaPreviewData.setDataAsync(context, itemState, items.reversed(), item) }
                            previewerState.openTransform(
                                index = MediaPreviewData.items.indexOfFirst { it.id == item.id },
                                itemState = itemState,
                            )
                        }
                    } else if (fileName.isAudioFast()) {
                        Permissions.checkNotification(context, Res.string.audio_notification_prompt) {
                            AudioPlayer.play(context, DPlaylistAudio.fromPath(context, path))
                        }
                    } else if (fileName.isTextFile()) {
                        navController.navigateTextFile(path, fileName, mediaId = "", type = TextFileType.CHAT)
                    } else if (fileName.isPdfFile()) {
                        navController.navigatePdf(File(path).toUri(), fileName)
                    } else {
                        navController.navigateOtherFile(path, fileName)
                    }
                },
                onLongClick = {
                    showContextMenu.value = true
                },
            ),
    ) {
        PDropdownMenu(
            expanded = showContextMenu.value,
            onDismissRequest = { showContextMenu.value = false },
        ) {
            PDropdownMenuItem(
                text = { Text(stringResource(Res.string.save)) },
                onClick = {
                    showContextMenu.value = false
                    coMain {
                        val result = withIO { FileHelper.copyFileToDownloads(path, fileName) }
                        if (result.isNotEmpty()) {
                            DialogHelper.showConfirmDialog("", LocaleHelper.getStringFAsync(Res.string.file_save_to, "path", result))
                        } else {
                            DialogHelper.showErrorMessage(result)
                        }
                    }
                },
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (index == 0) 16.dp else 6.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChatFileInfo(
                modifier = Modifier.weight(1f),
                fileName = fileName,
                size = item.size,
                duration = item.duration,
                summary = item.summary,
                isCurrentlyPlaying = isCurrentlyPlaying,
            )
            ChatFileThumbnail(
                context = context,
                fileName = fileName,
                previewPath = previewPath,
                item = item,
                itemState = itemState,
                previewerState = previewerState,
                isActive = isActive,
                downloadProgress = downloadProgress,
                downloadTask = downloadTask,
            )
        }
    }
}
