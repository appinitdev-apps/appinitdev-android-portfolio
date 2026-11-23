
@Composable
internal fun ChatImageItem(
    context: Context,
    item: DMessageFile,
    items: List<VChat>,
    messageId: String,
    peer: DPeer?,
    imageWidthDp: Dp,
    imageWidthPx: Int,
    previewerState: MediaPreviewerState,
    downloadTask: DownloadTask?,
) {
    val itemState = rememberTransformItemState()
    val isRemoteFile = item.isRemoteFile()
    val taskActive = downloadTask?.isActive() == true
    val downloadProgress = downloadTask?.let {
        if (it.messageFile.size > 0) it.downloadedSize.toFloat() / it.messageFile.size.toFloat() else 0f
    } ?: 0f

    LaunchedEffect(item.uri) {
        if (isRemoteFile && downloadTask == null && peer != null) {
            DownloadQueue.addDownloadTask(item, peer, messageId)
        }
    }

    Box(
        modifier = Modifier.clickable {
            if (taskActive) return@clickable
            coMain {
                withIO { MediaPreviewData.setDataAsync(context, itemState, items.reversed(), item) }
                previewerState.openTransform(
                    index = MediaPreviewData.items.indexOfFirst { it.id == item.id },
                    itemState = itemState,
                )
            }
        },
    ) {
        TransformImageView(
            modifier = Modifier.size(imageWidthDp).clip(RoundedCornerShape(6.dp)),
            path = item.getPreviewPath(context, peer),
            fileName = item.fileName,
            key = item.id,
            itemState = itemState,
            previewerState = previewerState,
            widthPx = imageWidthPx,
            forceVideoDecoder = item.fileName.isVideoFast() && !item.isRemoteFile(),
        )

        if (taskActive) {
            DownloadProgressOverlay(
                modifier = Modifier.size(imageWidthDp),
                downloadProgress = downloadProgress,
                status = downloadTask.status,
                onPause = { DownloadQueue.pauseDownload(item.id) },
                onResume = { DownloadQueue.resumeDownload(item.id) },
                onCancel = { DownloadQueue.removeDownload(item.id) },
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .clip(RoundedCornerShape(bottomEnd = 6.dp))
                .background(Color.Black.copy(alpha = 0.4f)),
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                text = if (item.duration > 0) item.duration.formatDuration() else item.size.formatBytes(),
                color = Color.White,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
            )
        }
    }
}
