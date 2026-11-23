@SuppressLint("SourceLockedOrientationActivity", "UnsafeOptInUsageError")
@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    player: ExoPlayer,
    playerView: PlayerView,
    videoState: VideoState,
) {
    VideoPlayerSurface(
        modifier = modifier,
        playerView = playerView,
        player = player,
        videoState = videoState,
        usePlayerController = false,
    )
    if (videoState.isFullscreenMode && videoState.player == player) {
        VideoPlayerFullScreenDialog(
            player = player,
            currentPlayerView = playerView,
            videoState = videoState,
        )
    }
}
@OptIn(UnstableApi::class)
@Composable
fun rememberVideoPlayer(context: Context, playerInstance: ExoPlayer.() -> Unit = {}): ExoPlayer {
    return remember {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(10000L)
            .setSeekForwardIncrementMs(10000L)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AUDIO_CONTENT_TYPE_MOVIE)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                false, 
            )
            .apply {
                val cache = VideoPlayerCacheManager.getCache()
                if (cache != null) {
                    val cacheDataSourceFactory = CacheDataSource.Factory()
                        .setCache(cache)
                        .setUpstreamDataSourceFactory(DefaultDataSource.Factory(context, httpDataSourceFactory))
                    setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
                }
            }
            .build()
            .apply {
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            }
            .also(playerInstance)
    }
}
