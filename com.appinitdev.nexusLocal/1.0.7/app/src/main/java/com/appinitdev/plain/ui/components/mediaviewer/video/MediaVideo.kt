@kotlin.OptIn(ExperimentalFoundationApi::class)
@OptIn(UnstableApi::class)
@Composable
fun MediaVideo(
    modifier: Modifier = Modifier, pagerState: PagerState, videoState: VideoState,
    page: Int, model: PreviewItem, scale: Float = DEFAULT_SCALE,
    offsetX: Float = DEFAULT_OFFSET_X, offsetY: Float = DEFAULT_OFFSET_Y,
    rotation: Float = DEFAULT_ROTATION, gesture: RawGesture = RawGesture(),
    onMounted: () -> Unit = {}, onSizeChange: suspend (SizeChangeContent) -> Unit = {},
    boundClip: Boolean = true,
) {
    val scope = rememberCoroutineScope()
    val viewerAlpha = remember { Animatable(0F) }
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }
    var bSize by remember { mutableStateOf(IntSize(0, 0)) }
    var vSize by remember { mutableStateOf(IntSize(0, 0)) }
    val sizing = MediaVideoSizing(bSize, vSize)
    var videoSpecified by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = vSize, key2 = bSize) {
        if (vSize != IntSize.Zero && bSize != IntSize.Zero) {
            onSizeChange(SizeChangeContent(defaultSize = sizing.displaySize, containerSize = bSize, maxScale = sizing.maxScale))
        }
    }
    LaunchedEffect(model.path) {
        if (model.intrinsicSize == IntSize.Zero) {
            when (val data = model.data) {
                is com.appinitdev.nexusLocal.data.DVideo -> model.initAsync(data)
                else -> {
                    val size = com.appinitdev.nexusLocal.helpers.VideoHelper.getIntrinsicSize(model.path)
                    if (size != IntSize.Zero) model.intrinsicSize = size
                }
            }
        }
        if (model.intrinsicSize != IntSize.Zero) {
            vSize = model.intrinsicSize; videoSpecified = true
        }
    }
    val audioManager = remember(context) { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val focusManager = remember(audioManager) { VideoAudioFocusManager(audioManager) }
    val progressDao = remember { AppDatabase.instance.videoPlayProgressDao() }
    val defaultPlayerView = remember { PlayerView(context) }
    var mediaSession = remember<MediaSession?> { null }
    var firstFrameRendered by remember { mutableStateOf(false) }
    val player = rememberVideoPlayer(context, playerInstance = {
        addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                if (!videoState.isPreviewerOpen || pagerState.settledPage != page) return
                scope.launch {
                    videoState.totalTime = player.duration.coerceAtLeast(0L)
                    videoState.isPlaying = player.isPlaying
                    if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)) {
                        videoState.isSeeking = false
                    }
                    if (events.contains(Player.EVENT_RENDERED_FIRST_FRAME)) {
                        firstFrameRendered = true
                    }
                    videoState.updateTime()
                    defaultPlayerView.keepScreenOn = player.isPlaying
                }
            }
        })
    })
    LaunchedEffect(firstFrameRendered) {
        if (firstFrameRendered) {
            viewerAlpha.snapTo(1F)
            onMounted()
        }
    }
    LaunchedEffect(player, pagerState.settledPage, videoState.isPreviewerOpen) {
        if (!videoState.isPreviewerOpen || pagerState.settledPage != page) {
            if (model.mediaId.isNotEmpty() && player.currentPosition > 0) {
                val mediaId = model.mediaId
                val pos = player.currentPosition
                TempData.videoPlayProgressMap[mediaId] = pos
                coIO { progressDao.upsert(DVideoPlayProgress(mediaId, pos, TimeHelper.now())) }
            }
            focusManager.abandonFocus()
            player.stop()
            return@LaunchedEffect
        }
        videoState.initData(player)
        mediaSession?.release()
        mediaSession = MediaSession.Builder(appContext, ForwardingPlayer(player))
            .setId("VideoPlayerMediaSession_" + UUID.randomUUID().toString().lowercase().split("-").first())
            .build()
        val savedPos = if (model.mediaId.isNotEmpty()) TempData.videoPlayProgressMap[model.mediaId] else null
        val expectedTotalMs = ((model.data as? com.appinitdev.nexusLocal.data.DVideo)?.duration ?: 0L) * 1000
        if (expectedTotalMs > 0L) {
            videoState.totalTime = expectedTotalMs
        }
        if (savedPos != null && savedPos > 0L) {
            videoState.currentTime = savedPos
        }
        val exoPlayerMediaItems = listOf(VideoPlayerMediaItem.StorageMediaItem(storageUri = model.path.pathToUri())).map {
            val uri = it.toUri(context)
            MediaItem.Builder().apply { setUri(uri); setMediaMetadata(it.mediaMetadata); setMimeType(it.mimeType); setDrmConfiguration(null) }.build()
        }
        player.setMediaItems(exoPlayerMediaItems)
        if (savedPos != null && savedPos > 0) {
            player.seekTo(savedPos)
        }
        player.prepare()
        focusManager.requestFocus(player)
        player.play()
    }
    DisposableEffect(Unit) {
        onDispose {
            val mediaId = model.mediaId
            val pos = player.currentPosition
            if (mediaId.isNotEmpty() && pos > 0) {
                TempData.videoPlayProgressMap[mediaId] = pos
                coIO { progressDao.upsert(DVideoPlayProgress(mediaId, pos, TimeHelper.now())) }
            }
            focusManager.abandonFocus() 
            player.stop()
            player.release()
            mediaSession?.release()
            mediaSession = null
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { clip = boundClip; alpha = viewerAlpha.value }
            .onSizeChanged { bSize = it }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val released = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) { waitForUpOrCancellation() }
                    if (released == null) {
                        gesture.onLongPress(down.position); videoState.startSpeedBoost()
                        waitForUpOrCancellation(); videoState.stopSpeedBoost()
                    }
                }
            }
            .pointerInput(key1 = videoSpecified) {
                if (videoSpecified) detectTransformGestures(
                    onTap = gesture.onTap, onDoubleTap = gesture.onDoubleTap,
                    gestureStart = gesture.gestureStart, gestureEnd = gesture.gestureEnd, onGesture = gesture.onGesture,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        val videoModifier = Modifier.graphicsLayer {
            if (videoSpecified) {
                scaleX = scale; scaleY = scale; translationX = offsetX; translationY = offsetY; rotationZ = rotation
            }
        }
        VideoPlayer(
            modifier = videoModifier
                .align(Alignment.Center)
                .size(
                    LocalDensity.current.run { sizing.displaySize.width.toDp() },
                    LocalDensity.current.run { sizing.displaySize.height.toDp() }
                ),
            player = player, playerView = defaultPlayerView, videoState = videoState,
        )
    }
}
