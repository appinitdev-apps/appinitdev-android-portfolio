@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerPage(audioPlaylistVM: AudioPlaylistViewModel, onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var progress by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableFloatStateOf(0f) }
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    val isPlaying by AudioPlayer.isPlayingFlow.collectAsState()
    val playMode by TempData.audioPlayMode.collectAsState()
    var showPlaylist by remember { mutableStateOf(false) }
    var showSleepTimer by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var isTimerActive by remember { mutableStateOf(false) }
    val currentPlayingPath = audioPlaylistVM.selectedPath
    LaunchedEffect(currentPlayingPath.value) {
        scope.launch {
            val path = currentPlayingPath.value
            if (path.isNotEmpty()) {
                val audio = withIO { DPlaylistAudio.fromPath(context, path) }
                duration = audio.duration.toFloat()
                if (!isDragging) progress = AudioPlayer.playerProgress / 1000f
                title = audio.title; artist = audio.artist
            }
            isTimerActive = TempData.audioSleepTimerFutureTime > SystemClock.elapsedRealtime()
        }
    }
    LaunchedEffect(isPlaying, isDragging) {
        if (isPlaying && !isDragging) { while (true) { progress = AudioPlayer.playerProgress / 1000f; delay(1000) } }
    }
    LaunchedEffect(Unit) {
        while (true) { isTimerActive = TempData.audioSleepTimerFutureTime > SystemClock.elapsedRealtime(); delay(1000) }
    }
    PModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 32.dp).height(280.dp), contentAlignment = Alignment.Center) {
                AudioPlayerCover(path = currentPlayingPath.value)
            }
            AudioPlayerTrackInfo(title = title, artist = artist)
            VerticalSpace(32.dp)
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
                WaveSlider(
                    value = progress, onValueChange = { isDragging = true; progress = minOf(it, duration) },
                    onValueChangeFinished = { if (duration > 0 && progress >= 0) AudioPlayer.seekTo(progress.toLong()); isDragging = false },
                    valueRange = 0f..maxOf(duration, 1f), modifier = Modifier.fillMaxWidth().height(32.dp), isPlaying = isPlaying,
                )
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = (AudioPlayer.playerProgress / 1000).formatDuration(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = duration.toLong().formatDuration(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            AudioPlayerControls(
                playMode = playMode, onPlayModeChange = {}, isTimerActive = isTimerActive,
                onSleepTimer = { showSleepTimer = true }, onPlaylist = { showPlaylist = true }, isPlaying = isPlaying, scope = scope,
            )
        }
    }
    if (showSleepTimer) {
        SleepTimerPage(onDismissRequest = { showSleepTimer = false; isTimerActive = TempData.audioSleepTimerFutureTime > SystemClock.elapsedRealtime() })
    }
    if (showPlaylist) {
        AudioPlaylistPage(audioPlaylistVM, onDismissRequest = { showPlaylist = false })
    }
}