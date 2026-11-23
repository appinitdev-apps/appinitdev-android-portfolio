

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
            val aspectRatio = Rational(16, 9) // Ajusta según tu video
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
    // Estado para alternar entre el reproductor y la galería
    val cropImageLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uriCortada ->
                scope.launch {
                    // ANTES: saveImageToInternalStorage(context, uriCortada)
                    // AHORA: Agregamos selectedFormat
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
                // Obtenemos la Activity y actualizamos su variable
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


    LaunchedEffect(videoUri) { // Escuchamos cuando el video cambia
        if (videoUri != null) {
            // Extraemos los primeros frames de inmediato
            isExtracting = true
            dynamicFrames = extractSurroundingFrames(context, videoUri!!, 0L)
            isExtracting = false


            // Luego iniciamos el bucle de monitoreo
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

    // 2. Extraer fotogramas automáticamente cuando la posición cambia
    LaunchedEffect(currentVideoPosition, videoUri) {
        videoUri?.let { uri ->
            val isPlaying = exoPlayer.playbackState == Player.STATE_READY && exoPlayer.playWhenReady
            if (!isPlaying) {
                isExtracting = true // 1. Empezamos a cargar
                try {
                    dynamicFrames = extractSurroundingFrames(context, uri, currentVideoPosition)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isExtracting = false // 2. Terminamos (con éxito o error)
                }
            }
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing // Esto reserva el espacio de las barras
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (showGallery) PaddingValues(0.dp) else innerPadding)
        ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.Black)
//                .safeDrawingPadding()
//        ) {
//            // Tu Row de botones y el AndroidView del video...
//        }


            // ------------------------------------------------------------------
            // 1. CAPA BASE: Galería o Reproductor de Video (VA PRIMERO)
            // ------------------------------------------------------------------
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

                    // BOTONERA SUPERIOR
                    if(!isPipMode)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp), // Espacio uniforme entre botones
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón Principal: Cargar/Cambiar Video
                        Button(
                            onClick = { videoPicker.launch("video/*") },
                            modifier = Modifier.weight(1f), // Hace que ambos botones midan lo mismo
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
                                    fontWeight = FontWeight.ExtraBold, // Peso extra para máxima fuerza
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
                                        fontWeight = FontWeight.ExtraBold, // Peso extra para máxima fuerza
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                        }
                        // Botón Secundario: Ver Galería (Usamos OutlinedButton para jerarquía)
                        Button(
                            onClick = { showGallery = true },
                            modifier = Modifier.weight(1f), // Hace que ambos botones midan lo mismo
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBox, // Cambié el icono a uno de galería real
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.galer_a), // En mayúsculas resalta más
                                maxLines = 1,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.ExtraBold, // Peso extra para máxima fuerza
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }

                    if (videoUri != null) {
                        // ÁREA DEL VIDEO (Toma el espacio restante)
                        Box(modifier = Modifier.weight(1f)) {
                            AndroidView(
                                factory = { ctx ->
                                    PlayerView(ctx).apply {
                                        player = exoPlayer
                                        useController = true
                                        // 1. Quitar el oscurecimiento (dimming) del fondo
                                        val controlsBackground =
                                            findViewById<android.view.View>(androidx.media3.ui.R.id.exo_controls_background)
                                        controlsBackground?.setBackgroundColor(android.graphics.Color.TRANSPARENT)

                                        // 2. Hacer la barra inferior más pequeña y ajustarla
                                        val orientation =
                                            context.resources.configuration.orientation
                                        if (orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
                                            val bottomBar =
                                                findViewById<android.view.View>(androidx.media3.ui.R.id.exo_bottom_bar)
                                            bottomBar?.let {
                                                // Bajamos la barra un poco más (60f en lugar de 40f)
                                                it.translationY = 60f

                                                // Si quieres que sea más ancha, sube este valor (1.0f es el ancho original)
                                                // Al quitar el 0.75f que tenías antes, recuperará su ancho natural
                                                it.scaleX = 0.9f
                                                it.scaleY = 0.8f
                                            }
                                            val progress =
                                                findViewById<android.view.View>(androidx.media3.ui.R.id.exo_progress)
                                            progress?.let {
                                                // Esto hace que la línea de tiempo sea un poco más gruesa y fácil de tocar
                                                it.scaleY = 2.5f
                                                it.translationY = 20f
                                            }

                                            // --- NUEVOS AJUSTES ---

                                            // 3. Ocultar botones de Siguiente y Anterior permanentemente


                                            // 4. Bajar los botones centrales (Play, Adelantar, Retroceder)
                                            val centerControls =
                                                findViewById<android.view.View>(androidx.media3.ui.R.id.exo_center_controls)
                                            centerControls?.let {
                                                // Empuja los botones hacia abajo.
                                                // Aumenta este valor (ej. 300f, 400f) si los quieres aún más pegados a la barra de progreso
                                                it.translationY = 250f

                                                // Opcional: También puedes achicarlos un poco para que no estorben en la imagen
                                                it.scaleX = 0.8f
                                                it.scaleY = 0.8f
                                            }
                                        } else {
                                            val progress =
                                                findViewById<android.view.View>(androidx.media3.ui.R.id.exo_progress)

                                            progress?.let {
                                                // Esto hace que la línea de tiempo sea un poco más gruesa y fácil de tocar
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

                        // CARRIL INFERIOR DE FOTOGRAMAS DINÁMICOS
                        // CARRIL INFERIOR DE FOTOGRAMAS DINÁMICOS
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

                                    // Selector de Formato (JPG / PNG)
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

                                // Usamos Box para que el indicador de carga flote sobre la lista
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val listState = rememberLazyListState()
                                    LaunchedEffect(dynamicFrames) {
                                        if (dynamicFrames.isNotEmpty()) {
                                            // El índice 2 es el centro de tus 5 fotos (-2, -1, 0, 1, 2)
                                            val centerIndex = dynamicFrames.size / 2
                                            listState.scrollToItem(centerIndex)
                                        }
                                    }
                                    LazyRow(
                                        state = listState,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxSize(),
                                        // Si está cargando, podemos bajar la opacidad de la lista para que se note el cambio
                                        // alpha = if (isExtracting) 0.5f else 1f
                                    ) {
                                        items(dynamicFrames) { bitmap ->
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = "Fotograma extraído",
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .aspectRatio(bitmap.width.toFloat() / bitmap.height.toFloat())
                                                    .clickable(enabled = !isExtracting) { // Deshabilitar clics mientras carga
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

                                    // MOSTRAR INDICADOR SI ESTÁ CARGANDO
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
                                .padding(24.dp), // Espaciado para que no toque los bordes
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Icono representativo (Imagen)
                                Image(
                                    // REEMPLAZA 'ic_launcher_foreground' POR EL NOMBRE DE TU IMAGEN
                                    painter = painterResource(id = R.drawable.logoc),
                                    contentDescription = "Logo de bienvenida",
//                                modifier = Modifier
//                                    .size(200.dp) // Tamaño grande para que sea el protagonista
//                                    , // Espacio con el texto
                                    // Opcional: Si tu imagen es negra y quieres que se vea blanca
                                    // colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.7f))
                                )

                                // Título Principal
                                Text(
                                    text = stringResource(R.string.bienvenido),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                // Subtítulo con instrucciones
                                Text(
                                    text = stringResource(R.string.selecciona_un_video_de_tu_galer_a_para_extraer_los_mejores_fotogramas),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.LightGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(0.8f) // Limita el ancho para que el texto no sea muy largo
                                )
                            }
                        }
                    }
                }
            }

            // ------------------------------------------------------------------
            // 2. CAPAS SUPERIORES / EMERGENTES (VAN AL FINAL DEL BOX)
            // ------------------------------------------------------------------
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

                                // --- CÓDIGO NUEVO AQUÍ ---
                                showAutoSelectionScreen =
                                    false // 1. Cierra la pantalla de selección
                                showGallery =
                                    true              // 2. Abre la galería automáticamente

                            }
                        },
                        onClose = { showAutoSelectionScreen = false },
                        selectedFormat = selectedFormat,
                        isSaving = isExtracting
                    )
                }
            }

            // El diálogo de configuración
            val videoDurationSec = remember(videoUri) {
                if (videoUri != null) {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(context, videoUri)
                    val time =
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                            ?.toLong() ?: 0L
                    retriever.release()
                    time / 1000 // Convertimos milisegundos a segundos
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
                            showAutoSelectionScreen = true // Ahora sí se verá al cambiar a true
                        }
                    }
                )
            }

            // Indicador global (Opcional, flotará en el centro de toda la pantalla)
            if (isExtracting && !showAutoSelectionScreen) {
                // Solo mostramos el global si no estamos en la pantalla de selección,
                // ya que el carril inferior tiene su propio loading.
                val infiniteTransition = rememberInfiniteTransition(label = "blink")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.2f, // Baja hasta un 20% de visibilidad
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000), // Duración de 1 segundo
                        repeatMode = RepeatMode.Reverse // Va y viene (parpadeo suave)
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
                            // 1. Definimos el fondo con color semitransparente y bordes redondeados
                            .background(
                                color = Color.Black.copy(alpha = 0.6f), // Negro con 60% de opacidad
                                shape = RoundedCornerShape(24.dp)      // Esquinas redondeadas
                            )
                            // 2. Agregamos un relleno (padding) para que el contenido no toque los bordes del fondo
                            .padding(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logoc), // Usa tu icono de drawable
                            contentDescription = stringResource(R.string.cargando),
                            modifier = Modifier
                                .graphicsLayer(alpha = alpha) // Aquí aplicamos el parpadeo
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


// LÓGICA DE EXTRACCIÓN (5 antes, 5 después)
suspend fun extractSurroundingFrames(context: Context, uri: Uri, centerMs: Long): List<Bitmap> {
    return withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        val list = mutableListOf<Bitmap>()
        try {
            retriever.setDataSource(context, uri)
            // Intervalo de 100ms entre frames para ver diferencia real
            for (i in -2..2) {
                // Calculamos el tiempo en microsegundos (ms * 1000)
                val timeUs = (centerMs + (i * 200)).coerceAtLeast(0) * 1000

                // OPTION_CLOSEST es más lento pero obtiene el frame EXACTO
                retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)?.let {
                    list.add(it)
                }
            }
//            for (i in -3..3) {
//                val time = (centerMs + (i * 500)).coerceAtLeast(0)
//                retriever.getFrameAtTime(time * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
//                    ?.let {
//                        list.add(it)
//                    }
//
//            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }
        list
    }
}

/*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GalleryScreen(
    db: AppDatabase,
    onBack: () -> Unit,
    cropLauncher: ActivityResultLauncher<CropImageContractOptions>,
    selectedFormat: ExportFormat

) {
    val context = LocalContext.current
    val captures by db.captureDao().getAllCaptures().collectAsState(initial = emptyList())

    // Estado para controlar qué imagen se ve en pantalla completa
    var selectedCapture by remember { mutableStateOf<CaptureEntity?>(null) }

    // Lógica para agrupar capturas por fecha (Ej: "Hoy", "Ayer", "15 de Marzo, 2024")
    val groupedCaptures = remember(captures) {
        captures.groupBy { capture ->
            getRelativeDateString(capture.timestamp, context)
        }
    }



        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.mis_capturas), style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.volver)
                            )
                        }
                    },
                    // Dejamos que el TopAppBar maneje sus propios insets
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        ) { innerPadding ->// Este es el padding interno de la Galería

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {


            if (captures.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.a_n_no_has_guardado_ninguna_captura), color = Color.Gray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Iteramos sobre los grupos de fechas
                    groupedCaptures.forEach { (dateHeader, capturesInDate) ->
                        // 1. Cabecera de fecha (ocupa las 3 columnas)
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

                        // 2. Cuadrícula de imágenes para esa fecha
                        items(capturesInDate) { capture ->
                            val file = File(capture.imagePath)
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { selectedCapture = capture } // Abrir detalle
                            ) {
                                AsyncImage(
                                    model = file,
                                    contentDescription = "Captura",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop // Look Google Photos
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- VISTA DETALLE PANTALLA COMPLETA (Encimada) ---
        AnimatedVisibility(
            visible = selectedCapture != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            selectedCapture?.let { capture ->
                FullScreenDetail(
                    capture = capture,
                    onDismiss = {
                        selectedCapture = null
                    }, // Esto ahora se activa con la flecha y el botón atrás
                    onShare = { shareImage(context, File(capture.imagePath)) },
                    onRecrop = {
                        val bitmap = android.graphics.BitmapFactory.decodeFile(capture.imagePath)
                        if (bitmap != null) {
                            selectedCapture = null
                            launchCropper(context, bitmap, cropLauncher, selectedFormat)
                        }
                    },
                    selectedFormat = selectedFormat,
                    context=context
                )
            }
        }


    }
}
*/
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

    // --- ESTADOS PARA SELECCIÓN MÚLTIPLE ---
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedCaptures = remember { mutableStateListOf<CaptureEntity>() }

    // Estado para ver una imagen individual en pantalla completa
    var selectedCaptureDetail by remember { mutableStateOf<CaptureEntity?>(null) }

    // Agrupación por fecha
    val groupedCaptures = remember(captures) {
        captures.groupBy { getRelativeDateString(it.timestamp, context) }
    }

    // Manejo del botón atrás físico para cancelar la selección
    BackHandler(enabled = selectedCaptureDetail != null) {
        selectedCaptureDetail = null // Al dar atrás, cerramos el detalle y volvemos a la rejilla
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
                .padding(innerPadding) // Evita que la primera fila se corte por la TopBar
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
                    // Padding inferior extra (100.dp) para que la última fila no se corte por la barra de Android
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

        // --- VISTA DETALLE PANTALLA COMPLETA ---
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
    // 1. Estado para controlar el zoom (habilitar/deshabilitar según sea necesario)
    val zoomableState = rememberZoomableState()

    // 2. Maneja el botón físico "Atrás" del dispositivo (Ya lo tenías)
    BackHandler {
        onDismiss()
    }

    // Contenedor principal negro
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.navigationBars)
            // 3. Mover el clic para cerrar aquí. Si haces clic FUERA de la imagen, se cierra.
            // Si haces clic EN la imagen, Zoomable consume el gesto si está ampliada.
            .clickable { onDismiss() }
    ) {

        // 4. EL COMPONENTE CLAVE: Zoomable envuelve la imagen
        Zoomable(
            state = zoomableState,
            modifier = Modifier.fillMaxSize(),
            // Opcional: Deshabilitar el gesto de clic de fondo cuando está ampliada
            // onTap = { onDismiss() } // Si quieres que un toque en la imagen también cierre
        ) {
            AsyncImage(
                model = File(capture.imagePath),
                contentDescription = "Detalle",
                modifier = Modifier.fillMaxSize(),
                // Importante: Fit asegura que la imagen inicie viéndose completa
                contentScale = ContentScale.Fit
            )
        }

        // --- BARRA SUPERIOR (Mismo código, pero tintado en blanco para contraste) ---
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
                containerColor = Color.Black.copy(alpha = 0.4f) // Sombreado sutil
            )
        )

        // --- BOTONERA INFERIOR (Mismo código, tintado en blanco) ---
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

// Funciones de Ayuda (Helpers)

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

// Función para compartir imagen usando FileProvider (Nativo Android)
fun shareImage(context: Context, file: File) {
    try {
        // 1. Obtenemos la URI segura
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        // 2. Detectamos el tipo de MIME según la extensión del archivo real
        val mimeType = if (file.extension.lowercase() == "png") "image/png" else "image/jpeg"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType // Ahora es dinámico
            putExtra(Intent.EXTRA_STREAM, uri)
            // Permiso crítico para que la app receptora pueda leer fuera de tu carpeta privada
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // 3. Lanzamos el selector de aplicaciones
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
    format: ExportFormat, // Añade este parámetro
): String {
    return withContext(Dispatchers.IO) {
        val extension = format.extension
        val filename = "Capture_${System.currentTimeMillis()}.$extension"
        val file = File(context.filesDir, filename)

        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                if (format == ExportFormat.PNG) {
                    // Si es PNG, lo decodificamos y comprimimos sin pérdida
                    val bitmap = android.graphics.BitmapFactory.decodeStream(input)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                } else {
                    // Si es JPG, simplemente copiamos el flujo (asumiendo que el temporal es alta calidad)
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

// 2. Modifica la función de lanzamiento para que use los parámetros correctos
fun launchCropper(
    context: Context,
    bitmap: Bitmap,
    launcher: ActivityResultLauncher<CropImageContractOptions>,
    format: ExportFormat, // Añadimos el formato como parámetro
) {
    // El nombre del archivo ahora usa la extensión elegida (.jpg o .png)
    val tempFile = File(context.cacheDir, "temp_capture.${format.extension}")

    tempFile.outputStream().use { out ->
        // Elegimos el formato de compresión según la elección del usuario
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

        // OPCIONAL: Puedes configurar el formato de salida del cropper también
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
            // Usamos i + 1 para no capturar el segundo 0 si no queremos
            // Usamos i + 1 para no capturar el segundo 0 si no queremos
            val timeUs = i * intervalSec * 1_000_000L

            // OPTION_CLOSEST_SYNC es más rápido para capturas masivas
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

/**
 * Convierte un Bitmap en una Uri temporal guardando el archivo en la caché.
 * Esto permite que las funciones de guardado final procesen la imagen como un archivo.
 */
suspend fun saveBitmapToTempUri(
    context: Context,
    bitmap: Bitmap,
    format: ExportFormat, // Añadimos el parámetro para respetar la elección del usuario
): Uri {
    return withContext(Dispatchers.IO) {
        // 1. Usamos la extensión dinámica (.jpg o .png)
        val extension = format.extension
        val tempFile =
            File(context.cacheDir, "auto_capture_${System.currentTimeMillis()}.$extension")

        try {
            tempFile.outputStream().use { out ->
                // 2. Seleccionamos el formato de compresión adecuado
                val compressFormat = if (format == ExportFormat.PNG) {
                    Bitmap.CompressFormat.PNG
                } else {
                    Bitmap.CompressFormat.JPEG
                }

                // 3. Comprimimos con la configuración elegida
                bitmap.compress(compressFormat, 100, out)
                out.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Retornamos la Uri del archivo creado
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

    // Cálculo dinámico del máximo permitido
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

                // --- CAMPO DE INTERVALO CON AUTO-AJUSTE ---
                OutlinedTextField(
                    value = intervalSec,
                    onValueChange = { newValue ->
                        if (newValue.all { c -> c.isDigit() } && newValue.length < 6) {
                            intervalSec = newValue

                            // Lógica de auto-ajuste:
                            val newInterval = newValue.toLongOrNull() ?: 0
                            if (newInterval > 0) {
                                val newMax = (videoDuration / newInterval).toInt().coerceAtMost(100)
                                // Si el número de fotos actual es mayor al nuevo máximo, lo bajamos automáticamente

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

                // --- CAMPO DE CANTIDAD DE FOTOS ---
                OutlinedTextField(
                    value = numFrames,
                    onValueChange = { newValue ->
                        if (newValue.all { c -> c.isDigit() } && newValue.length < 4) {
                            val newValueInt = newValue.toIntOrNull() ?: 0
                            // Aquí también podemos auto-ajustar si el usuario intenta escribir de más
                            if (newValueInt <= maxPhotosPossible || newValue.isEmpty()) {
                                numFrames = newValue
                            } else {
                                // Opcional: Forzar el máximo si intenta pasarse
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
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AutoSelectionInterface(
//    frames: List<SelectableFrame>,
//    onSave: (List<SelectableFrame>) -> Unit,
//    onClose: () -> Unit,
//    selectedFormat: ExportFormat
//) {
//    val context = LocalContext.current
//    var frameInDetail by remember { mutableStateOf<SelectableFrame?>(null) }
//
//    // Manejo del botón atrás físico
//    BackHandler(enabled = frameInDetail != null) {
//        frameInDetail = null
//    }
//
//    // Usamos Surface en lugar de Scaffold para evitar que se dupliquen los Insets
//    Surface(
//        modifier = Modifier.fillMaxSize(),
//        color = MaterialTheme.colorScheme.background
//    ) {
//        Column(modifier = Modifier.fillMaxSize()) {
//            // 1. BARRA SUPERIOR (Manual)
//            TopAppBar(
//                title = {
//                    Text(
//                        stringResource(R.string.seleccionar_fotogramas),
//                        style = MaterialTheme.typography.titleMedium
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = onClose) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cerrar")
//                    }
//                },
//                actions = {
//                    val selectedCount = frames.count { it.isSelected.value }
//                    TextButton(
//                        onClick = { onSave(frames.filter { it.isSelected.value }) },
//                        enabled = selectedCount > 0
//                    ) {
//                        Text(
//                            text = "${context.getString(R.string.guardar)} ($selectedCount)",
//                            fontWeight = FontWeight.Bold,
//                            color = if (selectedCount > 0) MaterialTheme.colorScheme.primary else Color.Gray
//                        )
//                    }
//                }
//            )
//
//            // 2. CONTENIDO (Grid)
//            if (frames.isEmpty()) {
//                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                    Text(stringResource(R.string.no_se_pudieron_extraer_fotogramas))
//                }
//            } else {
//                LazyVerticalGrid(
//                    columns = GridCells.Fixed(3),
//                    modifier = Modifier
//                        .weight(1f) // Esto hace que la grid ocupe el espacio disponible y empuje el resto
//                        .fillMaxWidth(),
//                    // Padding inferior extra para que la última fila no pegue con el borde
//                    contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 16.dp),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    items(frames) { frame ->
//                        Box(
//                            modifier = Modifier
//                                .aspectRatio(1f)
//                                .clip(RoundedCornerShape(8.dp))
//                                .background(MaterialTheme.colorScheme.surfaceVariant)
//                                .clickable { frameInDetail = frame }
//                        ) {
//                            Image(
//                                bitmap = frame.bitmap.asImageBitmap(),
//                                contentDescription = null,
//                                modifier = Modifier.fillMaxSize(),
//                                contentScale = ContentScale.Crop // Crop se ve mejor en cuadrículas
//                            )
//
//                            // Checkbox con fondo para que se vea bien sobre cualquier imagen
//                            Box(
//                                modifier = Modifier
//                                    .align(Alignment.TopEnd)
//                                    .padding(4.dp)
//                                    .background(Color.Black.copy(alpha = 0.2f), CircleShape)
//                            ) {
//                                Checkbox(
//                                    checked = frame.isSelected.value,
//                                    onCheckedChange = { frame.isSelected.value = it }
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//
//            // 3. ESPACIO DE SEGURIDAD PARA LA BARRA DE NAVEGACIÓN
//            // Esto asegura que el contenido no quede debajo de los botones del sistema
//            Spacer(modifier = Modifier.navigationBarsPadding())
//        }
//
//        // 4. CAPA DE DETALLE (PANTALLA COMPLETA)
//        AnimatedVisibility(
//            visible = frameInDetail != null,
//            enter = fadeIn(),
//            exit = fadeOut()
//        ) {
//            frameInDetail?.let { selected ->
//                FullScreenFrameViewer(
//                    frame = selected,
//                    onDismiss = { frameInDetail = null },
//                    onShare = {
//                        val uri = saveBitmapToUriSync(context, selected.bitmap, selectedFormat)
//                        val intent = Intent(Intent.ACTION_SEND).apply {
//                            type = "image/*"
//                            putExtra(Intent.EXTRA_STREAM, uri)
//                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                        }
//                        context.startActivity(Intent.createChooser(intent, context.getString(R.string.compartir)))
//                    },
//                    selectedFormat = selectedFormat,
//                    context = context
//                )
//            }
//        }
//    }
//}
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AutoSelectionInterface(
//    frames: List<SelectableFrame>,
//    onSave: (List<SelectableFrame>) -> Unit,
//    isSaving: Boolean,
//    onClose: () -> Unit,
//    selectedFormat: ExportFormat
//) {
//    val context = LocalContext.current
//    var frameInDetail by remember { mutableStateOf<SelectableFrame?>(null) }
//
//    // Manejo del botón atrás físico para cerrar el detalle si está abierto
//    BackHandler(enabled = frameInDetail != null) {
//        frameInDetail = null
//    }
//
//    // Usamos Surface como contenedor principal para controlar el fondo y evitar paddings automáticos
//    Surface(
//        modifier = Modifier.fillMaxSize(),
//        color = MaterialTheme.colorScheme.background
//    ) {
//        if (isSaving) {
//            LoadingOverlay() // Llamamos a una función con tu animación
//        }
//        Column(modifier = Modifier.fillMaxSize()) {
//            // 1. BARRA SUPERIOR MANUAL (Sustituye a la del Scaffold)
//            TopAppBar(
//                title = {
//                    Text(
//                        stringResource(R.string.seleccionar_fotogramas),
//                        style = MaterialTheme.typography.titleMedium
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = onClose) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cerrar")
//                    }
//                },
//                actions = {
//                    val selectedCount = frames.count { it.isSelected.value }
//                    TextButton(
//                        onClick = { onSave(frames.filter { it.isSelected.value }) },
//                        enabled = selectedCount > 0
//                    ) {
//                        Text(
//                            text = "${context.getString(R.string.guardar)} ($selectedCount)",
//                            fontWeight = FontWeight.Bold,
//                            color = if (selectedCount > 0) MaterialTheme.colorScheme.primary else Color.Gray
//                        )
//                    }
//                }
//            )
//
//            // 2. CONTENIDO (Grid)
//            if (frames.isEmpty()) {
//                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                    Text(stringResource(R.string.no_se_pudieron_extraer_fotogramas))
//                }
//            } else {
//                LazyVerticalGrid(
//                    columns = GridCells.Fixed(3),
//                    modifier = Modifier
//                        .weight(1f) // Esto hace que la grid use todo el espacio y empuje lo demás
//                        .fillMaxWidth(),
//                    contentPadding = PaddingValues(8.dp),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    items(frames) { frame ->
//                        Box(
//                            modifier = Modifier
//                                .aspectRatio(1f)
//                                .clip(RoundedCornerShape(8.dp))
//                                .background(MaterialTheme.colorScheme.surfaceVariant)
//                                .clickable { frameInDetail = frame }
//                        ) {
//                            Image(
//                                bitmap = frame.bitmap.asImageBitmap(),
//                                contentDescription = null,
//                                modifier = Modifier.fillMaxSize(),
//                                contentScale = ContentScale.Crop
//                            )
//
//                            // Checkbox con un pequeño fondo para que sea visible siempre
//                            Box(
//                                modifier = Modifier
//                                    .align(Alignment.TopEnd)
//                                    .padding(4.dp)
//                                    .background(Color.Black.copy(alpha = 0.2f), CircleShape)
//                            ) {
//                                Checkbox(
//                                    checked = frame.isSelected.value,
//                                    onCheckedChange = { frame.isSelected.value = it }
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//
//            // 3. ESPACIO FINAL (Crucial para Edge-to-Edge)
//            // Este Spacer añade EXACTAMENTE el tamaño de la barra de navegación del sistema
//            // evitando que el contenido quede detrás de los botones de Android pero sin duplicar espacios.
//            Spacer(modifier = Modifier.navigationBarsPadding())
//        }
//
//        // 4. CAPA DE DETALLE (Encima de todo)
//        AnimatedVisibility(
//            visible = frameInDetail != null,
//            enter = fadeIn(),
//            exit = fadeOut()
//        ) {
//            frameInDetail?.let { selected ->
//                FullScreenFrameViewer(
//                    frame = selected,
//                    onDismiss = { frameInDetail = null },
//                    onShare = {
//                        val uri = saveBitmapToUriSync(context, selected.bitmap, selectedFormat)
//                        val intent = Intent(Intent.ACTION_SEND).apply {
//                            type = "image/*"
//                            putExtra(Intent.EXTRA_STREAM, uri)
//                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                        }
//                        context.startActivity(Intent.createChooser(intent, context.getString(R.string.compartir)))
//                    },
//                    selectedFormat = selectedFormat,
//                    context = context
//                )
//            }
//        }
//    }
//}

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

    // Manejo del botón atrás físico para cerrar el detalle
    BackHandler(enabled = frameInDetail != null) {
        frameInDetail = null
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Usamos Scaffold porque gestiona automáticamente los insets (barras del sistema)
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
            // El paddingValues aquí ya contiene el espacio de la Status Bar y la TopAppBar
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
                        // Añadimos padding inferior extra para que la última fila
                        // no quede pegada a la barra de navegación
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

        // --- CAPAS SUPERIORES (Z-Index más alto) ---

        // 2. CAPA DE DETALLE
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

        // 3. CAPA DE CARGA GLOBAL
        // Al estar fuera del Scaffold pero dentro del Surface principal,
        // cubrirá toda la pantalla, incluyendo la TopAppBar.
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

    // Necesitamos el launcher del cropper aquí también
    val cropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // Si se recorta con éxito, cerramos el visor para ver los cambios
            // (o podrías implementar lógica para actualizar el bitmap del frame)
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

        // Barra Superior
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        // Botonera Inferior: COMPARTIR Y RECORTAR
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
            // Define la subcarpeta en Pictures
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

// Función auxiliar simple para compartir bitmaps que no están en la BD aún
fun saveBitmapToUriSync(
    context: Context,
    bitmap: Bitmap,
    format: ExportFormat, // Agregamos el formato elegido
): Uri {
    // 1. Usamos la extensión dinámica (.jpg o .png) según el enum
    val extension = format.extension
    val file = File(context.cacheDir, "share_temp.$extension")

    file.outputStream().use { out ->
        // 2. Seleccionamos el formato de compresión de Android equivalente
        val compressFormat = if (format == ExportFormat.PNG) {
            Bitmap.CompressFormat.PNG
        } else {
            Bitmap.CompressFormat.JPEG
        }

        // 3. Comprimimos con calidad máxima para el compartido
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