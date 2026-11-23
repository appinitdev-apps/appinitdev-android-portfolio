
@OptIn(UnstableApi::class)
@Composable
fun VideoDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val videoUrl = "https://appinitdev-android.web.app/video_tester/tester.mp4"

    var isLoading by remember { mutableStateOf(true) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = true

            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) isLoading = false
                }
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) isLoading = false
                }
            })
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    // Usamos DialogProperties para permitir que el modal use todo el ancho
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false // Permite expandir el modal más allá de los márgenes estándar
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.9f) // Ocupa el 90% del ancho de la pantalla
                .fillMaxHeight(0.85f) // Ocupa el 85% de la altura de la pantalla
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = stringResource(R.string.tutorial_de_ejemplo),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f) // Hace que el video ocupe todo el espacio disponible
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = true
                                // Ajusta el video para que mantenga su proporción dentro del contenedor
                                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.cerrar_tutorial))
                }
            }
        }
    }
}