
@OptIn(UnstableApi::class)
@Composable
fun DlnaReceiverVideoPlayer(vm: DlnaReceiverViewModel, onExit: () -> Unit) {
    val context = LocalContext.current
    val mediaUri by DlnaRendererState.mediaUri.collectAsState()
    val mediaTitle by DlnaRendererState.mediaTitle.collectAsState()
    val playbackState by DlnaRendererState.playbackState.collectAsState()
    val seekTargetMs by DlnaRendererState.seekTargetMs.collectAsState()
    val positionMs by DlnaRendererState.currentPositionMs.collectAsState()
    val durationMs by DlnaRendererState.durationMs.collectAsState()

    val player = rememberVideoPlayer(context)
    val playerView = remember { PlayerView(context) }
    var showControls by remember { mutableStateOf(true) }
    var controlsSeed by remember { mutableIntStateOf(0) }

    LaunchedEffect(mediaUri) {
        if (mediaUri.isNotEmpty()) {
            player.playWhenReady = false
            player.setMediaItem(MediaItem.fromUri(mediaUri))
            player.prepare()
        }
    }
    LaunchedEffect(playbackState) {
        when (playbackState) {
            DlnaPlaybackState.PLAYING -> player.play()
            DlnaPlaybackState.PAUSED -> player.pause()
            DlnaPlaybackState.STOPPED -> { player.pause(); player.seekTo(0) }
            else -> {}
        }
    }
    LaunchedEffect(seekTargetMs) { seekTargetMs?.let { player.seekTo(it) } }
    LaunchedEffect(player) { vm.startPositionSync(player) }
    LaunchedEffect(showControls, controlsSeed) {
        if (showControls) { delay(4.seconds); showControls = false }
    }
    DisposableEffect(Unit) {
        context.findActivity().setFullScreen(true)
        onDispose { context.findActivity().setFullScreen(false); player.stop(); player.release() }
    }

    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black)
            .clickable(indication = null, interactionSource = interactionSource) {
                showControls = !showControls; if (showControls) controlsSeed++
            },
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { playerView.apply { this.player = player; useController = false; resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT } },
        )
        AnimatedVisibility(visible = showControls, enter = fadeIn(), exit = fadeOut()) {
            DlnaVideoControls(
                mediaTitle = mediaTitle, positionMs = positionMs, durationMs = durationMs,
                isPlaying = playbackState == DlnaPlaybackState.PLAYING,
                onPlayPause = {
                    if (player.isPlaying) {
                        player.pause()
                        DlnaRendererState.playbackState.value = DlnaPlaybackState.PAUSED
                    } else {
                        player.play()
                        DlnaRendererState.playbackState.value = DlnaPlaybackState.PLAYING
                    }
                },
                onSeek = { ratio -> player.seekTo((ratio * durationMs).toLong()) },
                onExit = onExit,
            )
        }
    }
}

@Composable
private fun DlnaVideoControls(
    mediaTitle: String,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onExit: () -> Unit,
) {
    val darkOverlay = Color.Black.copy(alpha = 0.55f)
    Column(modifier = Modifier.fillMaxSize()) {
        
        Row(
            modifier = Modifier.fillMaxWidth().background(darkOverlay).statusBarsPadding().padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onExit) {
                Icon(painter = painterResource(Res.drawable.arrow_left), contentDescription = stringResource(Res.string.dlna_receiver_exit_player), tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Text(text = mediaTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(end = 8.dp))
        }
        
        Box(modifier = Modifier.weight(1f))
        
        Column(
            modifier = Modifier.fillMaxWidth().background(darkOverlay).navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val progress = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f
            PlayerSlider(modifier = Modifier.fillMaxWidth().height(24.dp), progress = progress, bufferedProgress = 0f, onProgressChange = onSeek)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = positionMs.formatMinSec(), style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.width(52.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    IconButton(modifier = Modifier.size(48.dp), onClick = onPlayPause) {
                        Icon(painter = painterResource(if (isPlaying) Res.drawable.pause else Res.drawable.play_arrow), contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
                Text(text = durationMs.formatMinSec(), style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.width(52.dp))
            }
        }
    }
}
