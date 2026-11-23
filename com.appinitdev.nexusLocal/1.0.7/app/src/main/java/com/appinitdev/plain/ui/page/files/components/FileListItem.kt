@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListItem(
    file: DFile, isSelected: Boolean, isSelectMode: Boolean,
    itemState: TransformItemState, previewerState: MediaPreviewerState,
    onClick: () -> Unit, onLongClick: () -> Unit, audioPlaylistVM: AudioPlaylistViewModel,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isAudio = file.path.isAudioFast()
    val isCurrentlyPlaying = audioPlaylistVM.selectedPath.value == file.path && isAudio
    val isPlaying by AudioPlayer.isPlayingFlow.collectAsState()
    var progress by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableFloatStateOf(0f) }
    var showAudioPlayer by remember { mutableStateOf(false) }
    LaunchedEffect(isCurrentlyPlaying) {
        if (isCurrentlyPlaying && isAudio) {
            scope.launch { withIO {
                val audio = DPlaylistAudio.fromPath(context, file.path)
                duration = audio.duration.toFloat()
            } }
        }
    }
    var progressUpdateJob: Job? = null
    LaunchedEffect(isCurrentlyPlaying, isPlaying) {
        progressUpdateJob?.cancel()
        if (isCurrentlyPlaying && isPlaying) {
            progressUpdateJob = scope.launch {
                while (isActive) { progress = AudioPlayer.playerProgress / 1000f; delay(500) }
            }
        }
    }
    Column {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp,
                bottomStart = if (isCurrentlyPlaying) 0.dp else 8.dp,
                bottomEnd = if (isCurrentlyPlaying) 0.dp else 8.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (isSelectMode) {
                    Checkbox(checked = isSelected, onCheckedChange = null)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                FileListItemThumbnail(file, itemState, previewerState, context)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = file.name, style = MaterialTheme.typography.bodyLarge,
                        color = if (isCurrentlyPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                    VerticalSpace(4.dp)
                    Text(text = if (file.isDir) pluralStringResource(Res.plurals.items, file.children, file.children) + ", " + file.updatedAt.formatDateTime()
                        else file.size.formatBytes() + ", " + file.updatedAt.formatDateTime(),
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (isCurrentlyPlaying) {
            FileListItemPlayer(isSelected = isSelected, isPlaying = isPlaying, progress = progress, duration = duration,
                onProgressChange = { newProgress -> progress = newProgress * duration },
                onShowFullPlayer = { showAudioPlayer = true })
        }
    }
    if (showAudioPlayer) {
        AudioPlayerPage(audioPlaylistVM, onDismissRequest = { showAudioPlayer = false })
    }
}
@Composable
private fun FileListItemThumbnail(
    file: DFile, itemState: TransformItemState, previewerState: MediaPreviewerState, context: android.content.Context,
) {
    val isMedia = file.path.isImageFast() || file.path.isVideoFast()
    val isZipEntry = ZipBrowserHelper.isZipPath(file.path)
    var zipCachedPath by remember(file.path) { mutableStateOf<String?>(null) }
    if (isMedia && isZipEntry) {
        LaunchedEffect(file.path) {
            zipCachedPath = withIO { ZipBrowserHelper.extractEntryToCache(context, file.path)?.absolutePath }
        }
    }
    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
        when {
            isMedia && !isZipEntry -> {
                TransformImageView(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)),
                    path = file.path, fileName = file.name, key = file.path,
                    itemState = itemState, previewerState = previewerState, widthPx = context.dp2px(48))
            }
            isMedia && zipCachedPath != null -> {
                TransformImageView(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)),
                    path = zipCachedPath!!, fileName = file.name, key = file.path,
                    itemState = itemState, previewerState = previewerState, widthPx = context.dp2px(48))
            }
            else -> {
                AsyncImage(
                    model = if (file.isDir) AppHelper.getFileIconPath("folder") else AppHelper.getFileIconPath(file.path.getFilenameExtension()),
                    modifier = Modifier.size(48.dp), alignment = Alignment.Center, contentDescription = file.path,
                )
            }
        }
    }
}
