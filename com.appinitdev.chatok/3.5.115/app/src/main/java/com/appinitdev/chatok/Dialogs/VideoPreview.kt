@Composable
fun VideoPreview(
    uri: Uri?,
    hideDialog: () -> Unit,
    send: (String) -> Unit
) {
    var lifecycle by remember {
        mutableStateOf(Lifecycle.Event.ON_CREATE)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val exoPlayer = ExoPlayer.Builder(context).build().apply {
        setMediaItem(MediaItem.fromUri(uri!!))
        prepare()
        playWhenReady = false
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
    Dialog(
        onDismissRequest = hideDialog,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))
        ) {
            var reply by rememberSaveable {
                mutableStateOf("")
            }
            val brush = Brush.linearGradient(
                listOf(
                    Color(0xFF238CDD),
                    Color(0xFF1952C4)
                )
            )
            Box {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.85f)
                            .aspectRatio(16f / 9f),
                        factory = {
                            PlayerView(context).also { playerView ->
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                            .background(MaterialTheme.colorScheme.onPrimary, CircleShape)
                    ) {
                        TextField(
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(text = stringResource(R.string.mensaje)) },
                            value = reply,
                            onValueChange = { reply = it },
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        IconButton(
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .background(brush, CircleShape),
                            onClick = { send(reply) }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Send,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}
