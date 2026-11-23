@OptIn(UnstableApi::class)
@Composable
fun DlnaReceiverAudioPlayer(vm: DlnaReceiverViewModel, onExit: () -> Unit) {
    val context = LocalContext.current
    val mediaUri by DlnaRendererState.mediaUri.collectAsState()
    val mediaTitle by DlnaRendererState.mediaTitle.collectAsState()
    val albumArtUri by DlnaRendererState.mediaAlbumArtUri.collectAsState()
    var albumArtFailed by remember(albumArtUri) { mutableStateOf(false) }
    val playbackState by DlnaRendererState.playbackState.collectAsState()
    val seekTargetMs by DlnaRendererState.seekTargetMs.collectAsState()
    val positionMs by DlnaRendererState.currentPositionMs.collectAsState()
    val durationMs by DlnaRendererState.durationMs.collectAsState()
    val player = rememberVideoPlayer(context)
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
    DisposableEffect(Unit) {
        context.findActivity().setFullScreen(true)
        onDispose { context.findActivity().setFullScreen(false); player.stop(); player.release() }
    }
    val gradient = Brush.verticalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460)))
    Box(
        modifier = Modifier.fillMaxSize().background(gradient),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onExit) {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_left),
                        contentDescription = stringResource(Res.string.dlna_receiver_exit_player),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                modifier = Modifier.size(220.dp).clip(RoundedCornerShape(24.dp)),
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(24.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (albumArtUri.isNotEmpty() && !albumArtFailed) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(albumArtUri).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            onError = { albumArtFailed = true },
                        )
                    } else {
                        Icon(
                            painter = painterResource(Res.drawable.music2),
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(96.dp),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(36.dp))
            Text(
                text = mediaTitle.ifEmpty { stringResource(Res.string.unknown) },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.weight(1f))
            AudioPlayerControls(
                positionMs = positionMs,
                durationMs = durationMs,
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
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
