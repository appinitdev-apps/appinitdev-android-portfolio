@Composable
fun ChatFileItem(
    context: Context,
    items: List<VChat>,
    navController: NavHostController,
    m: VChat,
    peer: DPeer?,
    audioPlaylistVM: AudioPlaylistViewModel,
    previewerState: MediaPreviewerState,
    item: DMessageFile,
    index: Int,
    downloadProgressMap: Map<String, DownloadTask>,
    onShowAudioPlayer: () -> Unit,
) {
    val itemState = rememberTransformItemState()
    val previewPath = item.getPreviewPath(context, peer)
    val path = item.uri.getFinalPath(context)
    val fileName = item.fileName.ifEmpty { path.getFilenameFromPath() }
    val isAudio = fileName.isAudioFast()
    val currentPlayingPath = audioPlaylistVM.selectedPath
    val isCurrentlyPlaying = currentPlayingPath.value == path && isAudio
    val isPlaying by AudioPlayer.isPlayingFlow.collectAsState()
    val showContextMenu = remember { mutableStateOf(false) }
    val downloadTask = downloadProgressMap[item.id]
    val taskActive = downloadTask?.isActive() == true
    val downloadProgress = downloadTask?.let {
        if (it.messageFile.size > 0) it.downloadedSize.toFloat() / it.messageFile.size.toFloat() else 0f
    } ?: 0f
    LaunchedEffect(item.uri) {
        if (item.isRemoteFile() && downloadTask == null && peer != null) {
            DownloadQueue.addDownloadTask(item, peer, m.id)
        }
    }
    var progress by remember(item.id) { mutableFloatStateOf(0f) }
    var duration by remember(item.id) { mutableFloatStateOf(item.duration.toFloat()) }
    var isDraggingProgress by remember(item.id) { mutableStateOf(false) }
    LaunchedEffect(path, isAudio, isCurrentlyPlaying) {
        if (isAudio && isCurrentlyPlaying && duration <= 0f) {
            val loadedDuration = withIO {
                runCatching {
                    DPlaylistAudio.fromPath(context, path).duration.toFloat()
                }.getOrDefault(0f)
            }
            if (loadedDuration > 0f) {
                duration = loadedDuration
            }
        }
    }
    LaunchedEffect(isCurrentlyPlaying, isPlaying, isDraggingProgress) {
        if (isCurrentlyPlaying) {
            while (isActive) {
                if (!isDraggingProgress) {
                    progress = AudioPlayer.playerProgress / 1000f
                }
                delay(500)
            }
        }
    }
    Column {
        ChatFileItemContent(
            context = context,
            items = items,
            navController = navController,
            item = item,
            itemState = itemState,
            previewerState = previewerState,
            fileName = fileName,
            path = path,
            previewPath = previewPath,
            isActive = taskActive,
            downloadProgress = downloadProgress,
            downloadTask = downloadTask,
            isCurrentlyPlaying = isCurrentlyPlaying,
            showContextMenu = showContextMenu,
            index = index,
        )
        if (isCurrentlyPlaying) {
            ChatAudioInlineControls(
                progress = progress,
                duration = duration,
                isPlaying = isPlaying,
                onProgressChange = { newProgress ->
                    isDraggingProgress = true
                    if (duration > 0f) {
                        progress = newProgress * duration
                    }
                },
                onValueChangeFinished = {
                    if (duration > 0f) {
                        AudioPlayer.seekTo(progress.toLong())
                    }
                    isDraggingProgress = false
                },
                onShowFullPlayer = onShowAudioPlayer,
            )
        }
    }
}
