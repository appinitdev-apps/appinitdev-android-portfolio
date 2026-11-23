class MainActivity : ComponentActivity() {
    var canEnterPip = false
    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                Surface(modifier = Modifier.fillMaxSize()) {
                    VideoFrameCatcherApp(isPipMode)
                }
            }
        }
    }
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (canEnterPip && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val aspectRatio = Rational(16, 9) 
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .build()
            enterPictureInPictureMode(params)
        }
    }
}
@SuppressLint("LocalContextConfigurationRead")
@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoFrameCatcherApp(isPipMode: Boolean) {
    var selectedFormat by rememberSaveable { mutableStateOf(ExportFormat.PNG) }
    var showAutoConfig by rememberSaveable { mutableStateOf(false) }
    var showAutoSelectionScreen by rememberSaveable { mutableStateOf(false) }
    var showGallery by rememberSaveable { mutableStateOf(false) }
    var videoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var currentVideoPosition by rememberSaveable { mutableLongStateOf(0L) }
    var isExtracting by rememberSaveable { mutableStateOf(false) }
    var newlySavedCapture by rememberSaveable { mutableStateOf<CaptureEntity?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    var autoCapturedFrames by remember { mutableStateOf<List<SelectableFrame>>(emptyList()) }
    val cropImageLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uriCortada ->
                scope.launch {
                    val pathFinal = saveImageToInternalStorage(context, uriCortada, selectedFormat)
                    val nuevaCaptura = CaptureEntity(imagePath = pathFinal, videoName = "Video")
                    db.captureDao().insertCapture(nuevaCaptura)
                    Toast.makeText(
                        context,
                        context.getString(R.string.imagen_guardada),
                        Toast.LENGTH_SHORT
                    ).show()
                    newlySavedCapture = nuevaCaptura
                }
            }
        }
    }
    var dynamicFrames by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply { playWhenReady = false }
    }
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                val activity = context as? MainActivity
                activity?.canEnterPip = (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED)
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }
    val videoPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            videoUri = uri
            uri?.let {
                exoPlayer.setMediaItem(MediaItem.fromUri(it))
                exoPlayer.prepare()
                currentVideoPosition = 0L
            }
        }
    LaunchedEffect(videoUri) { 
        if (videoUri != null) {
            isExtracting = true
            dynamicFrames = extractSurroundingFrames(context, videoUri!!, 0L)
            isExtracting = false
            while (true) {
                kotlinx.coroutines.delay(500)
                val isPlaying =
                    exoPlayer.playbackState == ExoPlayer.STATE_READY && exoPlayer.playWhenReady
                if (exoPlayer.playbackState == ExoPlayer.STATE_READY && !isPlaying) {
                    val newPos = exoPlayer.currentPosition
                    if (kotlin.math.abs(newPos - currentVideoPosition) > 500) {
                        currentVideoPosition = newPos
                    }
                }
            }
        }
    }
    LaunchedEffect(currentVideoPosition, videoUri) {
        videoUri?.let { uri ->
            val isPlaying = exoPlayer.playbackState == Player.STATE_READY && exoPlayer.playWhenReady
            if (!isPlaying) {
                isExtracting = true 
                try {
                    dynamicFrames = extractSurroundingFrames(context, uri, currentVideoPosition)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isExtracting = false 
                }
            }
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing 
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (showGallery) PaddingValues(0.dp) else innerPadding)
        ) {
            if (showGallery) {
                BackHandler {
                    showGallery = false
                }
                GalleryScreen(
                    db = db,
                    onBack = { showGallery = false },
                    cropLauncher = cropImageLauncher,
                    selectedFormat = selectedFormat,
                    )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    if(!isPipMode)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp), 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { videoPicker.launch("video/*") },
                            modifier = Modifier.weight(1f), 
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = if (videoUri == null) Icons.Default.PlayArrow else Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (videoUri == null) stringResource(R.string.nuevo_video) else stringResource(
                                    R.string.video
                                ),
                                maxLines = 1,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.ExtraBold, 
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                        if (videoUri != null) {
                            Button(
                                onClick = { showAutoConfig = true },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF6200EE
                                    )
                                )
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    stringResource(R.string.automatico),
                                    maxLines = 1,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.ExtraBold, 
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                        }
                        Button(
                            onClick = { showGallery = true },
                            modifier = Modifier.weight(1f), 
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBox, 
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.galer_a), 
                                maxLines = 1,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.ExtraBold, 
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                    if (videoUri != null) {
                        Box(modifier = Modifier.weight(1f)) {
                            AndroidView(
                                factory = { ctx ->
                                    PlayerView(ctx).apply {
                                        player = exoPlayer
                                        useController = true
                                        val controlsBackground =
                                            findViewById<android.view.View>(androidx.media3.ui.R.id.exo_controls_background)
                                        controlsBackground?.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                        val orientation =
                                            context.resources.configuration.orientation
                                        if (orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
                                            val bottomBar =
                                                findViewById<android.view.View>(androidx.media3.ui.R.id.exo_bottom_bar)
                                            bottomBar?.let {
                                                it.translationY = 60f
                                                it.scaleX = 0.9f
                                                it.scaleY = 0.8f
                                            }
                                            val progress =
                                                findViewById<android.view.View>(androidx.media3.ui.R.id.exo_progress)
                                            progress?.let {
                                                it.scaleY = 2.5f
                                                it.translationY = 20f
                                            }
                                            val centerControls =
                                                findViewById<android.view.View>(androidx.media3.ui.R.id.exo_center_controls)
                                            centerControls?.let {
                                                it.translationY = 250f
                                                it.scaleX = 0.8f
                                                it.scaleY = 0.8f
                                            }
                                        } else {
                                            val progress =
                                                findViewById<android.view.View>(androidx.media3.ui.R.id.exo_progress)
                                            progress?.let {
                                                it.scaleY = 2.5f
                                            }
                                        }
                                        setShowNextButton(false)
                                        setShowPreviousButton(false)
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        if(!isPipMode)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        stringResource(R.string.toca_un_fotograma_para_guardar_o_recortar),
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row(
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.surface,
                                                RoundedCornerShape(16.dp)
                                            )
                                            .padding(4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        ExportFormat.entries.forEach { format ->
                                            val isSelected = selectedFormat == format
                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                                    .clickable { selectedFormat = format }
                                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = format.name,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val listState = rememberLazyListState()
                                    LaunchedEffect(dynamicFrames) {
                                        if (dynamicFrames.isNotEmpty()) {
                                            val centerIndex = dynamicFrames.size / 2
                                            listState.scrollToItem(centerIndex)
                                        }
                                    }
                                    LazyRow(
                                        state = listState,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxSize(),
                                    ) {
                                        items(dynamicFrames) { bitmap ->
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = "Fotograma extraído",
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .aspectRatio(bitmap.width.toFloat() / bitmap.height.toFloat())
                                                    .clickable(enabled = !isExtracting) { 
                                                        exoPlayer.pause()
                                                        launchCropper(
                                                            context,
                                                            bitmap,
                                                            cropImageLauncher,
                                                            selectedFormat
                                                        )
                                                    },
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                    }
                                    if (isExtracting) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.primary,
                                            strokeWidth = 4.dp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(24.dp), 
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logoc),
                                    contentDescription = "Logo de bienvenida",
                                )
                                Text(
                                    text = stringResource(R.string.bienvenido),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = stringResource(R.string.selecciona_un_video_de_tu_galer_a_para_extraer_los_mejores_fotogramas),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.LightGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(0.8f) 
                                )
                            }
                        }
                    }
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
                        onShare = { shareImage(context, File(capture.imagePath)) },
                        onRecrop = {
                            val bitmap =
                                android.graphics.BitmapFactory.decodeFile(capture.imagePath)
                            if (bitmap != null) {
                                newlySavedCapture = null
                                launchCropper(context, bitmap, cropImageLauncher, selectedFormat)
                            }
                        },
                        selectedFormat = selectedFormat,
                        context = context
                    )
                }
            }
            if (showAutoSelectionScreen) {
                BackHandler { showAutoSelectionScreen = false }
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    AutoSelectionInterface(
                        frames = autoCapturedFrames,
                        onSave = { selectedFrames ->
                            scope.launch {
                                isExtracting = true
                                selectedFrames.forEach { frame ->
                                    val tempUri =
                                        saveBitmapToTempUri(context, frame.bitmap, selectedFormat)
                                    val pathFinal =
                                        saveImageToInternalStorage(context, tempUri, selectedFormat)
                                    db.captureDao().insertCapture(
                                        CaptureEntity(
                                            imagePath = pathFinal,
                                            videoName = "Auto Capture"
                                        )
                                    )
                                }
                                isExtracting = false
                                showAutoSelectionScreen =
                                    false 
                                showGallery =
                                    true              
                            }
                        },
                        onClose = { showAutoSelectionScreen = false },
                        selectedFormat = selectedFormat,
                        isSaving = isExtracting
                    )
                }
            }
            val videoDurationSec = remember(videoUri) {
                if (videoUri != null) {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(context, videoUri)
                    val time =
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                            ?.toLong() ?: 0L
                    retriever.release()
                    time / 1000 
                } else 0L
            }
            if (showAutoConfig) {
                AutoCaptureConfigDialog(
                    videoDuration = videoDurationSec,
                    onDismiss = { showAutoConfig = false },
                    onConfirm = { num, interval ->
                        showAutoConfig = false
                        scope.launch {
                            isExtracting = true
                            autoCapturedFrames =
                                runAutoExtraction(context, videoUri!!, num, interval)
                            isExtracting = false
                            showAutoSelectionScreen = true 
                        }
                    }
                )
            }
            if (isExtracting && !showAutoSelectionScreen) {
                val infiniteTransition = rememberInfiniteTransition(label = "blink")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.2f, 
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000), 
                        repeatMode = RepeatMode.Reverse 
                    ),
                    label = "alpha"
                )
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(
                                color = Color.Black.copy(alpha = 0.6f), 
                                shape = RoundedCornerShape(24.dp)      
                            )
                            .padding(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logoc), 
                            contentDescription = stringResource(R.string.cargando),
                            modifier = Modifier
                                .graphicsLayer(alpha = alpha) 
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.extrayendo_fotos),
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
suspend fun extractSurroundingFrames(context: Context, uri: Uri, centerMs: Long): List<Bitmap> {
    return withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        val list = mutableListOf<Bitmap>()
        try {
            retriever.setDataSource(context, uri)
            for (i in -2..2) {
                val timeUs = (centerMs + (i * 200)).coerceAtLeast(0) * 1000
                retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)?.let {
                    list.add(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }
        list
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
    val captures by db.captureDao().getAllCaptures().collectAsState(initial = emptyList())
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedCaptures = remember { mutableStateListOf<CaptureEntity>() }
    var selectedCaptureDetail by remember { mutableStateOf<CaptureEntity?>(null) }
    val groupedCaptures = remember(captures) {
        captures.groupBy { getRelativeDateString(it.timestamp, context) }
    }
    BackHandler(enabled = selectedCaptureDetail != null) {
        selectedCaptureDetail = null 
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isSelectionMode) "${selectedCaptures.size} " + stringResource(R.string.seleccionados)
                        else stringResource(R.string.mis_capturas),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isSelectionMode) {
                            isSelectionMode = false
                            selectedCaptures.clear()
                        } else onBack()
                    }) {
                        Icon(
                            imageVector = if (isSelectionMode) Icons.Default.Refresh else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.volver)
                        )
                    }
                },
                actions = {
                    if (isSelectionMode && selectedCaptures.isNotEmpty()) {
                        IconButton(onClick = {
                            scope.launch {
                                var successCount = 0
                                selectedCaptures.forEach { capture ->
                                    val bitmap =
                                        android.graphics.BitmapFactory.decodeFile(capture.imagePath)
                                    if (bitmap != null) {
                                        val uri =
                                            saveToPublicGallery(context, bitmap, selectedFormat)
                                        if (uri != null) successCount++
                                    }
                                }
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.fotos_guardadas_en_galer_a),
                                    Toast.LENGTH_SHORT
                                ).show()
                                isSelectionMode = false
                                selectedCaptures.clear()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = stringResource(R.string.descargar_seleccionados),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) 
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (captures.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.a_n_no_has_guardado_ninguna_captura),
                        color = Color.Gray
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(
                        start = 2.dp,
                        top = 2.dp,
                        end = 2.dp,
                        bottom = 100.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                ) {
                    groupedCaptures.forEach { (dateHeader, capturesInDate) ->
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = dateHeader,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(
                                    start = 8.dp,
                                    top = 16.dp,
                                    bottom = 8.dp
                                )
                            )
                        }
                        items(capturesInDate) { capture ->
                            val isSelected = selectedCaptures.contains(capture)
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .combinedClickable(
                                        onClick = {
                                            if (isSelectionMode) {
                                                if (isSelected) selectedCaptures.remove(capture)
                                                else selectedCaptures.add(capture)
                                                if (selectedCaptures.isEmpty()) isSelectionMode =
                                                    false
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
                                AsyncImage(
                                    model = File(capture.imagePath),
                                    contentDescription = "Captura",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    alpha = if (isSelected) 0.5f else 1f
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .background(Color.White, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }
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
                    onShare = { shareImage(context, File(capture.imagePath)) },
                    onRecrop = {
                        val bitmap = android.graphics.BitmapFactory.decodeFile(capture.imagePath)
                        if (bitmap != null) {
                            selectedCaptureDetail = null
                            launchCropper(context, bitmap, cropLauncher, selectedFormat)
                        }
                    },
                    selectedFormat = selectedFormat,
                    context = context
                )
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
) {
    val zoomableState = rememberZoomableState()
    BackHandler {
        onDismiss()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .clickable { onDismiss() }
    ) {
        Zoomable(
            state = zoomableState,
            modifier = Modifier.fillMaxSize(),
        ) {
            AsyncImage(
                model = File(capture.imagePath),
                contentDescription = "Detalle",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        TopAppBar(
            modifier = Modifier.fillMaxWidth(),
            title = { },
            navigationIcon = {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black.copy(alpha = 0.4f) 
            )
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            IconButton(onClick = onShare) {
                Icon(Icons.Default.Share, contentDescription = "Compartir", tint = Color.White)
            }
            IconButton(onClick = onRecrop) {
                Icon(
                    Icons.Default.Create,
                    contentDescription = "Recortar de nuevo",
                    tint = Color.White
                )
            }
            val scope = rememberCoroutineScope()
            IconButton(onClick = {
                scope.launch {
                    val bitmap = android.graphics.BitmapFactory.decodeFile(capture.imagePath)
                    if (bitmap != null) {
                        val uri = saveToPublicGallery(context, bitmap, selectedFormat)
                        if (uri != null) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.guardado_en_galeria),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Descargar",
                    tint = Color.White
                )
            }
        }
    }
}
fun getRelativeDateString(timestamp: Long, context: Context): String {
    val now = Calendar.getInstance()
    val time = Calendar.getInstance().apply { timeInMillis = timestamp }
    return when {
        isSameDay(now, time) -> context.getString(R.string.hoy)
        isYesterday(now, time) -> context.getString(R.string.ayer)
        else -> SimpleDateFormat("d 'de' MMMM, yyyy", Locale("es", "ES")).format(Date(timestamp))
    }
}
fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
fun isYesterday(now: Calendar, then: Calendar): Boolean {
    val yesterday = now.clone() as Calendar
    yesterday.add(Calendar.DAY_OF_YEAR, -1)
    return isSameDay(yesterday, then)
}
fun shareImage(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val mimeType = if (file.extension.lowercase() == "png") "image/png" else "image/jpeg"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType 
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(
                intent,
                context.getString(R.string.compartir_captura_con)
            )
        )
    } catch (e: Exception) {
        Toast.makeText(
            context,
            context.getString(R.string.error_al_compartir_la_imagen), Toast.LENGTH_SHORT
        ).show()
        Log.e("ShareError", "Error al compartir: ${e.message}")
    }
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
                if (format == ExportFormat.PNG) {
                    val bitmap = android.graphics.BitmapFactory.decodeStream(input)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                } else {
                    input.copyTo(output)
                }
            }
        }
        file.absolutePath
    }
}
data class SelectableFrame(
    val bitmap: Bitmap,
    var isSelected: MutableState<Boolean> = mutableStateOf(false),
)
fun launchCropper(
    context: Context,
    bitmap: Bitmap,
    launcher: ActivityResultLauncher<CropImageContractOptions>,
    format: ExportFormat, 
) {
    val tempFile = File(context.cacheDir, "temp_capture.${format.extension}")
    tempFile.outputStream().use { out ->
        val compressFormat = if (format == ExportFormat.PNG) {
            Bitmap.CompressFormat.PNG
        } else {
            Bitmap.CompressFormat.JPEG
        }
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
        outputCompressFormat = if (format == ExportFormat.PNG) {
            Bitmap.CompressFormat.PNG
        } else {
            Bitmap.CompressFormat.JPEG
        }
        outputCompressQuality = 100
    })
    launcher.launch(options)
}
suspend fun runAutoExtraction(
    context: Context,
    uri: Uri,
    count: Int,
    intervalSec: Long,
): List<SelectableFrame> = withContext(Dispatchers.IO) {
    val retriever = MediaMetadataRetriever()
    val frames = mutableListOf<SelectableFrame>()
    try {
        retriever.setDataSource(context, uri)
        for (i in 0 until count) {
            val timeUs = i * intervalSec * 1_000_000L
            val bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)
            if (bitmap != null) {
                frames.add(SelectableFrame(bitmap, mutableStateOf(false)))
            }
        }
    } catch (e: Exception) {
        Log.e("Capture", "Error extrayendo: ${e.message}")
    } finally {
        retriever.release()
    }
    frames
}
suspend fun saveBitmapToTempUri(
    context: Context,
    bitmap: Bitmap,
    format: ExportFormat, 
): Uri {
    return withContext(Dispatchers.IO) {
        val extension = format.extension
        val tempFile =
            File(context.cacheDir, "auto_capture_${System.currentTimeMillis()}.$extension")
        try {
            tempFile.outputStream().use { out ->
                val compressFormat = if (format == ExportFormat.PNG) {
                    Bitmap.CompressFormat.PNG
                } else {
                    Bitmap.CompressFormat.JPEG
                }
                bitmap.compress(compressFormat, 100, out)
                out.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Uri.fromFile(tempFile)
    }
}
@Composable
fun AutoCaptureConfigDialog(
    videoDuration: Long,
    onDismiss: () -> Unit,
    onConfirm: (numFrames: Int, intervalSec: Long) -> Unit,
) {
    var numFrames by remember { mutableStateOf("5") }
    var intervalSec by remember { mutableStateOf("1") }
    val n = numFrames.toIntOrNull() ?: 0
    val i = intervalSec.toLongOrNull() ?: 0
    val maxPhotosPossible = if (i > 0) (videoDuration / i).toInt().coerceAtMost(100) else 0
    val isIntervalError = i <= 0 || i > videoDuration
    val isNumError = n <= 0 || n > 100 || n > maxPhotosPossible
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.configurar_captura_autom_tica)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.video_) + " $videoDuration " + stringResource(R.string.segundos_m_ximo_posible) + " $maxPhotosPossible " + stringResource(
                        R.string.fotos
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                OutlinedTextField(
                    value = intervalSec,
                    onValueChange = { newValue ->
                        if (newValue.all { c -> c.isDigit() } && newValue.length < 6) {
                            intervalSec = newValue
                            val newInterval = newValue.toLongOrNull() ?: 0
                            if (newInterval > 0) {
                                val newMax = (videoDuration / newInterval).toInt().coerceAtMost(100)
                                numFrames = newMax.toString()
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.cada_cu_ntos_segundos)) },
                    isError = isIntervalError,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text(stringResource(R.string.segundos)) }
                )
                OutlinedTextField(
                    value = numFrames,
                    onValueChange = { newValue ->
                        if (newValue.all { c -> c.isDigit() } && newValue.length < 4) {
                            val newValueInt = newValue.toIntOrNull() ?: 0
                            if (newValueInt <= maxPhotosPossible || newValue.isEmpty()) {
                                numFrames = newValue
                            } else {
                                numFrames = maxPhotosPossible.toString()
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.cu_ntas_fotogramas)) },
                    isError = isNumError,
                    supportingText = {
                        if (n > 0 && i > 0) {
                            Text(
                                stringResource(R.string.extraer_fotos_hasta_el_segundo) + " ${n * i} " + stringResource(
                                    R.string.de
                                ) + " $videoDuration"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(n, i) },
                enabled = !isNumError && !isIntervalError && n > 0
            ) {
                Text(stringResource(R.string.empezar))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancelar)) }
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoSelectionInterface(
    frames: List<SelectableFrame>,
    onSave: (List<SelectableFrame>) -> Unit,
    isSaving: Boolean,
    onClose: () -> Unit,
    selectedFormat: ExportFormat,
) {
    val context = LocalContext.current
    var frameInDetail by remember { mutableStateOf<SelectableFrame?>(null) }
    BackHandler(enabled = frameInDetail != null) {
        frameInDetail = null
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.seleccionar_fotogramas),
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onClose, enabled = !isSaving) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cerrar")
                        }
                    },
                    actions = {
                        val selectedCount = frames.count { it.isSelected.value }
                        TextButton(
                            onClick = { onSave(frames.filter { it.isSelected.value }) },
                            enabled = selectedCount > 0 && !isSaving
                        ) {
                            Text(
                                text = "${stringResource(R.string.guardar)} ($selectedCount)",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedCount > 0 && !isSaving)
                                    MaterialTheme.colorScheme.primary
                                else Color.Gray
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (frames.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.no_se_pudieron_extraer_fotogramas))
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 8.dp,
                            top = 8.dp,
                            end = 8.dp,
                            bottom = 16.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(frames) { frame ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable(enabled = !isSaving) { frameInDetail = frame }
                            ) {
                                Image(
                                    bitmap = frame.bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .background(Color.Black.copy(alpha = 0.2f), CircleShape)
                                ) {
                                    Checkbox(
                                        checked = frame.isSelected.value,
                                        onCheckedChange = {
                                            if (!isSaving) frame.isSelected.value = it
                                        },
                                        enabled = !isSaving
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = frameInDetail != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            frameInDetail?.let { selected ->
                FullScreenFrameViewer(
                    frame = selected,
                    onDismiss = { frameInDetail = null },
                    onShare = {
                        val uri = saveBitmapToUriSync(context, selected.bitmap, selectedFormat)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/*"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(
                            Intent.createChooser(
                                intent,
                                context.getString(R.string.compartir)
                            )
                        )
                    },
                    selectedFormat = selectedFormat,
                    context = context
                )
            }
        }
        if (isSaving) {
            LoadingOverlay()
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenFrameViewer(
    frame: SelectableFrame,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    selectedFormat: ExportFormat,
    context: Context,
) {
    val context = LocalContext.current
    val zoomableState = rememberZoomableState()
    val cropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            onDismiss()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Zoomable(state = zoomableState) {
            Image(
                bitmap = frame.bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            IconButton(onClick = onShare) {
                Icon(Icons.Default.Share, "Compartir", tint = Color.White)
            }
            IconButton(onClick = {
                launchCropper(context, frame.bitmap, cropLauncher, selectedFormat)
            }) {
                Icon(Icons.Default.Create, "Recortar", tint = Color.White)
            }
            val scope = rememberCoroutineScope()
            IconButton(onClick = {
                scope.launch {
                    val uri = saveToPublicGallery(context, frame.bitmap, selectedFormat)
                    if (uri != null) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.guardado_en_galeria),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Descargar a Galería",
                    tint = Color.White
                )
            }
        }
    }
}
suspend fun saveToPublicGallery(
    context: Context,
    bitmap: Bitmap,
    format: ExportFormat,
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
            bitmap.compress(compressFormat, 100, output)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(targetUri, contentValues, null, null)
        }
        targetUri
    }
}
fun saveBitmapToUriSync(
    context: Context,
    bitmap: Bitmap,
    format: ExportFormat, 
): Uri {
    val extension = format.extension
    val file = File(context.cacheDir, "share_temp.$extension")
    file.outputStream().use { out ->
        val compressFormat = if (format == ExportFormat.PNG) {
            Bitmap.CompressFormat.PNG
        } else {
            Bitmap.CompressFormat.JPEG
        }
        bitmap.compress(compressFormat, 100, out)
    }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
enum class ExportFormat(val extension: String) {
    PNG("png"),
    JPG("jpg"),
}
@Composable
fun LoadingOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.logoc),
                contentDescription = null,
                modifier = Modifier.graphicsLayer(alpha = alpha)
            )
            Text("Guardando...", color = Color.White)
        }
    }
}