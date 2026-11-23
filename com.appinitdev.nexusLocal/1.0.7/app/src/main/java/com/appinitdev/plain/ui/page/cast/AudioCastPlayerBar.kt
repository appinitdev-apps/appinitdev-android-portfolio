@Composable
fun AudioCastPlayerBar(
    castVM: CastViewModel,
    modifier: Modifier = Modifier,
    dragSelectState: DragSelectState,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var showCastPlaylist by remember { mutableStateOf(false) }
    val isPlaying by CastPlayer.isPlaying.collectAsState()
    val progress by CastPlayer.progress.collectAsState()
    val duration by CastPlayer.duration.collectAsState()
    val supportsCallback by CastPlayer.supportsCallback.collectAsState()
    val currentUri by CastPlayer.currentUri.collectAsState()
    LaunchedEffect(currentUri) {
        scope.launch {
            if (currentUri.isNotEmpty()) {
                val audio = withIO { DPlaylistAudio.fromPath(context, currentUri) }
                title = audio.title
                artist = audio.artist
            } else {
                title = ""
                artist = ""
            }
        }
    }
    AnimatedVisibility(
        visible = castVM.castMode.value && CastPlayer.currentDevice != null && !dragSelectState.selectMode,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = modifier,
    ) {
        ElevatedCard(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(PlainTheme.CARD_RADIUS),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            AudioCastPlayerBarContent(
                castVM = castVM,
                title = title, artist = artist,
                isPlaying = isPlaying, progress = progress, duration = duration,
                supportsCallback = supportsCallback, currentUri = currentUri,
                onShowPlaylist = { showCastPlaylist = true },
            )
        }
    }
    if (showCastPlaylist) {
        AudioCastPlaylistPage(castVM = castVM, onDismissRequest = { showCastPlaylist = false })
    }
}
