@SuppressLint("UnsafeOptInUsageError")
@Composable
internal fun VideoPlayerFullScreenDialog(
    player: ExoPlayer,
    currentPlayerView: PlayerView,
    videoState: VideoState,
) {
    val context = LocalContext.current
    val fullScreenPlayerView = remember {
        PlayerView(context).apply {
            setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
        }
    }
    val view = LocalView.current
    val window = (view.context as Activity).window
    val insetsController = WindowCompat.getInsetsController(window, view)
    val onDismissRequest = {
        PlayerView.switchTargetView(player, fullScreenPlayerView, currentPlayerView)
        currentPlayerView.findViewById<ImageButton>(R.id.exo_fullscreen)
            .performClick()
        val currentActivity = context.findActivity()
        currentActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        currentActivity.setFullScreen(false)
        if (context.isGestureInteractionMode()) {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        } else {
            insetsController.show(WindowInsetsCompat.Type.statusBars())
        }
        videoState.isFullscreenMode = false
    }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            securePolicy = SecureFlagPolicy.Inherit,
            decorFitsSystemWindows = false,
        ),
    ) {
        val view = LocalView.current
        LaunchedEffect(Unit) {
            PlayerView.switchTargetView(player, currentPlayerView, fullScreenPlayerView)
            val currentActivity = context.findActivity()
            currentActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            (view.parent as DialogWindowProvider).window.setFullScreen(true)
            fullScreenPlayerView.setFullscreenButtonClickListener {
                if (!it) {
                    onDismissRequest()
                }
            }
            fullScreenPlayerView.findViewById<ImageButton>(androidx.media3.ui.R.id.exo_fullscreen)
                .performClick()
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            VideoPlayerSurface(
                playerView = fullScreenPlayerView,
                player = player,
                videoState = videoState,
                usePlayerController = true,
                autoDispose = false,
                onPipEntered = { onDismissRequest() },
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(),
            )
        }
    }
}
