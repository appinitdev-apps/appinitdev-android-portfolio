@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(userData: ChatUserData, hideDialog:()->Unit, message: Message) {
    val context = LocalContext.current
    val formatter = remember {
        SimpleDateFormat(("hh:mm a"), Locale.getDefault())
    }
    var lifecycle by remember {
        mutableStateOf(Lifecycle.Event.ON_CREATE)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val interactionSource = remember { MutableInteractionSource() }
    var show by remember {
        mutableStateOf(true)
    }
    val cacheSink = Factory()
        .setCache(simpleCache)
    val upstreamFactory = DefaultDataSource.Factory(context, DefaultHttpDataSource.Factory())
    val downStreamFactory = FileDataSource.Factory()
    val cacheDataSourceFactory =
        CacheDataSource.Factory()
            .setCache(simpleCache)
            .setCacheWriteDataSinkFactory(cacheSink)
            .setCacheReadDataSourceFactory(downStreamFactory)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaSource(ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(
                MediaItem.fromUri(message.vidUrl)))
            prepare()
            playWhenReady = false
            addListener(object : Player.Listener {
                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    show = !playWhenReady
                }
            })
        }
    }
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            lifecycle = event
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            exoPlayer.release()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    Dialog(onDismissRequest = hideDialog,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    )       {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))) {
        }
        Box {
            Column(
                modifier = Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        show = !show
                    }
                    .fillMaxSize()
            ){
            }
            AndroidView(
                modifier = Modifier
                    .fillMaxSize(),
                factory = {
                    PlayerView(it).also { playerView ->
                        playerView.player = exoPlayer
                    }
                },
                update = {
                    when(lifecycle) {
                        Lifecycle.Event.ON_RESUME -> {
                            it.onPause()
                            it.player?.pause()
                        }
                        Lifecycle.Event.ON_PAUSE -> {
                            it.onResume()
                        }
                        else -> Unit
                    }
                }
            )
            AnimatedVisibility(visible = show, enter = fadeIn(), exit = fadeOut()) {
                Row(modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { hideDialog() }) {
                        Icon(imageVector = Icons.Filled.ArrowBackIosNew, contentDescription = null)
                    }
                    AsyncImage(
                        model = userData.ppurl,
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(40.dp)
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)){
                        Text(
                            text = userData.username.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if(message.time != null) {
                                formatter.format(message.time.toDate())
                            } else "",
                            color = Color.LightGray,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Light)
                        )
                    }
                }
            }
        }
    }
}