@Composable
fun AudioPlayerBar(audioPlaylistVM: AudioPlaylistViewModel, castVM: CastViewModel, modifier: Modifier = Modifier, dragSelectState: DragSelectState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var progress by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableFloatStateOf(1f) }
    val isPlaying by AudioPlayer.isPlayingFlow.collectAsState()
    var showPlayer by remember { mutableStateOf(false) }
    var showSleepTimer by remember { mutableStateOf(false) }
    var showPlaylist by remember { mutableStateOf(false) }
    val currentPlayingPath = audioPlaylistVM.selectedPath
    LaunchedEffect(currentPlayingPath.value) {
        scope.launch {
            val path = currentPlayingPath.value
            if (path.isNotEmpty()) {
                val audio = withIO { DPlaylistAudio.fromPath(context, path) }
                title = audio.title; artist = audio.artist
                duration = audio.duration.toFloat(); progress = AudioPlayer.playerProgress / 1000f
            }
            if (showPlayer) showPlayer = path.isNotEmpty()
        }
    }
    var progressUpdateJob: Job? = null
    LaunchedEffect(isPlaying) {
        progressUpdateJob?.cancel()
        if (isPlaying) {
            progressUpdateJob = scope.launch {
                while (isActive) { progress = AudioPlayer.playerProgress / 1000f; delay(1000) }
            }
        }
    }
    AnimatedVisibility(
        visible = currentPlayingPath.value.isNotEmpty() && !dragSelectState.selectMode && !castVM.castMode.value,
        enter = slideInVertically { it }, exit = slideOutVertically { it }, modifier = modifier
    ) {
        AudioPlayerBarCard(
            title = title, artist = artist, progress = progress, duration = duration,
            isPlaying = isPlaying,
            onClickContent = { showPlayer = true }, onClickPlaylist = { showPlaylist = true }
        )
    }
    if (showPlayer) AudioPlayerPage(audioPlaylistVM, onDismissRequest = { showPlayer = false })
    if (showSleepTimer) SleepTimerPage(onDismissRequest = { showSleepTimer = false })
    if (showPlaylist) AudioPlaylistPage(audioPlaylistVM, onDismissRequest = { showPlaylist = false })
}
