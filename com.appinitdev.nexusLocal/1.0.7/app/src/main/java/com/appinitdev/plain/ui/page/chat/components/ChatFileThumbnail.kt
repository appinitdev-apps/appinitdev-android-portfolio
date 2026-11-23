
@Composable
fun ChatFileThumbnail(
    context: Context,
    fileName: String,
    previewPath: String,
    item: DMessageFile,
    itemState: TransformItemState,
    previewerState: MediaPreviewerState,
    isActive: Boolean,
    downloadProgress: Float,
    downloadTask: DownloadTask?,
) {
    Box {
        if (fileName.isImageFast() || fileName.isVideoFast()) {
            TransformImageView(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(4.dp)),
                path = previewPath,
                fileName = fileName,
                key = item.id,
                itemState = itemState,
                previewerState = previewerState,
                widthPx = context.dp2px(48),
                forceVideoDecoder = fileName.isVideoFast() && !item.isRemoteFile(),
            )
        } else {
            AsyncImage(
                model = AppHelper.getFileIconPath(fileName.getFilenameExtension()),
                modifier = Modifier.size(48.dp),
                alignment = Alignment.Center,
                contentDescription = fileName,
            )
        }

        if (isActive && downloadTask != null) {
            DownloadProgressOverlay(
                modifier = Modifier.size(48.dp),
                downloadProgress = downloadProgress,
                status = downloadTask.status,
                onPause = { DownloadQueue.pauseDownload(item.id) },
                onResume = { DownloadQueue.resumeDownload(item.id) },
                onCancel = { DownloadQueue.removeDownload(item.id) },
                size = 32.dp,
            )
        }
    }
}
