enum class AppScreen {
    WELCOME, HUB, MANUAL_CAPTURE, INTERVAL_CAPTURE, GALLERY
}
enum class ExportFormat(val extension: String, val displayName: String) {
    PNG("png", "PNG"),
    JPG("jpg", "JPEG")
}
enum class ExportQuality(val qualityInt: Int, val displayName: String) {
    HIGH(100, "💎 Máxima (100%)"),
    BALANCED(85, "⚖️ Equilibrada (85%)"),
    COMPRESSED(65, "⚡ Comprimida (65%)")
}
enum class StripColor(val colorInt: Int, val displayName: String) {
    BLACK(android.graphics.Color.BLACK, "⬛ Negro"),
    WHITE(android.graphics.Color.WHITE, "⬜ Blanco"),
    NEON(android.graphics.Color.parseColor("#00E5FF"), "🩵 Neón"),
    PINK(android.graphics.Color.parseColor("#FF4081"), "🩷 Rosa")
}
enum class FilterType(val displayName: String) {
    ORIGINAL("Original"),
    CINE("Cine"),
    BW("B&N"),
    VIVID("Vívido"),
    WARM("Cálido"),
    RETRO("Retro"),
    HDR("HDR"),
    DRAMATIC_BW("Drama")
}
data class VideoMetaData(
    val fileName: String = "video.mp4",
    val durationMs: Long = 0L,
    val width: Int = 1080,
    val height: Int = 1920,
    val resolutionText: String = "1080x1920"
)
data class FaceAnalysisResult(
    val hasFace: Boolean = false,
    val isSmiling: Boolean = false,
    val eyesOpen: Boolean = false,
    val faceCount: Int = 0
)
data class ScoredFrame(
    var bitmap: Bitmap,
    val timestampMs: Long = 0L,
    val sharpnessScore: Float = 0f,
    var isBestQuality: Boolean = false,
    var hasFace: Boolean = false,
    var isSmiling: Boolean = false,
    var eyesOpen: Boolean = false,
    var isSelected: MutableState<Boolean> = mutableStateOf(false)
)
data class AiFrameMeta(
    val sharpness: Float = 0f,
    val hasFace: Boolean = false,
    val isSmiling: Boolean = false,
    val eyesOpen: Boolean = false,
    val isBest: Boolean = false,
    val petOrObjectTag: String? = null,
    val actionTag: String? = null,
    val colorStyleTag: String? = null
)
class MainActivity : ComponentActivity() {
    var canEnterPip = false
    var videoWidth = 16
    var videoHeight = 9
    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        try {
            MobileAds.initialize(this) {}
        } catch (e: Exception) {
            e.printStackTrace()
        }
        setContent {
            MaterialTheme {
                var isPipMode by remember { mutableStateOf(false) }
                DisposableEffect(Unit) {
                    val listener = Consumer<PictureInPictureModeChangedInfo> { info ->
                        isPipMode = info.isInPictureInPictureMode
                    }
                    addOnPictureInPictureModeChangedListener(listener)
                    onDispose { removeOnPictureInPictureModeChangedListener(listener) }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0A0F1D)
                ) {
                    VideoFrameCatcherApp(isPipMode)
                }
            }
        }
    }
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (canEnterPip && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ratioFloat = if (videoHeight > 0) videoWidth.toFloat() / videoHeight.toFloat() else 1.777f
            val aspectRatio = if (ratioFloat in 0.4184f..2.39f) {
                Rational(videoWidth, videoHeight)
            } else {
                Rational(16, 9)
            }
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .build()
            enterPictureInPictureMode(params)
        }
    }
}
private val maxMemoryKb = (Runtime.getRuntime().maxMemory() / 1024).toInt()
private val cacheSizeKb = (maxMemoryKb / 6).coerceAtLeast(1024 * 32) 
private val frameCache = object : LruCache<Long, Bitmap>(cacheSizeKb) {
    override fun sizeOf(key: Long, bitmap: Bitmap): Int {
        return bitmap.byteCount / 1024
    }
}
fun getNearestCachedFrame(targetMs: Long, stepMs: Long, totalFrames: Int): Bitmap? {
    val exact = frameCache.get(targetMs)
    if (exact != null && !isBitmapBlackOrEmpty(exact)) return exact
    var nearestBmp: Bitmap? = null
    var minDiff = Long.MAX_VALUE
    val targetIndex = if (stepMs > 0) (targetMs / stepMs).toInt() else 0
    val searchRange = (targetIndex - 12).coerceAtLeast(0)..(targetIndex + 12).coerceAtMost(totalFrames - 1)
    for (i in searchRange) {
        val checkMs = i * stepMs
        val bmp = frameCache.get(checkMs)
        if (bmp != null && !isBitmapBlackOrEmpty(bmp)) {
            val diff = kotlin.math.abs(checkMs - targetMs)
            if (diff < minDiff) {
                minDiff = diff
                nearestBmp = bmp
            }
        }
    }
    return nearestBmp
}
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    if (isDebug) {
        return
    }
    Surface(
        color = Color(0xFF0A0F1D),
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        AndroidView<AdView>(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = "ca-app-pub-8325653088416363/2986446366"
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
@SuppressLint("LocalContextConfigurationRead")
@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoFrameCatcherApp(isPipMode: Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.WELCOME) }
    var selectedFormat by rememberSaveable { mutableStateOf(ExportFormat.PNG) }
    var videoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var videoMeta by remember { mutableStateOf(VideoMetaData()) }
    var currentVideoPosition by rememberSaveable { mutableLongStateOf(0L) }
    var isExtracting by rememberSaveable { mutableStateOf(false) }
    var newlySavedCapture by rememberSaveable { mutableStateOf<CaptureEntity?>(null) }
    var isMuted by rememberSaveable { mutableStateOf(false) }
    var autoCapturedFrames by remember { mutableStateOf<List<ScoredFrame>>(emptyList()) }
    var showAutoSelectionScreen by rememberSaveable { mutableStateOf(false) }
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply { playWhenReady = false }
    }
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                val activity = context as? MainActivity
                activity?.canEnterPip = (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED)
            }
            override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    val activity = context as? MainActivity
                    activity?.videoWidth = videoSize.width
                    activity?.videoHeight = videoSize.height
                }
            }
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Log.e("ExoPlayer", "Error de reproducción: ${error.message}")
                if (videoUri != null) {
                    try {
                        exoPlayer.setMediaItem(MediaItem.fromUri(videoUri!!))
                        exoPlayer.prepare()
                        if (currentVideoPosition > 0) {
                            exoPlayer.seekTo(currentVideoPosition)
                        }
                    } catch (_: Exception) {}
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }
    val cropImageLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uriCortada ->
                scope.launch {
                    val bitmap = android.graphics.BitmapFactory.decodeStream(
                        context.contentResolver.openInputStream(uriCortada)
                    )
                    if (bitmap != null) {
                        val localPath = saveBitmapToInternalStorage(context, bitmap, selectedFormat)
                        saveToPublicGallery(context, bitmap, selectedFormat)
                        val nuevaCaptura = CaptureEntity(
                            imagePath = localPath,
                            videoName = videoMeta.fileName.ifEmpty { "Video" },
                            captureType = "Captura manual"
                        )
                        val newId = db.captureDao().insertCapture(nuevaCaptura)
                        val entitySaved = nuevaCaptura.copy(id = newId.toInt())
                        Toast.makeText(context, context.getString(R.string.guardado_galeria_publica), Toast.LENGTH_LONG).show()
                        newlySavedCapture = entitySaved
                    }
                }
            }
        }
    }
    val selectAndLoadVideo: (Uri) -> Unit = { uri ->
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {}
        frameCache.evictAll()
        videoUri = uri
        exoPlayer.setMediaItem(MediaItem.fromUri(uri))
        exoPlayer.prepare()
        scope.launch(Dispatchers.IO) {
            val meta = extractVideoMetadata(context, uri)
            if (meta.durationMs > 0) {
                RecentVideoStore.addRecentVideo(
                    context,
                    RecentVideo(uri.toString(), meta.fileName, meta.durationMs)
                )
                withContext(Dispatchers.Main) {
                    videoMeta = meta
                    val initialSeekMs = if (meta.durationMs > 1000L) 1000L else (meta.durationMs / 2).coerceAtLeast(0L)
                    exoPlayer.seekTo(initialSeekMs)
                    currentVideoPosition = initialSeekMs
                    currentScreen = AppScreen.HUB
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "No se pudo acceder al video. Selecciónalo de nuevo.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            selectAndLoadVideo(uri)
        }
    }
    LaunchedEffect(videoUri) {
        frameCache.evictAll()
        if (videoUri != null) {
            while (true) {
                kotlinx.coroutines.delay(100)
                if (exoPlayer.playbackState == Player.STATE_READY) {
                    val pos = exoPlayer.currentPosition
                    if (abs(pos - currentVideoPosition) > 50) {
                        currentVideoPosition = pos
                    }
                }
            }
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0A0F1D),
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            AdBanner()
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                showAutoSelectionScreen -> {
                    BackHandler { showAutoSelectionScreen = false }
                    AutoSelectionInterface(
                        frames = autoCapturedFrames,
                        onSave = { selectedFrames ->
                            scope.launch {
                                isExtracting = true
                                selectedFrames.forEach { frame ->
                                    val localPath = saveBitmapToInternalStorage(context, frame.bitmap, selectedFormat)
                                    saveToPublicGallery(context, frame.bitmap, selectedFormat)
                                    db.captureDao().insertCapture(
                                        CaptureEntity(
                                            imagePath = localPath,
                                            videoName = videoMeta.fileName,
                                            captureType = "Captura por intervalo"
                                        )
                                    )
                                }
                                isExtracting = false
                                Toast.makeText(context, context.getString(R.string.guardado_galeria_publica), Toast.LENGTH_LONG).show()
                                showAutoSelectionScreen = false
                                currentScreen = AppScreen.GALLERY
                            }
                        },
                        onClose = { showAutoSelectionScreen = false },
                        selectedFormat = selectedFormat,
                        isSaving = isExtracting
                    )
                }
                currentScreen == AppScreen.GALLERY -> {
                    BackHandler { currentScreen = if (videoUri != null) AppScreen.HUB else AppScreen.WELCOME }
                    GalleryScreen(
                        db = db,
                        onBack = { currentScreen = if (videoUri != null) AppScreen.HUB else AppScreen.WELCOME },
                        cropLauncher = cropImageLauncher,
                        selectedFormat = selectedFormat
                    )
                }
                currentScreen == AppScreen.MANUAL_CAPTURE && videoUri != null -> {
                    BackHandler { currentScreen = AppScreen.HUB }
                    ManualCaptureScreen(
                        context = context,
                        videoMeta = videoMeta,
                        videoUri = videoUri!!,
                        exoPlayer = exoPlayer,
                        currentPosition = currentVideoPosition,
                        onPositionSeek = { target ->
                            exoPlayer.seekTo(target)
                            currentVideoPosition = target
                        },
                        isMuted = isMuted,
                        onToggleMute = {
                            isMuted = !isMuted
                            exoPlayer.volume = if (isMuted) 0f else 1f
                        },
                        onSaveFrame = { bitmap ->
                            scope.launch {
                                val localPath = saveBitmapToInternalStorage(context, bitmap, selectedFormat)
                                saveToPublicGallery(context, bitmap, selectedFormat)
                                val capture = CaptureEntity(
                                    imagePath = localPath,
                                    videoName = videoMeta.fileName,
                                    captureType = "Captura manual"
                                )
                                val newId = db.captureDao().insertCapture(capture)
                                newlySavedCapture = capture.copy(id = newId.toInt())
                                Toast.makeText(context, context.getString(R.string.guardado_galeria_publica), Toast.LENGTH_SHORT).show()
                            }
                        },
                        onBack = { currentScreen = AppScreen.HUB },
                        cropLauncher = cropImageLauncher,
                        selectedFormat = selectedFormat,
                        onQuickBurst = { burstFrames ->
                            autoCapturedFrames = burstFrames
                            showAutoSelectionScreen = true
                        }
                    )
                }
                currentScreen == AppScreen.INTERVAL_CAPTURE && videoUri != null -> {
                    BackHandler { currentScreen = AppScreen.HUB }
                    IntervalCaptureScreen(
                        context = context,
                        videoMeta = videoMeta,
                        videoUri = videoUri!!,
                        exoPlayer = exoPlayer,
                        isMuted = isMuted,
                        onToggleMute = {
                            isMuted = !isMuted
                            exoPlayer.volume = if (isMuted) 0f else 1f
                        },
                        onStartExtraction = { intervalMs, estimatedCount, startMs, endMs ->
                            scope.launch {
                                isExtracting = true
                                autoCapturedFrames = runAutoExtractionParallel(
                                    context = context,
                                    uri = videoUri!!,
                                    count = estimatedCount,
                                    intervalMs = intervalMs,
                                    startTimeMs = startMs,
                                    endTimeMs = endMs
                                )
                                isExtracting = false
                                showAutoSelectionScreen = true
                            }
                        },
                        onBack = { currentScreen = AppScreen.HUB }
                    )
                }
                currentScreen == AppScreen.HUB && videoUri != null -> {
                    BackHandler { currentScreen = AppScreen.WELCOME }
                    VideoHubScreen(
                        context = context,
                        videoMeta = videoMeta,
                        videoUri = videoUri!!,
                        exoPlayer = exoPlayer,
                        currentPosition = currentVideoPosition,
                        onPositionSeek = { target ->
                            exoPlayer.seekTo(target)
                            currentVideoPosition = target
                        },
                        selectedFormat = selectedFormat,
                        onFormatChange = { selectedFormat = it },
                        onSelectManualCapture = { currentScreen = AppScreen.MANUAL_CAPTURE },
                        onSelectIntervalCapture = { currentScreen = AppScreen.INTERVAL_CAPTURE },
                        onOpenGallery = { currentScreen = AppScreen.GALLERY },
                        onBack = {
                            videoUri = null
                            currentScreen = AppScreen.WELCOME
                        }
                    )
                }
                else -> {
                    WelcomeHeroScreen(
                        context = context,
                        onSelectVideo = { videoPicker.launch("video/*") },
                        onOpenGallery = { currentScreen = AppScreen.GALLERY },
                        onSelectRecentVideo = selectAndLoadVideo
                    )
                }
            }
            AnimatedVisibility(
                visible = newlySavedCapture != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                newlySavedCapture?.let { capture ->
                    FullScreenDetail(
                        capture = capture,
                        onDismiss = { newlySavedCapture = null },
                        onShare = { shareImage(context, capture.imagePath) },
                        onRecrop = {
                            val bitmap = loadBitmapFromPathOrUri(context, capture.imagePath)
                            if (bitmap != null) {
                                newlySavedCapture = null
                                launchCropper(context, bitmap, cropImageLauncher, selectedFormat)
                            }
                        },
                        selectedFormat = selectedFormat,
                        context = context,
                        db = db
                    )
                }
            }
            if (isExtracting && !showAutoSelectionScreen) {
                LoadingOverlay()
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoHubScreen(
    context: Context,
    videoMeta: VideoMetaData,
    videoUri: Uri,
    exoPlayer: ExoPlayer,
    currentPosition: Long,
    onPositionSeek: (Long) -> Unit,
    selectedFormat: ExportFormat,
    onFormatChange: (ExportFormat) -> Unit,
    onSelectManualCapture: () -> Unit,
    onSelectIntervalCapture: () -> Unit,
    onOpenGallery: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
    ) {
        Surface(
            color = Color(0xFF101728),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.volver),
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = videoMeta.fileName,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1E283F), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "📐 ${videoMeta.resolutionText}",
                            color = Color(0xFF00E5FF),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onOpenGallery,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1E283F), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = stringResource(R.string.galer_a),
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            val hubZoomState = rememberZoomableState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Zoomable(
                    state = hubZoomState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = false
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .clickable {
                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                            isPlaying = exoPlayer.isPlaying
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (exoPlayer.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sliderPos = if (videoMeta.durationMs > 0) currentPosition.toFloat() / videoMeta.durationMs.toFloat() else 0f
                Slider(
                    value = sliderPos.coerceIn(0f, 1f),
                    onValueChange = { frac ->
                        val target = (frac * videoMeta.durationMs).toLong()
                        onPositionSeek(target)
                    },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF1E68D7),
                        activeTrackColor = Color(0xFF1E68D7),
                        inactiveTrackColor = Color(0xFF2A3654)
                    )
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTimeMsShort(currentPosition),
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                Text(
                    text = formatTimeMsShort(videoMeta.durationMs),
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }
            Spacer(Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141C2E)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF23304A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Formato de salida",
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0D1322), RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ExportFormat.entries.forEach { format ->
                            val isSel = selectedFormat == format
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFF1E68D7) else Color.Transparent)
                                    .clickable { onFormatChange(format) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = format.displayName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Elige el método de captura",
                color = Color.LightGray,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(10.dp))
            CaptureMethodCard(
                title = "Captura manual",
                subtitle = "Desplázate por el video y guarda exactamente los fotogramas que quieras",
                icon = Icons.Default.TouchApp,
                onClick = onSelectManualCapture
            )
            Spacer(Modifier.height(12.dp))
            CaptureMethodCard(
                title = "Captura por intervalo",
                subtitle = "Extrae automáticamente fotogramas en intervalos de tiempo establecidos",
                icon = Icons.Default.Timer,
                onClick = onSelectIntervalCapture
            )
            Spacer(Modifier.height(12.dp))
            var isExtractingAudio by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            CaptureMethodCard(
                title = "🎵 Extraer Audio (MP3 / AAC)",
                subtitle = "Extrae y guarda la pista de audio de tu video en formato M4A / AAC",
                icon = Icons.Default.AudioFile,
                onClick = {
                    if (!isExtractingAudio) {
                        isExtractingAudio = true
                        scope.launch {
                            Toast.makeText(context, "🎵 Extrayendo pista de audio...", Toast.LENGTH_SHORT).show()
                            val audioUri = extractAudioFromVideo(context, videoUri, videoMeta.fileName)
                            isExtractingAudio = false
                            if (audioUri != null) {
                                Toast.makeText(context, "🎵 Audio guardado en Música/VideoToPhoto", Toast.LENGTH_LONG).show()
                                shareAudioFile(context, audioUri)
                            } else {
                                Toast.makeText(context, "No se pudo extraer la pista de audio del video", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            )
        }
    }
}
@Composable
fun CaptureMethodCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141C2E)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF23304A)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(Color(0xFF1E2A45), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualCaptureScreen(
    context: Context,
    videoMeta: VideoMetaData,
    videoUri: Uri,
    exoPlayer: ExoPlayer,
    currentPosition: Long,
    onPositionSeek: (Long) -> Unit,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    onSaveFrame: (Bitmap) -> Unit,
    onBack: () -> Unit,
    cropLauncher: ActivityResultLauncher<CropImageContractOptions>,
    selectedFormat: ExportFormat,
    onQuickBurst: (List<ScoredFrame>) -> Unit
) {
    var withWatermark by rememberSaveable { mutableStateOf(false) }
    val totalFrames = ((videoMeta.durationMs * 30) / 1000).coerceAtLeast(1)
    val currentFrameIndex = (((currentPosition * 30) / 1000) + 1).coerceIn(1, totalFrames)
    val stepMs = remember(videoMeta.durationMs) {
        when {
            videoMeta.durationMs <= 30_000L -> 100L
            videoMeta.durationMs <= 120_000L -> 200L
            else -> 500L
        }
    }
    val totalStripFrames = remember(videoMeta.durationMs, stepMs) {
        if (videoMeta.durationMs > 0) {
            (videoMeta.durationMs / stepMs).toInt().coerceAtLeast(1)
        } else 1
    }
    val activeStripIndex = (currentPosition / stepMs).toInt().coerceIn(0, totalStripFrames - 1)
    val scope = rememberCoroutineScope()
    val retriever = remember(videoUri) {
        MediaMetadataRetriever().apply {
            try { setDataSource(context, videoUri) } catch (e: Exception) { e.printStackTrace() }
        }
    }
    DisposableEffect(retriever) {
        onDispose {
            try { retriever.release() } catch (_: Exception) {}
        }
    }
    var selectedFrameBitmap by remember {
        mutableStateOf<Bitmap?>(getNearestCachedFrame(currentPosition, stepMs, totalStripFrames))
    }
    LaunchedEffect(currentPosition) {
        val quickBmp = getNearestCachedFrame(currentPosition, stepMs, totalStripFrames)
        if (quickBmp != null) {
            selectedFrameBitmap = quickBmp
        }
        withContext(Dispatchers.IO) {
            try {
                val fullResFrame = retriever.getFrameAtTime(currentPosition * 1000L, MediaMetadataRetriever.OPTION_CLOSEST)
                if (fullResFrame != null) {
                    withContext(Dispatchers.Main) {
                        selectedFrameBitmap = fullResFrame
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    val aiCache = remember { mutableStateMapOf<Long, AiFrameMeta>() }
    LaunchedEffect(videoUri, totalStripFrames) {
        withContext(Dispatchers.IO) {
            val bgRetriever = MediaMetadataRetriever()
            try {
                bgRetriever.setDataSource(context, videoUri)
                var maxScore = -1f
                var bestTs = 0L
                for (i in 0 until totalStripFrames) {
                    val tsMs = i * stepMs
                    var bmp = frameCache.get(tsMs)
                    if (bmp == null) {
                        bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                            bgRetriever.getScaledFrameAtTime(tsMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, 140, 140)
                        } else {
                            bgRetriever.getFrameAtTime(tsMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        }
                        if (bmp != null) {
                            frameCache.put(tsMs, bmp)
                        }
                    }
                    if (bmp != null && !aiCache.containsKey(tsMs)) {
                        val sharpness = calculateSharpnessScore(bmp)
                        val faceRes = analyzeFaceAndSmile(bmp)
                        val petObj = analyzePetAndObjects(bmp)
                        val isAction = analyzeActionIntensity(bmp)
                        val colorStyle = analyzeColorAndLighting(bmp)
                        var score = sharpness
                        if (faceRes.hasFace) score += 40f
                        if (faceRes.eyesOpen) score += 30f
                        if (faceRes.isSmiling) score += 30f
                        if (petObj != null) score += 20f
                        if (isAction) score += 25f
                        if (colorStyle != null) score += 15f
                        if (score > maxScore) {
                            maxScore = score
                            bestTs = tsMs
                        }
                        val meta = AiFrameMeta(
                            sharpness = score,
                            hasFace = faceRes.hasFace,
                            isSmiling = faceRes.isSmiling,
                            eyesOpen = faceRes.eyesOpen,
                            isBest = false,
                            petOrObjectTag = petObj,
                            actionTag = if (isAction) "⚡ Acción" else null,
                            colorStyleTag = colorStyle
                        )
                        withContext(Dispatchers.Main) {
                            aiCache[tsMs] = meta
                        }
                    }
                }
                if (maxScore > 0f) {
                    withContext(Dispatchers.Main) {
                        aiCache[bestTs] = aiCache[bestTs]?.copy(isBest = true) ?: AiFrameMeta(sharpness = maxScore, isBest = true)
                    }
                }
            } catch (_: Exception) {
            } finally {
                try { bgRetriever.release() } catch (_: Exception) {}
            }
        }
    }
    val listState = rememberLazyListState()
    LaunchedEffect(activeStripIndex) {
        withContext(Dispatchers.IO) {
            val start = (activeStripIndex - 5).coerceAtLeast(0)
            val end = (activeStripIndex + 5).coerceAtMost(totalStripFrames - 1)
            for (i in start..end) {
                val tsMs = i * stepMs
                if (frameCache.get(tsMs) == null) {
                    try {
                        val frameBmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                            retriever.getScaledFrameAtTime(tsMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, 140, 140)
                        } else {
                            retriever.getFrameAtTime(tsMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        }
                        if (frameBmp != null) {
                            frameCache.put(tsMs, frameBmp)
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }
    val isDragged by listState.interactionSource.collectIsDraggedAsState()
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(activeStripIndex) {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (_: Exception) {}
        if (!isDragged) {
            listState.animateScrollToItem(activeStripIndex)
        }
    }
    LaunchedEffect(isDragged) {
        if (isDragged) {
            snapshotFlow {
                val layoutInfo = listState.layoutInfo
                val visibleItems = layoutInfo.visibleItemsInfo
                if (visibleItems.isNotEmpty()) {
                    val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                    val centerItem = visibleItems.minByOrNull { item ->
                        val itemCenter = item.offset + (item.size / 2)
                        kotlin.math.abs(itemCenter - viewportCenter)
                    }
                    centerItem?.index
                } else null
            }.collect { centeredIndex ->
                if (centeredIndex != null && centeredIndex != activeStripIndex) {
                    val targetMs = (centeredIndex * stepMs).coerceIn(0L, videoMeta.durationMs)
                    onPositionSeek(targetMs)
                }
            }
        }
    }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val swipeToSeekModifier = Modifier.pointerInput(Unit) {
        detectHorizontalDragGestures(
            onDragStart = { dragAccumulator = 0f },
            onHorizontalDrag = { change, dragAmount ->
                change.consume()
                dragAccumulator += dragAmount
                val threshold = 16f
                if (abs(dragAccumulator) >= threshold) {
                    val frameSteps = -(dragAccumulator / threshold).toInt()
                    if (frameSteps != 0) {
                        val targetMs = (currentPosition + (frameSteps * stepMs)).coerceIn(0L, videoMeta.durationMs)
                        onPositionSeek(targetMs)
                        dragAccumulator += (frameSteps * threshold)
                    }
                }
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
    ) {
        Surface(color = Color(0xFF101728)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.volver),
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Captura manual",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onToggleMute) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Audio",
                        tint = Color.White
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            val manualZoomState = rememberZoomableState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black)
                    .then(swipeToSeekModifier),
                contentAlignment = Alignment.Center
            ) {
                Zoomable(
                    state = manualZoomState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = false
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }
                IconButton(onClick = {
                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                    isPlaying = exoPlayer.isPlaying
                }) {
                    Icon(
                        imageVector = if (exoPlayer.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                val sliderPos = if (videoMeta.durationMs > 0) currentPosition.toFloat() / videoMeta.durationMs.toFloat() else 0f
                Slider(
                    value = sliderPos.coerceIn(0f, 1f),
                    onValueChange = { frac ->
                        val target = (frac * videoMeta.durationMs).toLong()
                        onPositionSeek(target)
                    },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color(0xFF2A3654)
                    )
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatTimeMsShort(currentPosition), color = Color.Gray, fontSize = 11.sp)
                Text(formatTimeMsShort(videoMeta.durationMs), color = Color.Gray, fontSize = 11.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Fotograma $currentFrameIndex de $totalFrames",
                color = Color.LightGray,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        val target = ((activeStripIndex - 1) * stepMs).coerceAtLeast(0L)
                        onPositionSeek(target)
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFF1A2338), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Anterior",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(4.dp))
                LazyRow(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 110.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(76.dp)
                        .then(swipeToSeekModifier),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(totalStripFrames) { index ->
                        val timestampMs = index * stepMs
                        val isSelected = index == activeStripIndex
                        val aiMeta = aiCache[timestampMs]
                        var thumbnailBmp by remember(timestampMs) {
                            mutableStateOf(getNearestCachedFrame(timestampMs, stepMs, totalStripFrames))
                        }
                        LaunchedEffect(timestampMs) {
                            if (thumbnailBmp == null) {
                                withContext(Dispatchers.IO) {
                                    val cached = frameCache.get(timestampMs)
                                    if (cached != null) {
                                        thumbnailBmp = cached
                                    } else {
                                        val frameBmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                                            retriever.getScaledFrameAtTime(timestampMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, 160, 160)
                                        } else {
                                            retriever.getFrameAtTime(timestampMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                                        }
                                        if (frameBmp != null) {
                                            frameCache.put(timestampMs, frameBmp)
                                            withContext(Dispatchers.Main) {
                                                thumbnailBmp = frameBmp
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .width(54.dp)
                                .height(72.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 2.5.dp else 0.dp,
                                    color = if (isSelected) Color(0xFF00E5FF) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    onPositionSeek(timestampMs)
                                }
                        ) {
                            thumbnailBmp?.let { bmp ->
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } ?: Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF141C2E)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFF00E5FF),
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.width(4.dp))
                IconButton(
                    onClick = {
                        val target = ((activeStripIndex + 1) * stepMs).coerceAtMost(videoMeta.durationMs)
                        onPositionSeek(target)
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFF1A2338), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Siguiente",
                        tint = Color.White
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
                    .border(1.dp, Color(0xFF23304A), RoundedCornerShape(12.dp))
                    .then(swipeToSeekModifier),
                contentAlignment = Alignment.Center
            ) {
                selectedFrameBitmap?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Fotograma seleccionado",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    val currentAi = aiCache[currentPosition] ?: run {
                        val nearestTs = (currentPosition / stepMs) * stepMs
                        aiCache[nearestTs]
                    }
                    if (currentAi != null && (currentAi.isBest || currentAi.hasFace || currentAi.petOrObjectTag != null || currentAi.actionTag != null || currentAi.colorStyleTag != null)) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(6.dp)
                                .background(
                                    color = if (currentAi.isBest) Color(0xFF1E68D7) else Color(0xFF141C2E).copy(alpha = 0.85f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .border(1.dp, if (currentAi.isBest) Color(0xFF00E5FF) else Color(0xFF23304A), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                if (currentAi.isBest) {
                                    Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFF00E5FF), modifier = Modifier.size(10.dp))
                                    Text("✦ IA BEST", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                } else if (currentAi.isSmiling) {
                                    Text("😄 Sonrisa", color = Color.Yellow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                } else if (currentAi.hasFace) {
                                    Text("😊 Rostro", color = Color(0xFF00E5FF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                } else if (currentAi.petOrObjectTag != null) {
                                    Text(currentAi.petOrObjectTag!!, color = Color(0xFFFF9100), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                } else if (currentAi.actionTag != null) {
                                    Text(currentAi.actionTag!!, color = Color(0xFFFF4081), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                } else if (currentAi.colorStyleTag != null) {
                                    Text(currentAi.colorStyleTag!!, color = Color(0xFF00E5FF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } ?: CircularProgressIndicator(color = Color(0xFF00E5FF), modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(8.dp))
            FilterChip(
                selected = withWatermark,
                onClick = { withWatermark = !withWatermark },
                label = { Text(if (withWatermark) "🕒 Marca de tiempo: SI" else "🕒 Añadir marca de tiempo", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF1E68D7),
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White,
                    containerColor = Color(0xFF141C2E),
                    labelColor = Color.LightGray,
                    iconColor = Color.LightGray
                )
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var isAnalyzingBestFrame by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = {
                        if (!isAnalyzingBestFrame) {
                            isAnalyzingBestFrame = true
                            scope.launch {
                                val bestMs = findBestFrameNearTimestamp(
                                    context = context,
                                    videoUri = videoUri,
                                    centerMs = currentPosition,
                                    durationMs = videoMeta.durationMs
                                )
                                onPositionSeek(bestMs)
                                isAnalyzingBestFrame = false
                                Toast.makeText(context, "✦ IA: Fotograma perfecto seleccionado", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(21.dp),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF102844))
                ) {
                    if (isAnalyzingBestFrame) {
                        CircularProgressIndicator(color = Color(0xFF00E5FF), modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFF00E5FF), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("✦ IA Best", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                var isBurstLoading by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = {
                        if (!isBurstLoading) {
                            isBurstLoading = true
                            scope.launch {
                                val burst = extractBurstFrames(
                                    context = context,
                                    uri = videoUri,
                                    centerMs = currentPosition,
                                    durationMs = videoMeta.durationMs
                                )
                                isBurstLoading = false
                                if (burst.isNotEmpty()) {
                                    onQuickBurst(burst)
                                } else {
                                    Toast.makeText(context, "No se pudieron extraer fotos en ráfaga", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(21.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF9100)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF2E1C10))
                ) {
                    if (isBurstLoading) {
                        CircularProgressIndicator(color = Color(0xFFFF9100), modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Timer, null, tint = Color(0xFFFF9100), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("⚡ Ráfaga (10)", color = Color(0xFFFF9100), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    selectedFrameBitmap?.let { bmp ->
                        val finalFrame = if (withWatermark) drawTimestampWatermark(bmp, currentPosition) else bmp
                        onSaveFrame(finalFrame)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(50.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(25.dp),
                border = BorderStroke(1.5.dp, Color(0xFF1E68D7)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF122A52))
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.guardar),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntervalCaptureScreen(
    context: Context,
    videoMeta: VideoMetaData,
    videoUri: Uri,
    exoPlayer: ExoPlayer,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    onStartExtraction: (intervalMs: Long, estimatedCount: Int, startTimeMs: Long, endTimeMs: Long) -> Unit,
    onBack: () -> Unit
) {
    var intervalMs by rememberSaveable { mutableLongStateOf(1000L) }
    var startTimeMs by rememberSaveable { mutableLongStateOf(1000L.coerceAtMost(videoMeta.durationMs)) }
    var endTimeMs by rememberSaveable(videoMeta.durationMs) { mutableLongStateOf(videoMeta.durationMs) }
    val selectedDurationMs = (endTimeMs - startTimeMs).coerceAtLeast(100L)
    val estimatedCount = if (intervalMs > 0) ((selectedDurationMs / intervalMs) + 1).toInt().coerceIn(1, 500) else 1
    var currentVideoPosition by rememberSaveable { mutableLongStateOf(0L) }
    val scope = rememberCoroutineScope()
    val stepMs = intervalMs.coerceAtLeast(100L)
    val totalStripFrames = remember(videoMeta.durationMs, stepMs) {
        if (videoMeta.durationMs > 0 && stepMs > 0) {
            ((videoMeta.durationMs / stepMs) + 1).toInt().coerceIn(1, 300)
        } else 1
    }
    val activeStripIndex = (currentVideoPosition / stepMs).toInt().coerceIn(0, totalStripFrames - 1)
    val retriever = remember(videoUri) {
        MediaMetadataRetriever().apply {
            try { setDataSource(context, videoUri) } catch (e: Exception) { e.printStackTrace() }
        }
    }
    DisposableEffect(retriever) {
        onDispose {
            try { retriever.release() } catch (_: Exception) {}
        }
    }
    val listState = rememberLazyListState()
    LaunchedEffect(activeStripIndex) {
        withContext(Dispatchers.IO) {
            val start = (activeStripIndex - 5).coerceAtLeast(0)
            val end = (activeStripIndex + 5).coerceAtMost(totalStripFrames - 1)
            for (i in start..end) {
                val tsMs = i * stepMs
                if (frameCache.get(tsMs) == null) {
                    try {
                        val frameBmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                            retriever.getScaledFrameAtTime(tsMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, 200, 200)
                        } else {
                            retriever.getFrameAtTime(tsMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        }
                        if (frameBmp != null) {
                            frameCache.put(tsMs, frameBmp)
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }
    val isDraggedInterval by listState.interactionSource.collectIsDraggedAsState()
    val hapticInterval = LocalHapticFeedback.current
    LaunchedEffect(activeStripIndex) {
        try {
            hapticInterval.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (_: Exception) {}
        if (!isDraggedInterval) {
            listState.animateScrollToItem(activeStripIndex)
        }
    }
    LaunchedEffect(isDraggedInterval) {
        if (isDraggedInterval) {
            snapshotFlow {
                val layoutInfo = listState.layoutInfo
                val visibleItems = layoutInfo.visibleItemsInfo
                if (visibleItems.isNotEmpty()) {
                    val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                    val centerItem = visibleItems.minByOrNull { item ->
                        val itemCenter = item.offset + (item.size / 2)
                        kotlin.math.abs(itemCenter - viewportCenter)
                    }
                    centerItem?.index
                } else null
            }.collect { centeredIndex ->
                if (centeredIndex != null && centeredIndex != activeStripIndex) {
                    val targetMs = (centeredIndex * stepMs).coerceIn(0L, videoMeta.durationMs)
                    exoPlayer.seekTo(targetMs)
                    currentVideoPosition = targetMs
                }
            }
        }
    }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val swipeToSeekModifier = Modifier.pointerInput(Unit) {
        detectHorizontalDragGestures(
            onDragStart = { dragAccumulator = 0f },
            onHorizontalDrag = { change, dragAmount ->
                change.consume()
                dragAccumulator += dragAmount
                val threshold = 16f
                if (abs(dragAccumulator) >= threshold) {
                    val frameSteps = -(dragAccumulator / threshold).toInt()
                    if (frameSteps != 0) {
                        val targetMs = (exoPlayer.currentPosition + (frameSteps * stepMs)).coerceIn(0L, videoMeta.durationMs)
                        exoPlayer.seekTo(targetMs)
                        currentVideoPosition = targetMs
                        dragAccumulator += (frameSteps * threshold)
                    }
                }
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
    ) {
        Surface(color = Color(0xFF101728)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.volver),
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Captura por intervalo",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onToggleMute) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Audio",
                        tint = Color.White
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
                    .then(swipeToSeekModifier),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .clickable {
                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (exoPlayer.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141C2E), RoundedCornerShape(10.dp))
                    .padding(4.dp)
                    .then(swipeToSeekModifier),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val target = (exoPlayer.currentPosition - stepMs).coerceAtLeast(0L)
                        exoPlayer.seekTo(target)
                        currentVideoPosition = target
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ChevronLeft, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                LazyRow(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 110.dp),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(totalStripFrames) { index ->
                        val timestampMs = index * stepMs
                        val isCurrent = index == activeStripIndex
                        val isInRange = timestampMs in startTimeMs..endTimeMs
                        var thumbnailBmp by remember(timestampMs) {
                            mutableStateOf(frameCache.get(timestampMs))
                        }
                        LaunchedEffect(timestampMs) {
                            if (thumbnailBmp == null || isBitmapBlackOrEmpty(thumbnailBmp!!)) {
                                withContext(Dispatchers.IO) {
                                    val cached = frameCache.get(timestampMs)
                                    if (cached != null && !isBitmapBlackOrEmpty(cached)) {
                                        thumbnailBmp = cached
                                    } else {
                                        val sampleTsUs = (if (timestampMs < 1000L) 1000L else timestampMs) * 1000L
                                        val frameBmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                                            retriever.getScaledFrameAtTime(sampleTsUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, 200, 200)
                                        } else {
                                            retriever.getFrameAtTime(sampleTsUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                                        }
                                        if (frameBmp != null && !isBitmapBlackOrEmpty(frameBmp)) {
                                            frameCache.put(timestampMs, frameBmp)
                                            withContext(Dispatchers.Main) {
                                                thumbnailBmp = frameBmp
                                            }
                                        } else {
                                            val fallbackBmp = retriever.getFrameAtTime(1000_000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                                            if (fallbackBmp != null) {
                                                withContext(Dispatchers.Main) {
                                                    thumbnailBmp = fallbackBmp
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height(64.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    width = if (isCurrent) 2.dp else 0.dp,
                                    color = if (isCurrent) Color(0xFF00E5FF) else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    exoPlayer.seekTo(timestampMs)
                                    currentVideoPosition = timestampMs
                                }
                        ) {
                            thumbnailBmp?.let { bmp ->
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer(alpha = if (isInRange) 1f else 0.35f)
                                )
                            } ?: Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF1E283F)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF00E5FF), modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                }
                IconButton(
                    onClick = {
                        val target = (exoPlayer.currentPosition + stepMs).coerceAtMost(videoMeta.durationMs)
                        exoPlayer.seekTo(target)
                        currentVideoPosition = target
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ChevronRight, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatTimeMsShort(startTimeMs), color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("Posición: ${formatTimeMsShort(exoPlayer.currentPosition)}", color = Color.White, fontSize = 11.sp)
                Text(formatTimeMsShort(endTimeMs), color = Color(0xFFFF9100), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141C2E)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF23304A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Rango de extracción",
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val current = exoPlayer.currentPosition
                                if (current < endTimeMs - 500L) {
                                    startTimeMs = current
                                } else {
                                    startTimeMs = (endTimeMs - 1000L).coerceAtLeast(0L)
                                }
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Text("Fijar Inicio: ${formatTimeMsShort(startTimeMs)}", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                val current = exoPlayer.currentPosition
                                if (current > startTimeMs + 500L) {
                                    endTimeMs = current
                                } else {
                                    endTimeMs = (startTimeMs + 1000L).coerceAtMost(videoMeta.durationMs)
                                }
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFFF9100)),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Text("Fijar Fin: ${formatTimeMsShort(endTimeMs)}", color = Color(0xFFFF9100), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    if (videoMeta.durationMs > 0) {
                        val currentRange = (startTimeMs.toFloat() / videoMeta.durationMs.toFloat())..(endTimeMs.toFloat() / videoMeta.durationMs.toFloat())
                        RangeSlider(
                            value = currentRange,
                            onValueChange = { range ->
                                val newStart = (range.start * videoMeta.durationMs).toLong()
                                val newEnd = (range.endInclusive * videoMeta.durationMs).toLong()
                                if (newEnd - newStart >= 1000L) {
                                    startTimeMs = newStart
                                    endTimeMs = newEnd
                                }
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF00E5FF),
                                activeTrackColor = Color(0xFF00E5FF),
                                inactiveTrackColor = Color(0xFF23304A)
                            )
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141C2E)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF23304A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Frecuencia de intervalo",
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        val textInterval = if (intervalMs < 1000) "Cada ${intervalMs}ms ($estimatedCount fotos)"
                        else "Cada ${String.format(Locale.US, "%.1f", intervalMs / 1000f).removeSuffix(".0")}s ($estimatedCount fotos)"
                        Text(
                            text = textInterval,
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Slider(
                        value = intervalMs.toFloat(),
                        onValueChange = { intervalMs = it.toLong() },
                        valueRange = 100f..10000f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF1E68D7),
                            activeTrackColor = Color(0xFF1E68D7),
                            inactiveTrackColor = Color(0xFF23304A)
                        )
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onStartExtraction(intervalMs, estimatedCount, startTimeMs, endTimeMs) },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E68D7))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "EXTRAER ($estimatedCount)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "${formatTimeMsShort(startTimeMs)} - ${formatTimeMsShort(endTimeMs)}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }
                var isExportingClip by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = {
                        if (!isExportingClip) {
                            isExportingClip = true
                            scope.launch {
                                Toast.makeText(context, "✂️ Exportando clip MP4...", Toast.LENGTH_SHORT).show()
                                val clipUri = trimAndExportVideoClip(context, videoUri, startTimeMs, endTimeMs)
                                isExportingClip = false
                                if (clipUri != null) {
                                    Toast.makeText(context, "✂️ Clip guardado en Videos/VideoToPhoto", Toast.LENGTH_LONG).show()
                                    try {
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "video/mp4"
                                            putExtra(Intent.EXTRA_STREAM, clipUri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Compartir Clip MP4"))
                                    } catch (_: Exception) {}
                                } else {
                                    Toast.makeText(context, "Error al recortar clip MP4", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(0.75f)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF102844))
                ) {
                    if (isExportingClip) {
                        CircularProgressIndicator(color = Color(0xFF00E5FF), modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("✂️ Clip MP4", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Guardar video", color = Color.LightGray, fontSize = 10.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GalleryScreen(
    db: AppDatabase,
    onBack: () -> Unit,
    cropLauncher: ActivityResultLauncher<CropImageContractOptions>,
    selectedFormat: ExportFormat,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showOnlyFavorites by remember { mutableStateOf(false) }
    val capturesFlow = if (showOnlyFavorites) db.captureDao().getFavoriteCaptures() else db.captureDao().getAllCaptures()
    val captures by capturesFlow.collectAsState(initial = emptyList())
    var selectedVideoGroup by rememberSaveable { mutableStateOf<String?>(null) }
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedCaptures = remember { mutableStateListOf<CaptureEntity>() }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var selectedCaptureDetail by remember { mutableStateOf<CaptureEntity?>(null) }
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy h:mm a", Locale.getDefault()) }
    val groupedCaptures = remember(captures) {
        captures.groupBy { it.videoName }
    }
    BackHandler(enabled = selectedVideoGroup != null || selectedCaptureDetail != null) {
        if (selectedCaptureDetail != null) {
            selectedCaptureDetail = null
        } else if (selectedVideoGroup != null) {
            selectedVideoGroup = null
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = when {
                                isSelectionMode -> "${selectedCaptures.size} seleccionados"
                                selectedVideoGroup != null -> selectedVideoGroup!!
                                else -> "FOTOS GUARDADAS"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!isSelectionMode) {
                            Text(
                                text = if (selectedVideoGroup != null) "Toca una foto para ampliarla" else "Mantén presionado para seleccionar fotos",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isSelectionMode) {
                            isSelectionMode = false
                            selectedCaptures.clear()
                        } else if (selectedVideoGroup != null) {
                            selectedVideoGroup = null
                        } else onBack()
                    }) {
                        Icon(
                            imageVector = if (isSelectionMode) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.volver),
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = {
                            val activeCaptures = if (selectedVideoGroup != null) {
                                groupedCaptures[selectedVideoGroup] ?: emptyList()
                            } else captures
                            if (selectedCaptures.size == activeCaptures.size) {
                                selectedCaptures.clear()
                            } else {
                                selectedCaptures.clear()
                                selectedCaptures.addAll(activeCaptures)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.SelectAll,
                                contentDescription = stringResource(R.string.seleccionar_todo),
                                tint = Color.White
                            )
                        }
                        if (selectedCaptures.isNotEmpty()) {
                            IconButton(onClick = { showDeleteConfirmDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.eliminar_seleccionados),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    } else {
                        IconButton(onClick = { showOnlyFavorites = !showOnlyFavorites }) {
                            Icon(
                                imageVector = if (showOnlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = stringResource(R.string.favoritos),
                                tint = if (showOnlyFavorites) Color(0xFFFF4081) else Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF101728)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0A0F1D))
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (captures.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                stringResource(R.string.a_n_no_has_guardado_ninguna_captura),
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (selectedVideoGroup == null) {
                            items(groupedCaptures.entries.toList()) { (videoName, groupList) ->
                                val coverCapture = groupList.first()
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141C2E)),
                                    border = BorderStroke(1.dp, Color(0xFF23304A)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedVideoGroup = videoName
                                        }
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp)
                                        ) {
                                            AsyncImage(
                                                model = coverCapture.imagePath,
                                                contentDescription = "Portada de video",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .padding(10.dp)
                                                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.PhotoLibrary,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(Modifier.width(4.dp))
                                                    Text(
                                                        text = "${groupList.size}",
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                            Text(
                                                text = dateFormat.format(Date(coverCapture.timestamp)),
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = coverCapture.captureType,
                                                color = Color.Gray,
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            val groupPhotos = groupedCaptures[selectedVideoGroup] ?: emptyList()
                            items(groupPhotos) { capture ->
                                val isSelected = selectedCaptures.contains(capture)
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141C2E)),
                                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF00E5FF) else Color(0xFF23304A)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = {
                                                if (isSelectionMode) {
                                                    if (isSelected) selectedCaptures.remove(capture)
                                                    else selectedCaptures.add(capture)
                                                    if (selectedCaptures.isEmpty()) isSelectionMode = false
                                                } else {
                                                    selectedCaptureDetail = capture
                                                }
                                            },
                                            onLongClick = {
                                                if (!isSelectionMode) {
                                                    isSelectionMode = true
                                                    selectedCaptures.add(capture)
                                                }
                                            }
                                        )
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp)
                                        ) {
                                            AsyncImage(
                                                model = capture.imagePath,
                                                contentDescription = "Captura",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop,
                                                alpha = if (isSelected) 0.5f else 1f
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color(0xFF00E5FF),
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(8.dp)
                                                        .background(Color.Black, CircleShape)
                                                )
                                            }
                                        }
                                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                            Text(
                                                text = dateFormat.format(Date(capture.timestamp)),
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = capture.captureType,
                                                color = Color.Gray,
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            AdBanner()
        }
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text(stringResource(R.string.confirmar_eliminacion)) },
                text = { Text(stringResource(R.string.mensaje_confirmar_eliminacion)) },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmDialog = false
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    selectedCaptures.forEach { capture ->
                                        val file = File(capture.imagePath)
                                        if (file.exists()) file.delete()
                                    }
                                    db.captureDao().deleteCaptures(selectedCaptures.toList())
                                }
                                Toast.makeText(context, context.getString(R.string.eliminar_exito), Toast.LENGTH_SHORT).show()
                                isSelectionMode = false
                                selectedCaptures.clear()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.eliminar))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text(stringResource(R.string.cancelar))
                    }
                }
            )
        }
        AnimatedVisibility(
            visible = selectedCaptureDetail != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            selectedCaptureDetail?.let { capture ->
                FullScreenDetail(
                    capture = capture,
                    onDismiss = { selectedCaptureDetail = null },
                    onShare = { shareImage(context, capture.imagePath) },
                    onRecrop = {
                        val bitmap = loadBitmapFromPathOrUri(context, capture.imagePath)
                        if (bitmap != null) {
                            selectedCaptureDetail = null
                            launchCropper(context, bitmap, cropLauncher, selectedFormat)
                        }
                    },
                    selectedFormat = selectedFormat,
                    context = context,
                    db = db
                )
            }
        }
    }
}
@Composable
fun WelcomeHeroScreen(
    context: Context,
    onSelectVideo: () -> Unit,
    onOpenGallery: () -> Unit,
    onSelectRecentVideo: (Uri) -> Unit
) {
    val recentVideos = remember { RecentVideoStore.getRecentVideos(context) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A0F1D), Color(0xFF141C2E), Color(0xFF0D1322))
                )
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFF1A2338), CircleShape)
                    .border(2.dp, Color(0xFF00E5FF), CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logoc),
                    contentDescription = "Logo",
                    modifier = Modifier.size(80.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.bienvenido),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.selecciona_un_video_de_tu_galer_a_para_extraer_los_mejores_fotogramas),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onSelectVideo,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E68D7)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(54.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.nuevo_video),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onOpenGallery,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF23304A)),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "FOTOS GUARDADAS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            if (recentVideos.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "🎥 Videos recientes",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(bottom = 8.dp)
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(recentVideos) { item ->
                        val uri = Uri.parse(item.uriString)
                        var videoThumbnail by remember(item.uriString) { mutableStateOf<Bitmap?>(null) }
                        LaunchedEffect(item.uriString) {
                            withContext(Dispatchers.IO) {
                                val retriever = MediaMetadataRetriever()
                                try {
                                    retriever.setDataSource(context, uri)
                                    val targetMs = if (item.durationMs > 1000L) 1000L else (item.durationMs / 2)
                                    val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                                        retriever.getScaledFrameAtTime(targetMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, 240, 240)
                                    } else {
                                        retriever.getFrameAtTime(targetMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                                    }
                                    if (bmp != null && !isBitmapBlackOrEmpty(bmp)) {
                                        withContext(Dispatchers.Main) {
                                            videoThumbnail = bmp
                                        }
                                    }
                                } catch (_: Exception) {
                                } finally {
                                    try { retriever.release() } catch (_: Exception) {}
                                }
                            }
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF141C2E)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF23304A)),
                            modifier = Modifier
                                .width(130.dp)
                                .clickable { onSelectRecentVideo(uri) }
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(85.dp)
                                        .background(Color.Black),
                                    contentAlignment = Alignment.Center
                                ) {
                                    videoThumbnail?.let { bmp ->
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = "Miniatura de video",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } ?: Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(4.dp)
                                            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = formatTimeMsShort(item.durationMs),
                                            color = Color(0xFF00E5FF),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = item.name,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenDetail(
    capture: CaptureEntity,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onRecrop: () -> Unit,
    selectedFormat: ExportFormat,
    context: Context,
    db: AppDatabase
) {
    val zoomableState = rememberZoomableState()
    var baseBitmap by remember(capture) {
        mutableStateOf(loadBitmapFromPathOrUri(context, capture.imagePath))
    }
    var currentFilter by remember { mutableStateOf(FilterType.ORIGINAL) }
    var isFavoriteState by remember { mutableStateOf(capture.isFavorite) }
    var showInfoModal by remember { mutableStateOf(false) }
    var showEditTagDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val currentBitmap = remember(baseBitmap, currentFilter) {
        baseBitmap?.let { applyColorFilter(it, currentFilter) }
    }
    BackHandler { onDismiss() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Zoomable(
            state = zoomableState,
            modifier = Modifier.fillMaxSize(),
        ) {
            currentBitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Detalle",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
        TopAppBar(
            modifier = Modifier.fillMaxWidth(),
            title = { },
            navigationIcon = {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.volver),
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(onClick = { showEditTagDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "Editar",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { showInfoModal = true }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Información",
                        tint = Color.White
                    )
                }
                IconButton(onClick = {
                    val nextFav = !isFavoriteState
                    isFavoriteState = nextFav
                    scope.launch {
                        db.captureDao().updateFavorite(capture.id, nextFav)
                    }
                }) {
                    Icon(
                        imageVector = if (isFavoriteState) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(R.string.favoritos),
                        tint = if (isFavoriteState) Color(0xFFFF4081) else Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black.copy(alpha = 0.4f)
            )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(Color(0xFF1E2230).copy(alpha = 0.85f))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                FilterType.entries.forEach { filter ->
                    val isSel = currentFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSel) Color(0xFF00E5FF) else Color.Transparent)
                            .clickable { currentFilter = filter }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = filter.name,
                            color = if (isSel) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .background(Color(0xFF1E2230).copy(alpha = 0.85f), CircleShape)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    baseBitmap?.let { bmp ->
                        val enhanced = enhanceBitmapQuality(bmp)
                        baseBitmap = enhanced
                        Toast.makeText(context, context.getString(R.string.optimizado_exito), Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = stringResource(R.string.optimizar_hd),
                        tint = Color(0xFF00E5FF)
                    )
                }
                IconButton(onClick = {
                    baseBitmap?.let { bmp ->
                        val upscaled = upscaleBitmap2x(bmp)
                        baseBitmap = upscaled
                        Toast.makeText(context, context.getString(R.string.upscale_exito), Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(
                        Icons.Default.HighQuality,
                        contentDescription = stringResource(R.string.upscale_2x),
                        tint = Color(0xFFFF9100)
                    )
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = stringResource(R.string.compartir), tint = Color.White)
                }
                IconButton(onClick = onRecrop) {
                    Icon(
                        Icons.Default.Create,
                        contentDescription = stringResource(R.string.recortar),
                        tint = Color.White
                    )
                }
                IconButton(onClick = {
                    scope.launch {
                        currentBitmap?.let { bmp ->
                            val uri = saveToPublicGallery(context, bmp, selectedFormat)
                            if (uri != null) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.guardado_galeria_publica),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = stringResource(R.string.descargar),
                        tint = Color.White
                    )
                }
            }
        }
    }
    if (showInfoModal) {
        PhotoMetadataModal(
            capture = capture,
            bitmap = currentBitmap,
            onDismiss = { showInfoModal = false }
        )
    }
    if (showEditTagDialog) {
        EditCaptureTagModal(
            capture = capture,
            onSave = { newName, newTag ->
                scope.launch {
                    db.captureDao().updateCaptureNameAndTag(capture.id, newName, newTag)
                }
            },
            onDismiss = { showEditTagDialog = false }
        )
    }
}
@Composable
fun EditCaptureTagModal(
    capture: CaptureEntity,
    onSave: (newName: String, newTag: String) -> Unit,
    onDismiss: () -> Unit
) {
    var nameText by remember { mutableStateOf(capture.videoName) }
    var tagText by remember { mutableStateOf(capture.captureType) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Create, contentDescription = null, tint = Color(0xFF00E5FF))
                Spacer(Modifier.width(8.dp))
                Text("Renombrar y Etiquetar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Nombre de la Foto", color = Color.LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF23304A)
                    ),
                    singleLine = true
                )
                OutlinedTextField(
                    value = tagText,
                    onValueChange = { tagText = it },
                    label = { Text("Etiqueta Personalizada", color = Color.LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF23304A)
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(nameText.trim(), tagText.trim())
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E68D7))
            ) {
                Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF141C2E),
        shape = RoundedCornerShape(16.dp)
    )
}
@Composable
fun PhotoMetadataModal(
    capture: CaptureEntity,
    bitmap: Bitmap?,
    onDismiss: () -> Unit
) {
    val file = remember(capture.imagePath) { File(capture.imagePath) }
    val fileSizeText = remember(file) {
        if (file.exists()) {
            val bytes = file.length()
            if (bytes > 1024 * 1024) String.format(Locale.US, "%.2f MB", bytes / (1024f * 1024f))
            else "${bytes / 1024} KB"
        } else "N/A"
    }
    val width = bitmap?.width ?: 0
    val height = bitmap?.height ?: 0
    val megaPixels = if (width > 0 && height > 0) String.format(Locale.US, "%.1f MP", (width * height) / 1_000_000f) else ""
    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy, h:mm:ss a", Locale.getDefault()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF00E5FF))
                Spacer(Modifier.width(8.dp))
                Text("Detalles de la Foto", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MetadataRow(label = "📁 Nombre del Video", value = capture.videoName)
                MetadataRow(label = "📐 Resolución", value = if (width > 0) "$width x $height ($megaPixels)" else "N/A")
                MetadataRow(label = "💾 Tamaño de Archivo", value = fileSizeText)
                MetadataRow(label = "⚙️ Método de Captura", value = capture.captureType)
                MetadataRow(label = "📅 Fecha de Guardado", value = dateFormat.format(Date(capture.timestamp)))
                MetadataRow(label = "📍 Ruta de Archivo", value = capture.imagePath)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF141C2E),
        shape = RoundedCornerShape(16.dp)
    )
}
@Composable
fun MetadataRow(label: String, value: String) {
    Column {
        Text(text = label, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Text(text = value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoSelectionInterface(
    frames: List<ScoredFrame>,
    onSave: (List<ScoredFrame>) -> Unit,
    isSaving: Boolean,
    onClose: () -> Unit,
    selectedFormat: ExportFormat,
) {
    val context = LocalContext.current
    var showCompareDialog by remember { mutableStateOf(false) }
    var filterSmileAndEyes by remember { mutableStateOf(false) }
    var isExportingZip by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val displayedFrames = remember(frames, filterSmileAndEyes) {
        if (filterSmileAndEyes) {
            frames.filter { it.hasFace && (it.isSmiling || it.eyesOpen) }
        } else {
            frames
        }
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0A0F1D)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.seleccionar_fotogramas),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onClose, enabled = !isSaving) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.volver),
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        val selectedCount = frames.count { it.isSelected.value }
                        IconButton(onClick = {
                            val bestFrames = frames.filter { it.isBestQuality || it.hasFace }
                            if (bestFrames.isNotEmpty()) {
                                frames.forEach { frame ->
                                    frame.isSelected.value = frame.isBestQuality || frame.hasFace
                                }
                            } else {
                                val allSelected = frames.all { it.isSelected.value }
                                frames.forEach { it.isSelected.value = !allSelected }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Seleccionar mejores IA",
                                tint = Color(0xFF00E5FF)
                            )
                        }
                        IconButton(onClick = {
                            val allSelected = frames.all { it.isSelected.value }
                            frames.forEach { it.isSelected.value = !allSelected }
                        }) {
                            Icon(
                                imageVector = Icons.Default.SelectAll,
                                contentDescription = stringResource(R.string.seleccionar_todo),
                                tint = Color.White
                            )
                        }
                        TextButton(
                            onClick = { onSave(frames.filter { it.isSelected.value }) },
                            enabled = selectedCount > 0 && !isSaving
                        ) {
                            Text(
                                text = "${stringResource(R.string.guardar)} ($selectedCount)",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedCount > 0 && !isSaving) Color(0xFF00E5FF) else Color.Gray
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF101728)
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFF0A0F1D))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = filterSmileAndEyes,
                        onClick = { filterSmileAndEyes = !filterSmileAndEyes },
                        label = { Text(if (filterSmileAndEyes) "👥 Sonrisas/Ojos: SI" else "👥 Solo Sonrisas/Ojos", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1E68D7),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF141C2E),
                            labelColor = Color.LightGray
                        )
                    )
                    val selectedCount = frames.count { it.isSelected.value }
                    if (selectedCount == 2) {
                        OutlinedButton(
                            onClick = { showCompareDialog = true },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF))
                        ) {
                            Text("🔍 Comparar (2)", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            val selBitmaps = frames.filter { it.isSelected.value }.map { it.bitmap }
                            if (selBitmaps.isNotEmpty() && !isExportingZip) {
                                isExportingZip = true
                                scope.launch {
                                    Toast.makeText(context, "📦 Creando archivo ZIP...", Toast.LENGTH_SHORT).show()
                                    val zipUri = exportFramesToZip(context, selBitmaps, selectedFormat)
                                    isExportingZip = false
                                    if (zipUri != null) {
                                        Toast.makeText(context, "📦 ZIP guardado en Descargas/VideoToPhoto", Toast.LENGTH_LONG).show()
                                        try {
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = "application/zip"
                                                putExtra(Intent.EXTRA_STREAM, zipUri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(intent, "Compartir ZIP"))
                                        } catch (_: Exception) {}
                                    } else {
                                        Toast.makeText(context, "Error al crear archivo ZIP", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF9100)),
                        enabled = selectedCount > 0 && !isExportingZip
                    ) {
                        Text("📦 ZIP ($selectedCount)", color = Color(0xFFFF9100), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    var isGeneratingGif by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = {
                            val selBitmaps = frames.filter { it.isSelected.value }.map { it.bitmap }
                            if (selBitmaps.isNotEmpty() && !isGeneratingGif) {
                                isGeneratingGif = true
                                scope.launch {
                                    Toast.makeText(context, "🎞️ Creando GIF Boomerang...", Toast.LENGTH_SHORT).show()
                                    val gifUri = exportBitmapsToAnimatedGif(context, selBitmaps, isBoomerang = true)
                                    isGeneratingGif = false
                                    if (gifUri != null) {
                                        Toast.makeText(context, "🎞️ GIF guardado en Fotos/VideoToPhoto", Toast.LENGTH_LONG).show()
                                        try {
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = "image/gif"
                                                putExtra(Intent.EXTRA_STREAM, gifUri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(intent, "Compartir GIF"))
                                        } catch (_: Exception) {}
                                    } else {
                                        Toast.makeText(context, "Error al crear GIF", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                        enabled = selectedCount > 0 && !isGeneratingGif
                    ) {
                        Text("🎞️ GIF ($selectedCount)", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    var chosenStripColor by remember { mutableStateOf(StripColor.BLACK) }
                    OutlinedButton(
                        onClick = {
                            val selBitmaps = frames.filter { it.isSelected.value }.map { it.bitmap }
                            if (selBitmaps.isNotEmpty()) {
                                scope.launch {
                                    Toast.makeText(context, "📸 Creando Tira de Fotos...", Toast.LENGTH_SHORT).show()
                                    val collageBmp = createPhotoStripCollage(selBitmaps, chosenStripColor.colorInt)
                                    val uri = saveToPublicGallery(context, collageBmp, selectedFormat)
                                    if (uri != null) {
                                        Toast.makeText(context, "📸 Tira de fotos guardada en Galería", Toast.LENGTH_LONG).show()
                                        shareImage(context, uri.toString())
                                    }
                                }
                            }
                        },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF4081)),
                        enabled = selectedCount > 0
                    ) {
                        Text("📸 Tira (${chosenStripColor.displayName})", color = Color(0xFFFF4081), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (displayedFrames.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            if (filterSmileAndEyes) "No se encontraron rostros sonrientes en la selección" else stringResource(R.string.no_se_pudieron_extraer_fotogramas),
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayedFrames) { frame ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                border = if (frame.isBestQuality) BorderStroke(2.dp, Color(0xFF00E5FF)) else null,
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF141C2E)),
                                modifier = Modifier.aspectRatio(1f)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Image(
                                        bitmap = frame.bitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    if (frame.isBestQuality || frame.hasFace) {
                                        Row(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(4.dp)
                                                .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            if (frame.isBestQuality) {
                                                Text("✦ MEJOR", color = Color(0xFF00E5FF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                            if (frame.isSmiling) {
                                                Text("😊", fontSize = 9.sp)
                                            } else if (frame.eyesOpen) {
                                                Text("👁️", fontSize = 9.sp)
                                            }
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                    ) {
                                        Checkbox(
                                            checked = frame.isSelected.value,
                                            onCheckedChange = {
                                                if (!isSaving) frame.isSelected.value = it
                                            },
                                            enabled = !isSaving,
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Color(0xFF00E5FF),
                                                checkmarkColor = Color.Black
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (showCompareDialog) {
        val selTwo = frames.filter { it.isSelected.value }.take(2)
        if (selTwo.size == 2) {
            SideBySideCompareModal(
                frame1 = selTwo[0],
                frame2 = selTwo[1],
                onSelectOne = { winner ->
                    frames.forEach { it.isSelected.value = (it == winner) }
                },
                onDismiss = { showCompareDialog = false }
            )
        }
    }
}
suspend fun extractVideoMetadata(context: Context, uri: Uri): VideoMetaData = withContext(Dispatchers.IO) {
    try {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    } catch (_: Exception) {}
    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(context, uri)
        val name = getVideoFileName(context, uri)
        var dur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        if (dur == 0L) {
            val extractor = MediaExtractor()
            try {
                extractor.setDataSource(context, uri, null)
                for (i in 0 until extractor.trackCount) {
                    val format = extractor.getTrackFormat(i)
                    if (format.containsKey(android.media.MediaFormat.KEY_DURATION)) {
                        val durUs = format.getLong(android.media.MediaFormat.KEY_DURATION)
                        if (durUs > 0) {
                            dur = durUs / 1000
                            break
                        }
                    }
                }
            } catch (_: Exception) {
            } finally {
                try { extractor.release() } catch (_: Exception) {}
            }
        }
        val w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 1080
        val h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 1920
        val rot = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
        val (finalW, finalH) = if (rot == 90 || rot == 270) Pair(h, w) else Pair(w, h)
        VideoMetaData(
            fileName = name,
            durationMs = dur,
            width = finalW,
            height = finalH,
            resolutionText = "${finalW}x${finalH}"
        )
    } catch (e: Exception) {
        VideoMetaData()
    } finally {
        try { retriever.release() } catch (_: Exception) {}
    }
}
fun getVideoFileName(context: Context, uri: Uri): String {
    var name = "video.mp4"
    try {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                if (idx != -1) {
                    val displayName = it.getString(idx)
                    if (!displayName.isNullOrEmpty()) name = displayName
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return name
}
fun formatTimeMsShort(timeMs: Long): String {
    val totalSec = timeMs / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format(Locale.US, "%02d:%02d", min, sec)
}
fun isBitmapBlackOrEmpty(bitmap: Bitmap): Boolean {
    try {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= 0 || h <= 0) return true
        var totalLuminance = 0f
        val samplePoints = listOf(
            Pair(w / 4, h / 4), Pair(w / 2, h / 4), Pair(3 * w / 4, h / 4),
            Pair(w / 4, h / 2), Pair(w / 2, h / 2), Pair(3 * w / 4, h / 2),
            Pair(w / 4, 3 * h / 4), Pair(w / 2, 3 * h / 4), Pair(3 * w / 4, 3 * h / 4)
        )
        for ((x, y) in samplePoints) {
            val pixel = bitmap.getPixel(x.coerceIn(0, w - 1), y.coerceIn(0, h - 1))
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            val lum = 0.299f * r + 0.587f * g + 0.114f * b
            totalLuminance += lum
        }
        val avgLuminance = totalLuminance / samplePoints.size
        return avgLuminance < 8f
    } catch (e: Exception) {
        return false
    }
}
fun calculateSharpnessScore(bitmap: Bitmap): Float {
    return try {
        val scaled = Bitmap.createScaledBitmap(bitmap, 120, 120, false)
        val width = scaled.width
        val height = scaled.height
        val pixels = IntArray(width * height)
        scaled.getPixels(pixels, 0, width, 0, 0, width, height)
        var totalLaplacian = 0.0
        var count = 0
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val center = pixels[y * width + x] and 0xFF
                val left = pixels[y * width + (x - 1)] and 0xFF
                val right = pixels[y * width + (x + 1)] and 0xFF
                val top = pixels[(y - 1) * width + x] and 0xFF
                val bottom = pixels[(y + 1) * width + x] and 0xFF
                val lap = abs(4 * center - left - right - top - bottom)
                totalLaplacian += lap
                count++
            }
        }
        if (count > 0) (totalLaplacian / count).toFloat() else 0f
    } catch (e: Exception) {
        0f
    }
}
suspend fun analyzeFaceAndSmile(bitmap: Bitmap): FaceAnalysisResult = withContext(Dispatchers.IO) {
    try {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
        val detector = FaceDetection.getClient(options)
        val image = InputImage.fromBitmap(bitmap, 0)
        val task = detector.process(image)
        val faces: List<Face> = Tasks.await(task)
        detector.close()
        if (faces.isNotEmpty()) {
            val face = faces[0]
            val smileProb = face.smilingProbability ?: 0f
            val leftEye = face.leftEyeOpenProbability ?: 0f
            val rightEye = face.rightEyeOpenProbability ?: 0f
            FaceAnalysisResult(
                hasFace = true,
                isSmiling = smileProb > 0.35f,
                eyesOpen = leftEye > 0.4f && rightEye > 0.4f,
                faceCount = faces.size
            )
        } else {
            FaceAnalysisResult(hasFace = false)
        }
    } catch (e: Exception) {
        FaceAnalysisResult(hasFace = false)
    }
}
fun analyzeColorAndLighting(bitmap: Bitmap): String? {
    try {
        val scaled = Bitmap.createScaledBitmap(bitmap, 60, 60, false)
        val w = scaled.width
        val h = scaled.height
        val pixels = IntArray(w * h)
        scaled.getPixels(pixels, 0, w, 0, 0, w, h)
        var totalR = 0f
        var totalG = 0f
        var totalB = 0f
        var totalLum = 0f
        for (p in pixels) {
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            val lum = 0.299f * r + 0.587f * g + 0.114f * b
            totalR += r
            totalG += g
            totalB += b
            totalLum += lum
        }
        val count = pixels.size.toFloat()
        val avgR = totalR / count
        val avgG = totalG / count
        val avgB = totalB / count
        val avgLum = totalLum / count
        return when {
            avgR > 160 && avgG in 100f..180f && avgB < 120 -> "🌅 Atardecer"
            avgG > avgR * 1.15f && avgG > avgB * 1.15f -> "🌿 Naturaleza"
            avgLum < 45f && avgB > avgR -> "🌙 Cine / Noche"
            avgLum in 110f..205f && abs(avgR - avgB) < 35f -> "☀️ Luz Perfecta"
            else -> null
        }
    } catch (e: Exception) {
        return null
    }
}
fun analyzeActionIntensity(bitmap: Bitmap): Boolean {
    try {
        val scaled = Bitmap.createScaledBitmap(bitmap, 80, 80, false)
        val w = scaled.width
        val h = scaled.height
        val pixels = IntArray(w * h)
        scaled.getPixels(pixels, 0, w, 0, 0, w, h)
        var totalGrad = 0.0
        for (y in 0 until h - 1) {
            for (x in 0 until w - 1) {
                val p1 = pixels[y * w + x] and 0xFF
                val p2 = pixels[y * w + (x + 1)] and 0xFF
                val p3 = pixels[(y + 1) * w + x] and 0xFF
                val diff = abs(p1 - p2) + abs(p1 - p3)
                totalGrad += diff
            }
        }
        val avgGrad = totalGrad / (w * h)
        return avgGrad > 32.0
    } catch (e: Exception) {
        return false
    }
}
fun analyzePetAndObjects(bitmap: Bitmap): String? {
    try {
        val scaled = Bitmap.createScaledBitmap(bitmap, 60, 60, false)
        val w = scaled.width
        val h = scaled.height
        val pixels = IntArray(w * h)
        scaled.getPixels(pixels, 0, w, 0, 0, w, h)
        var brownOrangeCount = 0
        var greenCount = 0
        var skyBlueCount = 0
        for (p in pixels) {
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            if (r in 110..220 && g in 70..160 && b in 30..110) brownOrangeCount++
            if (g > r * 1.2f && g > b * 1.2f) greenCount++
            if (b > r * 1.2f && b > g && r < 140) skyBlueCount++
        }
        val total = pixels.size.toFloat()
        val brownRatio = brownOrangeCount / total
        val greenRatio = greenCount / total
        val skyRatio = skyBlueCount / total
        return when {
            brownRatio > 0.28f -> "🐶 Mascota / Pelaje"
            greenRatio > 0.30f || skyRatio > 0.30f -> "🌅 Paisaje"
            else -> null
        }
    } catch (e: Exception) {
        return null
    }
}
fun upscaleBitmap2x(source: Bitmap): Bitmap {
    val width = source.width * 2
    val height = source.height * 2
    val scaled = Bitmap.createScaledBitmap(source, width, height, true)
    return enhanceBitmapQuality(scaled)
}
fun enhanceBitmapQuality(source: Bitmap): Bitmap {
    val width = source.width
    val height = source.height
    val output = Bitmap.createBitmap(width, height, source.config ?: Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    val cm = ColorMatrix()
    cm.setSaturation(1.10f)
    val scale = 1.08f
    val translate = (-0.5f * scale + 0.5f) * 255f
    val contrastMatrix = floatArrayOf(
        scale, 0f, 0f, 0f, translate,
        0f, scale, 0f, 0f, translate,
        0f, 0f, scale, 0f, translate,
        0f, 0f, 0f, 1f, 0f
    )
    cm.postConcat(ColorMatrix(contrastMatrix))
    paint.colorFilter = ColorMatrixColorFilter(cm)
    canvas.drawBitmap(source, 0f, 0f, paint)
    return output
}
fun applyColorFilter(source: Bitmap, filter: FilterType): Bitmap {
    if (filter == FilterType.ORIGINAL) return source
    val width = source.width
    val height = source.height
    val output = Bitmap.createBitmap(width, height, source.config ?: Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    val cm = ColorMatrix()
    when (filter) {
        FilterType.CINE -> {
            cm.setSaturation(0.9f)
            val matrix = floatArrayOf(
                1.1f, 0f, 0f, 0f, 5f,
                0f, 1.05f, 0f, 0f, 0f,
                0f, 0f, 1.2f, 0f, 10f,
                0f, 0f, 0f, 1f, 0f
            )
            cm.postConcat(ColorMatrix(matrix))
        }
        FilterType.BW -> {
            cm.setSaturation(0f)
        }
        FilterType.VIVID -> {
            cm.setSaturation(1.35f)
        }
        FilterType.WARM -> {
            val matrix = floatArrayOf(
                1.15f, 0f, 0f, 0f, 15f,
                0f, 1.05f, 0f, 0f, 5f,
                0f, 0f, 0.9f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            )
            cm.postConcat(ColorMatrix(matrix))
        }
        FilterType.RETRO -> {
            val matrix = floatArrayOf(
                1.2f, 0.1f, 0.1f, 0f, 20f,
                0.1f, 1.0f, 0.1f, 0f, 15f,
                0.1f, 0.1f, 0.8f, 0f, -15f,
                0f, 0f, 0f, 1f, 0f
            )
            cm.postConcat(ColorMatrix(matrix))
        }
        FilterType.HDR -> {
            val matrix = floatArrayOf(
                1.3f, -0.1f, -0.1f, 0f, -10f,
                -0.1f, 1.3f, -0.1f, 0f, -10f,
                -0.1f, -0.1f, 1.3f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            )
            cm.postConcat(ColorMatrix(matrix))
        }
        FilterType.DRAMATIC_BW -> {
            cm.setSaturation(0f)
            val contrastMatrix = floatArrayOf(
                1.6f, 0f, 0f, 0f, -35f,
                0f, 1.6f, 0f, 0f, -35f,
                0f, 0f, 1.6f, 0f, -35f,
                0f, 0f, 0f, 1f, 0f
            )
            cm.postConcat(ColorMatrix(contrastMatrix))
        }
        FilterType.ORIGINAL -> {}
    }
    paint.colorFilter = ColorMatrixColorFilter(cm)
    canvas.drawBitmap(source, 0f, 0f, paint)
    return output
}
suspend fun runAutoExtractionParallel(
    context: Context,
    uri: Uri,
    count: Int,
    intervalMs: Long,
    startTimeMs: Long = 0L,
    endTimeMs: Long = 0L,
): List<ScoredFrame> = withContext(Dispatchers.IO) {
    val maxFrames = 30
    val actualCount = count.coerceIn(1, maxFrames)
    val effectiveRangeMs = if (endTimeMs > startTimeMs) endTimeMs - startTimeMs else count * intervalMs
    val effectiveIntervalMs = if (count > maxFrames && actualCount > 0) effectiveRangeMs / actualCount else intervalMs
    val frames = mutableListOf<ScoredFrame>()
    val workerCount = 4
    val batchSize = (actualCount + workerCount - 1) / workerCount
    val deferredBatches = (0 until workerCount).map { worker ->
        async(Dispatchers.IO) {
            val batchList = mutableListOf<ScoredFrame>()
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                val startIdx = worker * batchSize
                val endIdx = (startIdx + batchSize).coerceAtMost(actualCount)
                for (i in startIdx until endIdx) {
                    val targetMs = startTimeMs + (i * effectiveIntervalMs)
                    if (endTimeMs > 0 && targetMs > endTimeMs) break
                    val timeUs = targetMs * 1000L
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        retriever.getScaledFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, 640, 640)
                    } else {
                        retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    }
                    if (bitmap != null && !isBitmapBlackOrEmpty(bitmap)) {
                        val score = calculateSharpnessScore(bitmap)
                        val faceRes = analyzeFaceAndSmile(bitmap)
                        var compositeScore = score
                        if (faceRes.hasFace) compositeScore += 40f
                        if (faceRes.eyesOpen) compositeScore += 30f
                        if (faceRes.isSmiling) compositeScore += 30f
                        batchList.add(
                            ScoredFrame(
                                bitmap = bitmap,
                                timestampMs = timeUs / 1000,
                                sharpnessScore = compositeScore,
                                hasFace = faceRes.hasFace,
                                isSmiling = faceRes.isSmiling,
                                eyesOpen = faceRes.eyesOpen
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("Capture", "Error worker $worker: ${e.message}")
            } finally {
                try { retriever.release() } catch (_: Exception) {}
            }
            batchList
        }
    }
    val allBatches = deferredBatches.awaitAll()
    allBatches.forEach { frames.addAll(it) }
    frames.sortBy { it.timestampMs }
    val topScore = frames.maxOfOrNull { it.sharpnessScore } ?: 0f
    frames.forEach { frame ->
        if (topScore > 0 && frame.sharpnessScore >= topScore * 0.85f) {
            frame.isBestQuality = true
            frame.isSelected.value = true
        } else {
            frame.isSelected.value = false
        }
    }
    frames
}
suspend fun findBestFrameNearTimestamp(
    context: Context,
    videoUri: Uri,
    centerMs: Long,
    durationMs: Long,
): Long = withContext(Dispatchers.IO) {
    val retriever = MediaMetadataRetriever()
    var bestTimestampMs = centerMs
    var maxScore = -1f
    try {
        retriever.setDataSource(context, videoUri)
        val offsets = (-5..5).map { it * 100L }
        for (offset in offsets) {
            val sampleMs = (centerMs + offset).coerceIn(0L, durationMs)
            val sampleUs = sampleMs * 1000L
            val bitmap = retriever.getFrameAtTime(sampleUs, MediaMetadataRetriever.OPTION_CLOSEST)
            if (bitmap != null && !isBitmapBlackOrEmpty(bitmap)) {
                val sharpness = calculateSharpnessScore(bitmap)
                val face = analyzeFaceAndSmile(bitmap)
                var score = sharpness
                if (face.hasFace) score += 40f
                if (face.eyesOpen) score += 30f
                if (face.isSmiling) score += 30f
                if (score > maxScore) {
                    maxScore = score
                    bestTimestampMs = sampleMs
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        try { retriever.release() } catch (_: Exception) {}
    }
    bestTimestampMs
}
suspend fun saveToPublicGallery(
    context: Context,
    bitmap: Bitmap,
    format: ExportFormat,
    quality: ExportQuality = ExportQuality.HIGH
): Uri? = withContext(Dispatchers.IO) {
    val extension = format.extension
    val mimeType = if (format == ExportFormat.PNG) "image/png" else "image/jpeg"
    val filename = "Capture_${System.currentTimeMillis()}.$extension"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/VideoToPhoto"
            )
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    uri?.let { targetUri ->
        resolver.openOutputStream(targetUri)?.use { output ->
            val compressFormat = if (format == ExportFormat.PNG)
                Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
            bitmap.compress(compressFormat, quality.qualityInt, output)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(targetUri, contentValues, null, null)
        }
        targetUri
    }
}
fun loadBitmapFromPathOrUri(context: Context, pathOrUri: String): Bitmap? {
    return try {
        if (pathOrUri.startsWith("content://") || pathOrUri.startsWith("file://")) {
            val uri = Uri.parse(pathOrUri)
            context.contentResolver.openInputStream(uri)?.use { stream ->
                android.graphics.BitmapFactory.decodeStream(stream)
            }
        } else {
            val file = File(pathOrUri)
            if (file.exists()) {
                android.graphics.BitmapFactory.decodeFile(file.absolutePath)
            } else {
                val uri = Uri.parse(pathOrUri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    android.graphics.BitmapFactory.decodeStream(stream)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
suspend fun saveBitmapToInternalStorage(
    context: Context,
    bitmap: Bitmap,
    format: ExportFormat,
    quality: ExportQuality = ExportQuality.HIGH
): String = withContext(Dispatchers.IO) {
    val extension = format.extension
    val filename = "Capture_${System.currentTimeMillis()}.$extension"
    val file = File(context.filesDir, filename)
    file.outputStream().use { out ->
        val compressFormat = if (format == ExportFormat.PNG) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        bitmap.compress(compressFormat, quality.qualityInt, out)
    }
    file.absolutePath
}
suspend fun saveImageToInternalStorage(
    context: Context,
    uri: Uri,
    format: ExportFormat,
): String {
    return withContext(Dispatchers.IO) {
        val extension = format.extension
        val filename = "Capture_${System.currentTimeMillis()}.$extension"
        val file = File(context.filesDir, filename)
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    }
}
suspend fun saveBitmapToTempUri(
    context: Context,
    bitmap: Bitmap,
    format: ExportFormat,
): Uri {
    return withContext(Dispatchers.IO) {
        val extension = format.extension
        val tempFile = File(context.cacheDir, "auto_capture_${System.currentTimeMillis()}.$extension")
        try {
            tempFile.outputStream().use { out ->
                val compressFormat = if (format == ExportFormat.PNG) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                bitmap.compress(compressFormat, 100, out)
                out.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Uri.fromFile(tempFile)
    }
}
fun shareImage(context: Context, pathOrUri: String) {
    try {
        val uri = if (pathOrUri.startsWith("content://")) {
            Uri.parse(pathOrUri)
        } else {
            val file = File(pathOrUri)
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }
        val mimeType = if (pathOrUri.lowercase().endsWith("png")) "image/png" else "image/jpeg"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.compartir_captura_con)))
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.error_al_compartir_la_imagen), Toast.LENGTH_SHORT).show()
    }
}
fun launchCropper(
    context: Context,
    bitmap: Bitmap,
    launcher: ActivityResultLauncher<CropImageContractOptions>,
    format: ExportFormat,
) {
    val tempFile = File(context.cacheDir, "temp_capture.${format.extension}")
    tempFile.outputStream().use { out ->
        val compressFormat = if (format == ExportFormat.PNG) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        bitmap.compress(compressFormat, 100, out)
    }
    val uri = Uri.fromFile(tempFile)
    val options = CropImageContractOptions(uri, CropImageOptions().apply {
        guidelines = CropImageView.Guidelines.ON
        showProgressBar = true
        cropMenuCropButtonTitle = context.getString(R.string.guardar)
        activityTitle = context.getString(R.string.editar_captura)
        activityMenuIconColor = android.graphics.Color.WHITE
        allowRotation = true
        allowFlipping = true
        autoZoomEnabled = true
        outputCompressFormat = if (format == ExportFormat.PNG) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        outputCompressQuality = 100
    })
    launcher.launch(options)
}
@Composable
fun LoadingOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2230)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logoc),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .graphicsLayer(alpha = alpha)
                )
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.guardando), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
fun drawTimestampWatermark(source: Bitmap, timestampMs: Long): Bitmap {
    val result = source.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(result)
    val timeText = formatTimeMsShort(timestampMs)
    val watermarkText = "🕒 $timeText"
    val textSize = (source.height * 0.038f).coerceAtLeast(28f)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        this.textSize = textSize
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    val padding = textSize * 0.5f
    val textWidth = paint.measureText(watermarkText)
    val x = source.width - textWidth - padding * 2
    val y = source.height - padding * 1.5f
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(160, 0, 0, 0)
    }
    val rect = RectF(
        x - padding,
        y - textSize - padding / 2,
        x + textWidth + padding,
        y + padding / 2
    )
    canvas.drawRoundRect(rect, padding * 0.5f, padding * 0.5f, bgPaint)
    canvas.drawText(watermarkText, x, y, paint)
    return result
}
suspend fun extractAudioFromVideo(
    context: Context,
    videoUri: Uri,
    videoName: String
): Uri? = withContext(Dispatchers.IO) {
    var extractor: MediaExtractor? = null
    var muxer: MediaMuxer? = null
    try {
        extractor = MediaExtractor()
        extractor.setDataSource(context, videoUri, null)
        var audioTrackIndex = -1
        var format: android.media.MediaFormat? = null
        for (i in 0 until extractor.trackCount) {
            val trackFormat = extractor.getTrackFormat(i)
            val mime = trackFormat.getString(android.media.MediaFormat.KEY_MIME)
            if (mime != null && mime.startsWith("audio/")) {
                audioTrackIndex = i
                format = trackFormat
                break
            }
        }
        if (audioTrackIndex == -1 || format == null) return@withContext null
        extractor.selectTrack(audioTrackIndex)
        val cleanName = videoName.substringBeforeLast(".") + "_audio.m4a"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, cleanName)
            put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/VideoToPhoto")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        val resolver = context.contentResolver
        val audioUri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (audioUri != null) {
            val fileDescriptor = resolver.openFileDescriptor(audioUri, "w")?.fileDescriptor
            if (fileDescriptor != null) {
                muxer = MediaMuxer(fileDescriptor, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                val muxerTrackIndex = muxer.addTrack(format)
                muxer.start()
                val bufferSize = 1024 * 1024
                val buffer = java.nio.ByteBuffer.allocate(bufferSize)
                val bufferInfo = MediaCodec.BufferInfo()
                while (true) {
                    bufferInfo.offset = 0
                    bufferInfo.size = extractor.readSampleData(buffer, 0)
                    if (bufferInfo.size < 0) break
                    bufferInfo.presentationTimeUs = extractor.sampleTime
                    bufferInfo.flags = extractor.sampleFlags
                    muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                    extractor.advance()
                }
                muxer.stop()
                muxer.release()
                muxer = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(audioUri, contentValues, null, null)
                }
                return@withContext audioUri
            }
        }
        null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        try { muxer?.release() } catch (_: Exception) {}
        try { extractor?.release() } catch (_: Exception) {}
    }
}
fun shareAudioFile(context: Context, audioUri: Uri) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/*"
            putExtra(Intent.EXTRA_STREAM, audioUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir Audio"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error al compartir audio", Toast.LENGTH_SHORT).show()
    }
}
suspend fun extractBurstFrames(
    context: Context,
    uri: Uri,
    centerMs: Long,
    durationMs: Long,
): List<ScoredFrame> = withContext(Dispatchers.IO) {
    val retriever = MediaMetadataRetriever()
    val list = mutableListOf<ScoredFrame>()
    try {
        retriever.setDataSource(context, uri)
        val offsets = (-5..4).map { it * 40L }
        for (offset in offsets) {
            val targetMs = (centerMs + offset).coerceIn(0L, durationMs)
            val bitmap = retriever.getFrameAtTime(targetMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST)
            if (bitmap != null && !isBitmapBlackOrEmpty(bitmap)) {
                val score = calculateSharpnessScore(bitmap)
                val faceRes = analyzeFaceAndSmile(bitmap)
                list.add(
                    ScoredFrame(
                        bitmap = bitmap,
                        timestampMs = targetMs,
                        sharpnessScore = score,
                        hasFace = faceRes.hasFace,
                        isSmiling = faceRes.isSmiling,
                        eyesOpen = faceRes.eyesOpen
                    )
                )
            }
        }
        val maxScore = list.maxOfOrNull { it.sharpnessScore } ?: 0f
        list.forEach { frame ->
            if (maxScore > 0 && frame.sharpnessScore >= maxScore * 0.85f) {
                frame.isBestQuality = true
            }
            frame.isSelected.value = true
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        try { retriever.release() } catch (_: Exception) {}
    }
    list
}
data class RecentVideo(
    val uriString: String,
    val name: String,
    val durationMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)
object RecentVideoStore {
    private const val PREFS_NAME = "recent_videos_prefs"
    private const val KEY_RECENT_LIST = "key_recent_list"
    fun getRecentVideos(context: Context): List<RecentVideo> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_RECENT_LIST, null) ?: return emptyList()
        val list = mutableListOf<RecentVideo>()
        try {
            val array = org.json.JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    RecentVideo(
                        uriString = obj.getString("uriString"),
                        name = obj.getString("name"),
                        durationMs = obj.getLong("durationMs"),
                        timestamp = obj.getLong("timestamp")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
    fun addRecentVideo(context: Context, video: RecentVideo) {
        val current = getRecentVideos(context).toMutableList()
        current.removeAll { it.uriString == video.uriString }
        current.add(0, video)
        if (current.size > 8) {
            current.removeAt(current.lastIndex)
        }
        val array = org.json.JSONArray()
        current.forEach { item ->
            val obj = org.json.JSONObject().apply {
                put("uriString", item.uriString)
                put("name", item.name)
                put("durationMs", item.durationMs)
                put("timestamp", item.timestamp)
            }
            array.put(obj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_RECENT_LIST, array.toString())
            .apply()
    }
}
suspend fun exportFramesToZip(
    context: Context,
    frames: List<Bitmap>,
    format: ExportFormat
): Uri? = withContext(Dispatchers.IO) {
    try {
        val zipName = "Capturas_ZIP_${System.currentTimeMillis()}.zip"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, zipName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/zip")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/VideoToPhoto")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        val resolver = context.contentResolver
        val zipUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (zipUri != null) {
            resolver.openOutputStream(zipUri)?.use { outputStream ->
                java.util.zip.ZipOutputStream(java.io.BufferedOutputStream(outputStream)).use { zipOut ->
                    frames.forEachIndexed { index, bitmap ->
                        val entryName = "frame_${index + 1}.${format.extension}"
                        zipOut.putNextEntry(java.util.zip.ZipEntry(entryName))
                        val byteArrayStream = java.io.ByteArrayOutputStream()
                        val compressFormat = if (format == ExportFormat.PNG) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                        bitmap.compress(compressFormat, 100, byteArrayStream)
                        zipOut.write(byteArrayStream.toByteArray())
                        zipOut.closeEntry()
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(zipUri, contentValues, null, null)
            }
            return@withContext zipUri
        }
        null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SideBySideCompareModal(
    frame1: ScoredFrame,
    frame2: ScoredFrame,
    onSelectOne: (ScoredFrame) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0A0F1D)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Text(
                            "🔍 Comparador Lado a Lado",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF101728))
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CompareFrameColumn(
                        modifier = Modifier.weight(1f),
                        frame = frame1,
                        title = "Foto A (${formatTimeMsShort(frame1.timestampMs)})",
                        onChoose = {
                            onSelectOne(frame1)
                            onDismiss()
                        }
                    )
                    CompareFrameColumn(
                        modifier = Modifier.weight(1f),
                        frame = frame2,
                        title = "Foto B (${formatTimeMsShort(frame2.timestampMs)})",
                        onChoose = {
                            onSelectOne(frame2)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}
@Composable
fun CompareFrameColumn(
    modifier: Modifier = Modifier,
    frame: ScoredFrame,
    title: String,
    onChoose: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141C2E)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (frame.isBestQuality) Color(0xFF00E5FF) else Color(0xFF23304A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = frame.bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (frame.isBestQuality) {
                    Text("✦ Nitidez Top", color = Color(0xFF00E5FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                if (frame.isSmiling) {
                    Text("😊 Sonrisa", color = Color.Yellow, fontSize = 10.sp)
                } else if (frame.eyesOpen) {
                    Text("👀 Ojos", color = Color.Green, fontSize = 10.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onChoose,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E68D7)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Elegir esta foto", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
suspend fun exportBitmapsToAnimatedGif(
    context: Context,
    bitmaps: List<Bitmap>,
    delayMs: Int = 120,
    isBoomerang: Boolean = false
): Uri? = withContext(Dispatchers.IO) {
    val validBitmaps = bitmaps.filter { !it.isRecycled && it.width > 0 && it.height > 0 }
    if (validBitmaps.isEmpty()) return@withContext null
    try {
        val frames = if (isBoomerang) {
            validBitmaps + validBitmaps.drop(1).dropLast(1).reversed()
        } else validBitmaps
        val name = "GIF_${if (isBoomerang) "Boomerang_" else ""}${System.currentTimeMillis()}.gif"
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/gif")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/VideoToPhoto")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        uri?.let { destUri ->
            resolver.openOutputStream(destUri)?.use { stream ->
                val encoder = AnimatedGifEncoder()
                encoder.start(stream)
                encoder.setDelay(delayMs)
                frames.forEach { bmp ->
                    if (!bmp.isRecycled) {
                        encoder.addFrame(bmp)
                    }
                }
                encoder.finish()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(destUri, values, null, null)
            }
            destUri
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
fun createPhotoStripCollage(
    bitmaps: List<Bitmap>,
    backgroundColor: Int = android.graphics.Color.BLACK,
    titleText: String = "VIDEO TO PHOTO"
): Bitmap {
    val items = bitmaps.take(6)
    if (items.isEmpty()) return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    val targetWidth = 800
    val targetItemHeight = (targetWidth * 0.75f).toInt()
    val padding = 30
    val headerHeight = 80
    val footerHeight = 100
    val totalHeight = headerHeight + (items.size * targetItemHeight) + ((items.size + 1) * padding) + footerHeight
    val output = Bitmap.createBitmap(targetWidth + (padding * 2), totalHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    canvas.drawColor(backgroundColor)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var currentY = headerHeight + padding
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (backgroundColor == android.graphics.Color.WHITE) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        textSize = 36f
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(titleText, (output.width / 2).toFloat(), headerHeight.toFloat(), titlePaint)
    items.forEach { sourceBmp ->
        val scaled = Bitmap.createScaledBitmap(sourceBmp, targetWidth, targetItemHeight, true)
        canvas.drawBitmap(scaled, padding.toFloat(), currentY.toFloat(), paint)
        currentY += targetItemHeight + padding
    }
    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.GRAY
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("✨ Captured with Video to Photo", (output.width / 2).toFloat(), (totalHeight - padding).toFloat(), footerPaint)
    return output
}
suspend fun detectSceneCuts(
    context: Context,
    uri: Uri,
    durationMs: Long,
    stepMs: Long = 1000L
): List<Long> = withContext(Dispatchers.IO) {
    val retriever = MediaMetadataRetriever()
    val cuts = mutableListOf<Long>()
    try {
        retriever.setDataSource(context, uri)
        var prevBitmap: Bitmap? = null
        val totalSteps = (durationMs / stepMs).toInt().coerceIn(1, 100)
        for (i in 0 until totalSteps) {
            val sampleMs = i * stepMs
            val bmp = retriever.getScaledFrameAtTime(sampleMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, 120, 120)
            if (bmp != null) {
                if (prevBitmap != null) {
                    val diff = calculateBitmapDifference(prevBitmap, bmp)
                    if (diff > 0.35f) {
                        cuts.add(sampleMs)
                    }
                }
                prevBitmap = bmp
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        try { retriever.release() } catch (_: Exception) {}
    }
    cuts
}
fun calculateBitmapDifference(bmp1: Bitmap, bmp2: Bitmap): Float {
    try {
        val w = bmp1.width.coerceAtMost(bmp2.width)
        val h = bmp1.height.coerceAtMost(bmp2.height)
        var totalDiff = 0f
        val step = 12
        var samples = 0
        for (y in 0 until h step step) {
            for (x in 0 until w step step) {
                val p1 = bmp1.getPixel(x, y)
                val p2 = bmp2.getPixel(x, y)
                val rDiff = abs(((p1 shr 16) and 0xFF) - ((p2 shr 16) and 0xFF)) / 255f
                val gDiff = abs(((p1 shr 8) and 0xFF) - ((p2 shr 8) and 0xFF)) / 255f
                val bDiff = abs((p1 and 0xFF) - (p2 and 0xFF)) / 255f
                totalDiff += (rDiff + gDiff + bDiff) / 3f
                samples++
            }
        }
        return if (samples > 0) totalDiff / samples else 0f
    } catch (e: Exception) {
        return 0f
    }
}
suspend fun trimAndExportVideoClip(
    context: Context,
    videoUri: Uri,
    startMs: Long,
    endMs: Long,
    outputName: String = "Clip_${System.currentTimeMillis()}.mp4"
): Uri? = withContext(Dispatchers.IO) {
    var extractor: MediaExtractor? = null
    var muxer: MediaMuxer? = null
    try {
        extractor = MediaExtractor()
        extractor.setDataSource(context, videoUri, null)
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, outputName)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/VideoToPhoto")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        val resolver = context.contentResolver
        val clipUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        if (clipUri != null) {
            val pfd = resolver.openFileDescriptor(clipUri, "w")
            if (pfd != null) {
                muxer = MediaMuxer(pfd.fileDescriptor, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                val trackCount = extractor.trackCount
                val indexMap = HashMap<Int, Int>()
                for (i in 0 until trackCount) {
                    val format = extractor.getTrackFormat(i)
                    val mime = format.getString(android.media.MediaFormat.KEY_MIME) ?: ""
                    if (mime.startsWith("video/") || mime.startsWith("audio/")) {
                        extractor.selectTrack(i)
                        val muxerTrack = muxer.addTrack(format)
                        indexMap[i] = muxerTrack
                    }
                }
                muxer.start()
                val bufferSize = 1024 * 1024
                val buffer = java.nio.ByteBuffer.allocate(bufferSize)
                val bufferInfo = MediaCodec.BufferInfo()
                val startUs = startMs * 1000L
                val endUs = endMs * 1000L
                extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                while (true) {
                    val trackIndex = extractor.sampleTrackIndex
                    if (trackIndex < 0) break
                    val sampleTimeUs = extractor.sampleTime
                    if (sampleTimeUs > endUs) break
                    bufferInfo.offset = 0
                    bufferInfo.size = extractor.readSampleData(buffer, 0)
                    if (bufferInfo.size < 0) break
                    bufferInfo.presentationTimeUs = (sampleTimeUs - startUs).coerceAtLeast(0L)
                    bufferInfo.flags = extractor.sampleFlags
                    val muxerTrackIndex = indexMap[trackIndex]
                    if (muxerTrackIndex != null) {
                        muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                    }
                    extractor.advance()
                }
                muxer.stop()
                muxer.release()
                muxer = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(clipUri, values, null, null)
                }
                return@withContext clipUri
            }
        }
        null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        try { muxer?.release() } catch (_: Exception) {}
        try { extractor?.release() } catch (_: Exception) {}
    }
}
