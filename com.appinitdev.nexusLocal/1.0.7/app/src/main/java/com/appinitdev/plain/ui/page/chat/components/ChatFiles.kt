
@Composable
fun ChatFiles(
    context: Context,
    items: List<VChat>,
    navController: NavHostController,
    m: VChat,
    peer: DPeer?,
    audioPlaylistVM: AudioPlaylistViewModel,
    previewerState: MediaPreviewerState,
) {
    val fileItems = (m.value as DMessageFiles).items
    val currentPlayingPath = audioPlaylistVM.selectedPath
    var showAudioPlayer by remember { mutableStateOf(false) }
    val downloadProgressMap by DownloadQueue.downloadProgress.collectAsState(mapOf())

    LaunchedEffect(currentPlayingPath.value) {
        if (showAudioPlayer) {
            showAudioPlayer = currentPlayingPath.value.isNotEmpty()
        }
    }

    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.cardBackgroundNormal)
    ) {
        fileItems.forEachIndexed { index, item ->
            ChatFileItem(
                context = context,
                items = items,
                navController = navController,
                m = m,
                peer = peer,
                audioPlaylistVM = audioPlaylistVM,
                previewerState = previewerState,
                item = item,
                index = index,
                downloadProgressMap = downloadProgressMap,
                onShowAudioPlayer = { showAudioPlayer = true },
            )
        }
    }

    if (showAudioPlayer) {
        AudioPlayerPage(
            audioPlaylistVM,
            onDismissRequest = { showAudioPlayer = false },
        )
    }
}
