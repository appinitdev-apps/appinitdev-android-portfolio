
class MediaViewerActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MediaViewerActivity"
        const val EXTRA_FILE_PATH = "FILE_PATH_EXTRA"
        const val EXTRA_FILE_URI = "FILE_URI_EXTRA"
        const val EXTRA_OPEN_AS_TYPE = "OPEN_AS_TYPE_EXTRA"
        const val EXTRA_MIME_TYPE = "MIME_TYPE_EXTRA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filePath = intent.getStringExtra(EXTRA_FILE_PATH).orEmpty()
        val fileUri = getParcelableExtraCompat<Uri>(EXTRA_FILE_URI) ?: Uri.EMPTY
        val openAsType = intent.getIntExtra(EXTRA_OPEN_AS_TYPE, OPEN_AS_DEFAULT)
        val mimeTypeExtra = intent.getStringExtra(EXTRA_MIME_TYPE).orEmpty()

        val fileName = try {
            File(filePath).name
        } catch (e: Exception) {
            "Archivo Multimedia"
        }

        val finalMimeType = if (mimeTypeExtra.isEmpty() || mimeTypeExtra.contains("*")) {
            getMimeTypeFromUri(fileUri)
        } else {
            mimeTypeExtra
        }

        val onPlaybackError = {
            Log.e(TAG, "Formato no soportado internamente ($finalMimeType). Redirigiendo a app externa.")
            openPath(filePath, forceChooser = true, openAsType = openAsType)
            finish()
        }

        setContent {
            MediaViewerScreen(
                fileUri = fileUri,
                fileName = fileName,
                mimeType = finalMimeType,
                onBack = { finish() },
                onOpenExternal = { openPath(filePath, forceChooser = true, openAsType = openAsType) },
                onError = onPlaybackError
            )
        }
    }

    @Suppress("DEPRECATION")
    private inline fun <reified T : Parcelable> getParcelableExtraCompat(key: String): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(key, T::class.java)
        } else {
            intent.getParcelableExtra(key) as? T
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaViewerScreen(
    fileUri: Uri,
    fileName: String,
    mimeType: String,
    onBack: () -> Unit,
    onOpenExternal: () -> Unit,
    onError: () -> Unit,
) {
    val mediaType = remember(mimeType) { mimeType.toMultimediaType() }
    val currentOnError = rememberUpdatedState(onError)

    var controlsVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(controlsVisible, isPlaying) {
        if (controlsVisible && isPlaying) {
            delay(3500)
            controlsVisible = false
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { controlsVisible = !controlsVisible },
            contentAlignment = Alignment.Center
        ) {
            when (mediaType) {
                MultimediaType.IMAGE -> {
                    ImageViewer(uri = fileUri, onError = currentOnError.value)
                }
                MultimediaType.VIDEO, MultimediaType.AUDIO -> {
                    MediaPlayer(
                        uri = fileUri,
                        isAudioOnly = mediaType == MultimediaType.AUDIO,
                        controlsVisible = controlsVisible,
                        onIsPlayingChanged = { isPlaying = it },
                        onError = currentOnError.value
                    )
                }
                MultimediaType.UNKNOWN -> {
                    LaunchedEffect(Unit) { currentOnError.value() }
                }
            }

            // Capa superior de controles flotantes
            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = fileName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Regresar",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onOpenExternal) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Abrir externo",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black.copy(alpha = 0.5f))
                )
            }
        }
    }
}

@Composable
fun ImageViewer(
    uri: Uri,
    onError: () -> Unit,
) {
    var rotation by remember { mutableStateOf(0f) }
    var mirrored by remember { mutableStateOf(false) }
    val zoomState = rememberZoomState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        AsyncImage(
            model = uri,
            contentDescription = "Visualizador",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = rotation
                    scaleX = if (mirrored) -1f else 1f
                }
                .zoomable(zoomState)
//                .pointerInput(Unit) {
//                    detectTransformGestures { _, _, _, rotationChange ->
//                        rotation += rotationChange
//                    }
//                },
            ,onError = {
                onError()
            }
        )

        FilledIconButton(
            onClick = {
                rotation = (rotation + 90f) % 360f
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 96.dp, end = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.RotateRight,
                contentDescription = "Rotar"
            )
        }
        FilledIconButton(
            onClick = {
                mirrored = !mirrored
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 96.dp, start = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Flip,
                contentDescription = "Modo espejo"
            )
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(UnstableApi::class)
@Composable
fun MediaPlayer(
    uri: Uri,
    isAudioOnly: Boolean,
    controlsVisible: Boolean,
    onIsPlayingChanged: (Boolean) -> Unit,
    onError: () -> Unit,
) {
    val context = LocalContext.current
    val currentOnError = rememberUpdatedState(onError)

    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var playbackState by remember { mutableStateOf(Player.STATE_IDLE) }

    // Estados de control para mejorar radicalmente la UX del Slider
    var isUserDraggingSlider by remember { mutableStateOf(false) }
    var sliderTransientValue by remember { mutableStateOf(0f) }

    // Estados para el Zoom (Pinch-to-Zoom)
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // Instancia única y segura de ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
        }
    }

    // Escuchador de estado del reproductor
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                onIsPlayingChanged(playing)
            }
            override fun onPlaybackStateChanged(state: Int) {
                playbackState = state
                if (state == Player.STATE_READY) {
                    duration = exoPlayer.duration.coerceAtLeast(0L)
                }
            }
            override fun onPlayerError(error: PlaybackException) {
                currentOnError.value()
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Hilo de actualización periódica del tiempo transcurrido
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            // No pisar el estado si el usuario está arrastrando activamente el control de progreso
            if (!isUserDraggingSlider) {
                currentPosition = exoPlayer.currentPosition
            }
            delay(200)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    if (scale > 1f) {
                        offsetX += pan.x
                        offsetY += pan.y
                    } else {
                        offsetX = 0f
                        offsetY = 0f
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // --- CAPA DEL CONTENIDO MULTIMEDIA (CON ZOOM APLICADO) ---
        if (isAudioOnly) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Audiotrack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(72.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Reproduciendo Audio",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        setBackgroundColor(android.graphics.Color.BLACK)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offsetX
                        translationY = offsetY
                    }
            )
        }

        // --- UX STATE: INDICADOR DE CARGA / BUFFERING ---
        if (playbackState == Player.STATE_BUFFERING) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp,
                modifier = Modifier.size(56.dp)
            )
        }

        // --- CONTROLES CLÁSICOS CON DISEÑO REFINADO ---
        AnimatedVisibility(
            visible = controlsVisible && playbackState != Player.STATE_BUFFERING,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            // Fondo oscuro degradado estilo cinematográfico de alta legibilidad
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            ) {
                // Fila Central de Botones Estilizados Premium
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón de Retroceder 10s Estilo Vidrio Esmerilado
                    CustomControlButton(
                        icon = Icons.Default.Replay10,
                        onClick = { exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0L)) }
                    )

                    Spacer(modifier = Modifier.width(36.dp))

                    // Botón Central Elevado e Inteligente (Play / Pause / Replay)
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .border(2.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                            .padding(4.dp) // Genera un anillo externo premium muy estético
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = false, radius = 48.dp),
                                onClick = {
                                    if (playbackState == Player.STATE_ENDED) {
                                        exoPlayer.seekTo(0L)
                                        exoPlayer.play()
                                    } else if (isPlaying) {
                                        exoPlayer.pause()
                                    } else {
                                        exoPlayer.play()
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val playStateIcon = when (playbackState) {
                            Player.STATE_ENDED -> Icons.Default.Replay
                            else -> if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow
                        }
                        Icon(
                            imageVector = playStateIcon,
                            contentDescription = null,
                            tint = Color.Black, // Contraste inverso muy elegante sobre fondo blanco
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(36.dp))

                    // Botón de Adelantar 10s Estilo Vidrio Esmerilado
                    CustomControlButton(
                        icon = Icons.Default.Forward10,
                        onClick = { exoPlayer.seekTo((exoPlayer.currentPosition + 10000).coerceAtMost(duration)) }
                    )
                }

                // Panel inferior de la línea de tiempo (Slider Optimizado)

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                ) {
                    // Determinar qué valor pintar en la barra para que responda instantáneamente al dedo
                    val activeSliderValue = if (isUserDraggingSlider) sliderTransientValue else currentPosition.toFloat()

                    Slider(
                        value = activeSliderValue,
                        onValueChange = { newValue ->
                            isUserDraggingSlider = true
                            sliderTransientValue = newValue
                        },
                        onValueChangeFinished = {
                            isUserDraggingSlider = false
                            currentPosition = sliderTransientValue.toLong()
                            exoPlayer.seekTo(currentPosition)
                        },
                        valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                        modifier = Modifier.fillMaxWidth(),
                        // 1. Rediseñamos el círculo deslizante (Thumb) para que sea pequeño y elegante
                        thumb = {
                            SliderDefaults.Thumb(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                colors = SliderDefaults.colors(thumbColor = Color.White),
                                // Al reducir el tamaño a 12.dp (el de defecto es 20.dp) se ve súper minimalista
                                modifier = Modifier.size(12.dp)
                            )
                        },
                        // 2. Rediseñamos la barra de progreso (Track) para que sea ultra delgada
                        track = { sliderState ->
                            SliderDefaults.Track(
                                sliderState = sliderState,
                                colors = SliderDefaults.colors(
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.24f)
                                ),
                                // Reducimos la altura a la mitad (4.dp) para darle ese aspecto refinado de cine
                                modifier = Modifier.height(4.dp)
                            )
                        }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                         // .padding(horizontal = 4.dp, top = 4.0.dp), // Espaciado sutil superior
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(activeSliderValue.toLong()),
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            )
                        )
                        Text(
                            text = formatTime(duration),
                            color = Color.White.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                        )
                    }
                }
                /*Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                ) {
                    // Determinar qué valor pintar en la barra para que responda instantáneamente al dedo
                    val activeSliderValue = if (isUserDraggingSlider) sliderTransientValue else currentPosition.toFloat()

                    Slider(
                        value = activeSliderValue,
                        onValueChange = { newValue ->
                            isUserDraggingSlider = true
                            sliderTransientValue = newValue
                        },
                        onValueChangeFinished = {
                            isUserDraggingSlider = false
                            currentPosition = sliderTransientValue.toLong()
                            exoPlayer.seekTo(currentPosition) // El salto en memoria ocurre solo al soltar el dedo
                        },
                        valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(activeSliderValue.toLong()),
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = formatTime(duration),
                            color = Color.White.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                        )
                    }
                }*/
            }
        }
    }
}

@Composable
fun CustomControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape) // Borde sutil estilo cristal
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.08f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 32.dp),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(26.dp)
        )
    }
}
// Componente flotante (HUD) para mostrar los cambios de volumen o seek en tiempo real
@Composable
fun VideoGestureHUD(icon: ImageVector, label: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
        shape = CircleShape,
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}