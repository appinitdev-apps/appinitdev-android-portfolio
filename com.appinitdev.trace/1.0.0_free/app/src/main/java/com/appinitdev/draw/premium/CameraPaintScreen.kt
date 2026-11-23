@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraPaintScreen(
    startWithCameraOn: Boolean,
    initialImageUri: Uri? = null,
    initialGridType: String = "NONE", 
    initialFilter: String = "NONE", 
    initialTab: Int = -1, 
    onBack: () -> Unit,
    onChangeMode: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val OffsetSaver = listSaver<MutableState<Offset>, Float>(
        save = { listOf(it.value.x, it.value.y) },
        restore = { mutableStateOf(Offset(it[0], it[1])) }
    )
    var permissionsGranted by rememberSaveable { mutableStateOf(false) }
    var showCamera by rememberSaveable { mutableStateOf(startWithCameraOn) }
    var isFlashOn by rememberSaveable { mutableStateOf(false) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var scale by rememberSaveable { mutableStateOf(1f) }
    var rotation by rememberSaveable { mutableStateOf(0f) }
    var flipX by rememberSaveable { mutableStateOf(1f) } 
    var flipY by rememberSaveable { mutableStateOf(1f) } 
    var offset by rememberSaveable(saver = OffsetSaver) { mutableStateOf(Offset.Zero) }
    var isLocked by rememberSaveable { mutableStateOf(false) }
    var imageUriStr by rememberSaveable { mutableStateOf(initialImageUri?.toString()) }
    var originalImageUriStr by rememberSaveable { mutableStateOf(initialImageUri?.toString()) }
    val imageUri = imageUriStr?.let { Uri.parse(it) }
    val originalImageUri = originalImageUriStr?.let { Uri.parse(it) }
    var sketchLineSize by rememberSaveable { mutableStateOf(1.0f) } 
    var sketchThreshold by rememberSaveable { mutableStateOf(0.15f) } 
    var isSketchMode by rememberSaveable { mutableStateOf(false) } 
    var imageAlpha by rememberSaveable { mutableStateOf(if (!startWithCameraOn) 1.0f else 0.5f) }
    var currentColor by remember { mutableStateOf(Color(0xFFFF4081)) }
    var currentFilter by rememberSaveable { mutableStateOf(initialFilter) } 
    var gridType by rememberSaveable { mutableStateOf(initialGridType) } 
    var gridSize by rememberSaveable { mutableFloatStateOf(200f) } 
    var showImage by rememberSaveable { mutableStateOf(true) }
    var cameraControl: CameraControl? by remember { mutableStateOf(null) }
    var isProcessingBg by remember { mutableStateOf(false) }
    var isRecordingTimeLapse by rememberSaveable { mutableStateOf(false) } 
    var timeLapseCount by rememberSaveable { mutableIntStateOf(0) } 
    var currentTimeLapseSessionId by rememberSaveable { mutableStateOf<String?>(null) } 
    var showPip by rememberSaveable { mutableStateOf(false) }
    var pipOffset by rememberSaveable(saver = OffsetSaver) { mutableStateOf(Offset(20f, 600f)) } 
    var pipSize by rememberSaveable { mutableFloatStateOf(160f) }
    var isWorldLockActive by rememberSaveable { mutableStateOf(false) }
    var referencePoints by rememberSaveable { mutableStateOf<List<Offset>>(emptyList()) }
    var isRefPointsActive by rememberSaveable { mutableStateOf(false) }
    var pencilColorMode by rememberSaveable { mutableStateOf("BLUE") }
    var posterizeLevels by rememberSaveable { mutableFloatStateOf(10f) }
    var gammaValue by rememberSaveable { mutableFloatStateOf(2.0f) }
    var sharpnessValue by rememberSaveable { mutableFloatStateOf(2.0f) }
    var grayscaleSaturation by rememberSaveable { mutableFloatStateOf(0.0f) }
    var filterJob by remember { mutableStateOf<Job?>(null) }
    var showColorGuide by remember { mutableStateOf(false) }
    var selectedColorForGuide by remember { mutableStateOf<Color?>(null) }
    var showSidePanel by remember { mutableStateOf(true) } 
    val activeColor = Color(0xFFFF4081)
    var showCaptureDialog by remember { mutableStateOf(false) } 
    var capturedBitmapForPreview by remember { mutableStateOf<Bitmap?>(null) } 
    var showRealTimeCapturePreview by remember { mutableStateOf(false) } 
    var captureModeSelected by remember { mutableStateOf("NONE") } 
    var capturePreviewViewRef by remember { mutableStateOf<PreviewView?>(null) } 
    LaunchedEffect(initialFilter, originalImageUriStr) {
        if (initialFilter == "SKETCH" && originalImageUriStr != null) {
            val uriActual = originalImageUri ?: return@LaunchedEffect
            isProcessingBg = true
            scope.launch(Dispatchers.IO) {
                try {
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    context.contentResolver.openInputStream(uriActual)?.use { 
                        BitmapFactory.decodeStream(it, null, options)
                    }
                    var inSampleSize = 1
                    while (options.outWidth / inSampleSize > 2048 || options.outHeight / inSampleSize > 2048) {
                        inSampleSize *= 2
                    }
                    val decodeOptions = BitmapFactory.Options().apply {
                        inSampleSize = inSampleSize
                        inMutable = true
                    }
                    val bitmapOriginal: Bitmap = context.contentResolver.openInputStream(uriActual)?.use {
                        BitmapFactory.decodeStream(it, null, decodeOptions)
                    } ?: throw Exception("No se pudo cargar la imagen")
                    val gpuImage = com.appinitdev.gpuimage.GPUImage(context)
                    gpuImage.setImage(bitmapOriginal)
                    val filterSketch = GPUImageThresholdEdgeDetectionFilter()
                    filterSketch.setLineSize(sketchLineSize)
                    filterSketch.setThreshold(sketchThreshold)
                    gpuImage.setFilter(filterSketch)
                    val bitmapProcesado = gpuImage.bitmapWithFilterApplied
                    val width = bitmapProcesado.width
                    val height = bitmapProcesado.height
                    val pixels = IntArray(width * height)
                    bitmapProcesado.getPixels(pixels, 0, width, 0, 0, width, height)
                    for (i in pixels.indices) {
                        val p = pixels[i]
                        val r = (p shr 16) and 0xFF
                        val g = (p shr 8) and 0xFF
                        val b = p and 0xFF
                        if (r > 220 && g > 220 && b > 220) {
                            pixels[i] = 0x00000000
                        }
                    }
                    val bitmapFinal = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    bitmapFinal.setPixels(pixels, 0, width, 0, 0, width, height)
                    val archivoTemporal = File(context.cacheDir, "sketch_${System.currentTimeMillis()}.png")
                    FileOutputStream(archivoTemporal).use { stream ->
                        bitmapFinal.compress(Bitmap.CompressFormat.PNG, 90, stream)
                    }
                    val nuevoUri = Uri.fromFile(archivoTemporal)
                    bitmapOriginal.recycle()
                    withContext(Dispatchers.Main) {
                        imageUriStr = nuevoUri.toString()
                        isProcessingBg = false
                    }
                } catch (e: Exception) {
                    Log.e("CameraPaintScreen", "Error en procesamiento de boceto", e)
                    withContext(Dispatchers.Main) {
                        isProcessingBg = false
                        Toast.makeText(context, "Error al generar boceto estable", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    fun applyGpuFilterDebounced(delayMs: Long = 100L, filterSupplier: () -> com.appinitdev.gpuimage.filter.GPUImageFilter) {
        filterJob?.cancel()
        filterJob = scope.launch {
            if (delayMs > 0) delay(delayMs)
            val uriActual = originalImageUri ?: return@launch
            val gpuFilter = filterSupplier()
            isProcessingBg = true
            try {
                withContext(Dispatchers.IO) {
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(uriActual)?.use { BitmapFactory.decodeStream(it, null, options) }
                    var inSampleSize = 1
                    while (options.outWidth / inSampleSize > 2048 || options.outHeight / inSampleSize > 2048) inSampleSize *= 2
                    val decodeOptions = BitmapFactory.Options().apply { 
                        this.inSampleSize = inSampleSize
                        inMutable = true 
                    }
                    val bitmapOriginal: Bitmap = context.contentResolver.openInputStream(uriActual)?.use {
                        BitmapFactory.decodeStream(it, null, decodeOptions)
                    } ?: throw Exception("No se pudo cargar")
                    val gpuImage = com.appinitdev.gpuimage.GPUImage(context)
                    gpuImage.setImage(bitmapOriginal)
                    gpuImage.setFilter(gpuFilter)
                    val bitmapProcesado = gpuImage.bitmapWithFilterApplied
                    val archivoTemporal = File(context.cacheDir, "filter_${System.currentTimeMillis()}.png")
                    FileOutputStream(archivoTemporal).use { stream ->
                        bitmapProcesado.compress(Bitmap.CompressFormat.PNG, 90, stream)
                    }
                    val nuevoUri = Uri.fromFile(archivoTemporal)
                    bitmapOriginal.recycle()
                    withContext(Dispatchers.Main) {
                        imageUriStr = nuevoUri.toString()
                        isProcessingBg = false
                    }
                }
            } catch (e: Exception) {
                Log.e("CameraPaintScreen", "Error applying GPU filter", e)
                withContext(Dispatchers.Main) {
                    isProcessingBg = false
                }
            }
        }
    }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            permissionsGranted = perms[Manifest.permission.CAMERA] == true
        }
    LaunchedEffect(Unit) { permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA)) }
    LaunchedEffect(isRecordingTimeLapse) {
        if (isRecordingTimeLapse) {
            val sessionId = java.util.UUID.randomUUID().toString()
            currentTimeLapseSessionId = sessionId
            timeLapseCount = 0
            Toast.makeText(context, "TimeLapse iniciado", Toast.LENGTH_SHORT).show()
            delay(500)
            val firstBitmap = withContext(Dispatchers.Main) { previewViewRef?.bitmap }
            if (firstBitmap != null) {
                GalleryStorageManager.saveCapturedWork(
                    context = context, 
                    bitmap = firstBitmap, 
                    templateUri = imageUri,
                    isTimeLapse = true,
                    sessionId = sessionId
                )
                timeLapseCount = 1
            }
            while (isActive && isRecordingTimeLapse) {
                delay(5000) 
                val bitmap = withContext(Dispatchers.Main) { previewViewRef?.bitmap }
                if (bitmap != null) {
                    GalleryStorageManager.saveCapturedWork(
                        context = context, 
                        bitmap = bitmap, 
                        templateUri = imageUri,
                        isTimeLapse = true,
                        sessionId = sessionId
                    )
                    timeLapseCount++
                }
            }
        } else {
            if (currentTimeLapseSessionId != null && timeLapseCount > 0) {
                Toast.makeText(context, "TimeLapse guardado ($timeLapseCount fotos)", Toast.LENGTH_SHORT).show()
            }
            currentTimeLapseSessionId = null
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
    ) {
        if (permissionsGranted && showCamera) {
            CameraPreviewComposable(
                modifier = Modifier.fillMaxSize(),
                showCamera = showCamera,
                onCameraReady = { control -> cameraControl = control },
                onPreviewReady = { previewView -> previewViewRef = previewView }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (showCamera) Color.Transparent else Color.White)
                .pointerInput(isLocked, isWorldLockActive) {
                    if (!isLocked && !isWorldLockActive) {
                        detectTransformGestures { _, pan, zoom, rotationChange ->
                            scale = (scale * zoom).coerceIn(0.5f, 15f)
                            rotation += rotationChange
                            offset += pan
                        }
                    }
                }
                .pointerInput(isLocked, isRefPointsActive) {
                    detectTapGestures(
                        onLongPress = { tapOffset ->
                            if (!isLocked && isRefPointsActive) {
                                if (referencePoints.size < 4) {
                                    referencePoints = referencePoints + tapOffset
                                } else {
                                    Toast.makeText(context, context.getString(R.string.max_ref_points), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
        ) {
            if (showImage && imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = coil.request.ImageRequest.Builder(LocalContext.current)
                            .data(imageUri)
                            .memoryCacheKey(imageUri.toString() + "_" + currentFilter + "_" + pencilColorMode + "_" + grayscaleSaturation)
                            .build()
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale * flipX,
                            scaleY = scale * flipY,
                            rotationZ = rotation,
                            translationX = offset.x,
                            translationY = offset.y
                        ),
                    alpha = imageAlpha,
                    colorFilter = when (currentFilter) {
                        "GRAYSCALE" -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(grayscaleSaturation) })
                        "INVERT" -> ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(-1f,0f,0f,0f,255f, 0f,-1f,0f,0f,255f, 0f,0f,-1f,0f,255f, 0f,0f,0f,1f,0f)))
                        "SEPIA" -> ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(0.393f, 0.769f, 0.189f, 0f, 0f, 0.349f, 0.686f, 0.168f, 0f, 0f, 0.272f, 0.534f, 0.131f, 0f, 0f, 0f, 0f, 0f, 1f, 0f)))
                        "BLUE_PENCIL" -> when (pencilColorMode) {
                            "RED" -> ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
                                0.2126f * 0.85f, 0.7152f * 0.85f, 0.0722f * 0.85f, 0f, 200f,
                                0.2126f * 0.15f, 0.7152f * 0.15f, 0.0722f * 0.15f, 0f, 0f,
                                0.2126f * 0.15f, 0.7152f * 0.15f, 0.0722f * 0.15f, 0f, 0f,
                                0f, 0f, 0f, 1f, 0f
                            )))
                            "GREEN" -> ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
                                0.2126f * 0.15f, 0.7152f * 0.15f, 0.0722f * 0.15f, 0f, 0f,
                                0.2126f * 0.85f, 0.7152f * 0.85f, 0.0722f * 0.85f, 0f, 200f,
                                0.2126f * 0.25f, 0.7152f * 0.25f, 0.0722f * 0.25f, 0f, 20f,
                                0f, 0f, 0f, 1f, 0f
                            )))
                            else -> ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
                                0.2126f * 0.15f, 0.7152f * 0.15f, 0.0722f * 0.15f, 0f, 0f,
                                0.2126f * 0.45f, 0.7152f * 0.45f, 0.0722f * 0.45f, 0f, 50f,
                                0.2126f * 0.85f, 0.7152f * 0.85f, 0.0722f * 0.85f, 0f, 200f,
                                0f, 0f, 0f, 1f, 0f
                            )))
                        }
                        else -> null
                    }
                )
                referencePoints.forEachIndexed { index, point ->
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(point.x.roundToInt() - 20, point.y.roundToInt() - 20) }
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(activeColor.copy(alpha = 0.3f))
                            .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                            .pointerInput(index) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val newList = referencePoints.toMutableList()
                                    if (index < newList.size) {
                                        newList[index] = newList[index] + dragAmount
                                        referencePoints = newList
                                    }
                                }
                            }
                            .pointerInput(index) {
                                detectTapGestures(
                                    onTap = {
                                        referencePoints = referencePoints.filterIndexed { i, _ -> i != index }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.size(24.dp)) {
                            val lineLength = size.width * 0.6f
                            val stroke = 2.dp.toPx()
                            drawLine(
                                color = Color.Black.copy(alpha = 0.3f),
                                start = Offset(size.width / 2 - lineLength / 2 + 1, size.height / 2 + 1),
                                end = Offset(size.width / 2 + lineLength / 2 + 1, size.height / 2 + 1),
                                strokeWidth = stroke
                            )
                            drawLine(
                                color = Color.White,
                                start = Offset(size.width / 2 - lineLength / 2, size.height / 2),
                                end = Offset(size.width / 2 + lineLength / 2, size.height / 2),
                                strokeWidth = stroke
                            )
                            drawLine(
                                color = Color.White,
                                start = Offset(size.width / 2, size.height / 2 - lineLength / 2),
                                end = Offset(size.width / 2, size.height / 2 + lineLength / 2),
                                strokeWidth = stroke
                            )
                        }
                    }
                }
            }
            if (gridType != "NONE") {
                GridOverlay(
                    type = gridType,
                    gridSpacing = gridSize,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        if (showPip && imageUri != null) {
            val pipPainter = rememberAsyncImagePainter(imageUri)
            val intrinsicSize = pipPainter.intrinsicSize
            val aspectRatio = if (intrinsicSize.width != 0f && intrinsicSize.height != 0f && !intrinsicSize.width.isNaN()) {
                intrinsicSize.width / intrinsicSize.height
            } else {
                1f
            }
            Box(
                modifier = Modifier
                    .offset { IntOffset(pipOffset.x.roundToInt(), pipOffset.y.roundToInt()) }
                    .width(pipSize.dp)
                    .aspectRatio(aspectRatio)
                    .shadow(8.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            pipOffset += pan
                            pipSize = (pipSize * zoom).coerceIn(100f, 500f)
                        }
                    }
                    .border(2.dp, activeColor, RoundedCornerShape(12.dp))
            ) {
                Image(
                    painter = pipPainter,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { showPip = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                pipSize = (pipSize + dragAmount.x / 2).coerceIn(100f, 500f)
                            }
                        }
                        .background(activeColor.copy(alpha = 0.8f), RoundedCornerShape(topStart = 12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.OpenInFull, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
        if (showColorGuide) {
            ColorMixingDialog(
                onDismiss = { showColorGuide = false },
                activeColor = activeColor
            )
        }
        if (isProcessingBg) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .pointerInput(Unit) {}, 
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.procesando_imagen),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        Box(
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            AnimatedVisibility(
                visible = showSidePanel,
                enter = expandHorizontally(expandFrom = Alignment.End),
                exit = shrinkHorizontally(shrinkTowards = Alignment.End)
            ) {
                CameraControlsPanelPro(
                    showCamera = showCamera,
                    onToggleCamera = { showCamera = !showCamera },
                    isFlashOn = isFlashOn,
                    onToggleFlash = {
                        cameraControl?.let { control ->
                            val targetState = !isFlashOn
                            control.enableTorch(targetState)
                            isFlashOn = targetState
                        }
                    },
                    imageUri = imageUri,
                    imageAlpha = imageAlpha,
                    onAlphaChange = { imageAlpha = it },
                    currentColor = currentColor,
                    onImageUriChange = { 
                        imageUriStr = it.toString()
                        originalImageUriStr = it.toString() 
                    },
                    onTabSelected = {},
                    onRemoveBackground = {
                        val uriActual = imageUri
                        if (uriActual != null) {
                            if (isProcessingBg) return@CameraControlsPanelPro
                            isProcessingBg = true
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                                    context.contentResolver.openInputStream(uriActual)?.use { BitmapFactory.decodeStream(it, null, options) }
                                    var inSampleSize = 1
                                    while (options.outWidth / inSampleSize > 2048 || options.outHeight / inSampleSize > 2048) inSampleSize *= 2
                                    val decodeOptions = BitmapFactory.Options().apply { 
                                        this.inSampleSize = inSampleSize
                                        inMutable = true 
                                    }
                                    val bitmapOriginal = context.contentResolver.openInputStream(uriActual)?.use {
                                        BitmapFactory.decodeStream(it, null, decodeOptions)
                                    } ?: throw Exception("No se pudo cargar la imagen")
                                    val bitmapSinFondo = bitmapOriginal.removeBackground(context = context)
                                    val archivoTemporal = File(context.cacheDir, "nobg_${System.currentTimeMillis()}.png")
                                    FileOutputStream(archivoTemporal).use { stream ->
                                        bitmapSinFondo.compress(Bitmap.CompressFormat.PNG, 90, stream)
                                    }
                                    val nuevoUri = Uri.fromFile(archivoTemporal)
                                    bitmapOriginal.recycle()
                                    withContext(Dispatchers.Main) {
                                        imageUriStr = nuevoUri.toString()
                                        isProcessingBg = false
                                    }
                                } catch (e: Exception) {
                                    Log.e("CameraPaintScreen", "Error removeBackground", e)
                                    withContext(Dispatchers.Main) {
                                        isProcessingBg = false
                                        Toast.makeText(context, "Error al quitar fondo", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(context, context.getString(R.string.selecciona_una_imagen_antes_de_remover_el_fondo), Toast.LENGTH_SHORT).show()
                        }
                    },
                    onSketch = { lineSize, threshold ->
                        val uriActual = originalImageUri
                        if (uriActual != null) {
                            if (isProcessingBg) return@CameraControlsPanelPro
                            isProcessingBg = true
                            Toast.makeText(context, context.getString(R.string.procesando_boceto), Toast.LENGTH_SHORT).show()
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                                    context.contentResolver.openInputStream(uriActual)?.use { BitmapFactory.decodeStream(it, null, options) }
                                    var inSampleSize = 1
                                    while (options.outWidth / inSampleSize > 2048 || options.outHeight / inSampleSize > 2048) inSampleSize *= 2
                                    val decodeOptions = BitmapFactory.Options().apply { 
                                        this.inSampleSize = inSampleSize
                                        inMutable = true 
                                    }
                                    val bitmapOriginal = context.contentResolver.openInputStream(uriActual)?.use {
                                        BitmapFactory.decodeStream(it, null, decodeOptions)
                                    } ?: throw Exception("No se pudo cargar la imagen")
                                    val gpuImage = com.appinitdev.gpuimage.GPUImage(context)
                                    val blurFilter = com.appinitdev.gpuimage.filter.GPUImageBilateralBlurFilter()
                                    blurFilter.setDistanceNormalizationFactor(0.8f)
                                    gpuImage.setImage(bitmapOriginal)
                                    gpuImage.setFilter(blurFilter)
                                    val bitmapBlurred = gpuImage.bitmapWithFilterApplied
                                    gpuImage.setImage(bitmapBlurred)
                                    val filterSketch = GPUImageThresholdEdgeDetectionFilter()
                                    filterSketch.setLineSize(lineSize * 1.5f) 
                                    filterSketch.setThreshold(threshold)
                                    gpuImage.setFilter(filterSketch)
                                    val bitmapProcesado = gpuImage.bitmapWithFilterApplied
                                    val width = bitmapProcesado.width
                                    val height = bitmapProcesado.height
                                    val pixels = IntArray(width * height)
                                    bitmapProcesado.getPixels(pixels, 0, width, 0, 0, width, height)
                                    for (i in pixels.indices) {
                                        val p = pixels[i]
                                        val r = (p shr 16) and 0xFF
                                        val g = (p shr 8) and 0xFF
                                        val b = p and 0xFF
                                        if (r > 215 && g > 215 && b > 215) {
                                            pixels[i] = 0x00000000
                                        } else {
                                            pixels[i] = (p and 0xFF000000.toInt()) or 0x00000000
                                        }
                                    }
                                    val bitmapFinal = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                    bitmapFinal.setPixels(pixels, 0, width, 0, 0, width, height)
                                    val archivoTemporal = File(context.cacheDir, "sketch_${System.currentTimeMillis()}.png")
                                    FileOutputStream(archivoTemporal).use { stream ->
                                        bitmapFinal.compress(Bitmap.CompressFormat.PNG, 90, stream)
                                    }
                                    val nuevoUri = Uri.fromFile(archivoTemporal)
                                    bitmapOriginal.recycle()
                                    bitmapBlurred.recycle()
                                    withContext(Dispatchers.Main) {
                                        imageUriStr = nuevoUri.toString()
                                        isProcessingBg = false
                                        Toast.makeText(context, context.getString(R.string.boceto_listo), Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Log.e("CameraPaintScreen", "Error sketch", e)
                                    withContext(Dispatchers.Main) {
                                        isProcessingBg = false
                                        Toast.makeText(context, context.getString(R.string.error_boceto), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(context, context.getString(R.string.selecciona_imagen_primero), Toast.LENGTH_SHORT).show()
                        }
                    },
                    onRestoreOriginal = {
                        imageUriStr = originalImageUriStr
                        Toast.makeText(context, context.getString(R.string.imagen_restaurada), Toast.LENGTH_SHORT).show()
                    },
                    onCapture = {
                        captureModeSelected = "CAMERA_ONLY"
                        showRealTimeCapturePreview = true
                        if (!showCamera) {
                            showCamera = true
                        }
                    },
                    onRotateLeft = { rotation -= 90f },
                    onRotateRight = { rotation += 90f },
                    onFlipHorizontal = { flipX *= -1f },
                    onFlipVertical = { flipY *= -1f },
                    currentFilter = currentFilter,
                    onFilterChange = { nuevoFiltro ->
                        val processedFilters = listOf("SKETCH", "POSTERIZE", "SHARPEN", "GAMMA")
                        if (processedFilters.contains(currentFilter) && !processedFilters.contains(nuevoFiltro)) {
                            imageUriStr = originalImageUriStr
                        }
                        currentFilter = nuevoFiltro 
                        when (nuevoFiltro) {
                            "POSTERIZE" -> applyGpuFilterDebounced(0L) { GPUImagePosterizeFilter(posterizeLevels.toInt()) }
                            "SHARPEN" -> applyGpuFilterDebounced(0L) { GPUImageSharpenFilter(sharpnessValue) }
                            "GAMMA" -> applyGpuFilterDebounced(0L) { GPUImageGammaFilter(gammaValue) }
                        }
                    },
                    pencilColorMode = pencilColorMode,
                    onPencilColorModeChange = { pencilColorMode = it },
                    posterizeLevels = posterizeLevels,
                    onPosterizeLevelsChange = { level ->
                        posterizeLevels = level
                        applyGpuFilterDebounced(150L) { GPUImagePosterizeFilter(level.toInt()) }
                    },
                    gammaValue = gammaValue,
                    onGammaValueChange = { g ->
                        gammaValue = g
                        applyGpuFilterDebounced(150L) { GPUImageGammaFilter(g) }
                    },
                    sharpnessValue = sharpnessValue,
                    onSharpnessValueChange = { s ->
                        sharpnessValue = s
                        applyGpuFilterDebounced(150L) { GPUImageSharpenFilter(s) }
                    },
                    grayscaleSaturation = grayscaleSaturation,
                    onGrayscaleSaturationChange = { sat ->
                        grayscaleSaturation = sat
                    },
                    isRefPointsActive = isRefPointsActive,
                    onToggleRefPoints = { isRefPointsActive = !isRefPointsActive },
                    referencePointsCount = referencePoints.size,
                    isCollapsed = false,
                    onChangeMode = onChangeMode,
                    gridType = gridType,
                    onGridTypeChange = { gridType = it },
                    gridSize = gridSize,
                    onGridSizeChange = { gridSize = it },
                    isRecordingTimeLapse = isRecordingTimeLapse,
                    onToggleTimeLapse = { 
                        isRecordingTimeLapse = !isRecordingTimeLapse 
                        if (isRecordingTimeLapse) timeLapseCount = 0
                    },
                    showPip = showPip,
                    onTogglePip = { showPip = !showPip },
                    isWorldLockActive = isWorldLockActive,
                    onToggleWorldLock = { isWorldLockActive = !isWorldLockActive },
                    onShowColorGuide = { showColorGuide = true },
                    onClearReferencePoints = { referencePoints = emptyList() },
                    initialTab = initialTab 
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.25f))
            ) {
                Icon(Icons.Filled.ArrowBack, null, tint = Color.White)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
            }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (isRecordingTimeLapse) {
                        Surface(
                            color = Color.Red.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color.White, CircleShape)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "${stringResource(R.string.rec)} $timeLapseCount",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = { showSidePanel = !showSidePanel },
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.25f))
                    ) {
                        Icon(if (showSidePanel) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null, tint = Color.White)
                    }
                IconButton(
                    onClick = { isLocked = !isLocked },
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.25f))
                ) {
                    Icon(if (isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen, null, tint = if (isLocked) currentColor else Color.White)
                }
            }
        }
        if (showRealTimeCapturePreview) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .pointerInput(Unit) {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp, bottom = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (captureModeSelected == "SIDE_BY_SIDE") 
                                stringResource(R.string.capturar_todo).uppercase() 
                            else stringResource(R.string.solo_dibujo).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.DarkGray)
                    ) {
                        if (captureModeSelected == "CAMERA_ONLY") {
                            CameraPreviewComposable(
                                modifier = Modifier.fillMaxSize(),
                                showCamera = true,
                                onCameraReady = {},
                                onPreviewReady = { capturePreviewViewRef = it }
                            )
                        } else {
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.weight(1f)) {
                                    CameraPreviewComposable(
                                        modifier = Modifier.fillMaxSize(),
                                        showCamera = true,
                                        onCameraReady = {},
                                        onPreviewReady = { capturePreviewViewRef = it }
                                    )
                                }
                                Box(modifier = Modifier.weight(1f).background(Color.White)) {
                                    if (imageUri != null) {
                                        Image(
                                            painter = rememberAsyncImagePainter(imageUri),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { 
                                showRealTimeCapturePreview = false 
                                showCamera = true 
                            },
                            modifier = Modifier.size(60.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                        }
                        FloatingActionButton(
                            onClick = {
                                isProcessingBg = true
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        val cameraBitmap = withContext(Dispatchers.Main) { 
                                            capturePreviewViewRef?.bitmap 
                                        }
                                        if (cameraBitmap == null) {
                                            withContext(Dispatchers.Main) { isProcessingBg = false }
                                            return@launch
                                        }
                                        val resultBitmap = if (captureModeSelected == "SIDE_BY_SIDE") {
                                            val templateBitmap = imageUri?.let { uri ->
                                                val source = ImageDecoder.createSource(context.contentResolver, uri)
                                                ImageDecoder.decodeBitmap(source)
                                            }
                                            val camW = cameraBitmap.width
                                            val camH = cameraBitmap.height
                                            val totalWidth = if (templateBitmap != null) camW * 2 else camW
                                            val rb = Bitmap.createBitmap(totalWidth, camH, Bitmap.Config.ARGB_8888)
                                            val canvas = android.graphics.Canvas(rb)
                                            canvas.drawBitmap(cameraBitmap, 0f, 0f, null)
                                            templateBitmap?.let { tb ->
                                                val paint = android.graphics.Paint().apply { isFilterBitmap = true }
                                                val bgPaint = android.graphics.Paint().apply { color = android.graphics.Color.WHITE }
                                                canvas.drawRect(android.graphics.RectF(camW.toFloat(), 0f, totalWidth.toFloat(), camH.toFloat()), bgPaint)
                                                val scale = camH.toFloat() / tb.height.toFloat()
                                                val finalW = tb.width * scale
                                                val left = camW + (camW - finalW) / 2f
                                                canvas.drawBitmap(tb, null, android.graphics.RectF(left, 0f, left + finalW, camH.toFloat()), paint)
                                            }
                                            rb
                                        } else {
                                            cameraBitmap
                                        }
                                        withContext(Dispatchers.Main) {
                                            isProcessingBg = false
                                            showRealTimeCapturePreview = false
                                            capturedBitmapForPreview = resultBitmap
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        withContext(Dispatchers.Main) { isProcessingBg = false }
                                    }
                                }
                            },
                            containerColor = currentColor,
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(40.dp))
                        }
                        Box(modifier = Modifier.size(60.dp))
                    }
                }
            }
        }
        if (capturedBitmapForPreview != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .pointerInput(Unit) {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp, bottom = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.capturar_obra).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            fontSize = 18.sp
                        )
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        color = Color.Black,
                        shadowElevation = 10.dp
                    ) {
                        Image(
                            bitmap = capturedBitmapForPreview!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { capturedBitmapForPreview = null },
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(30.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.cerrar), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                val bitmapToSave = capturedBitmapForPreview
                                if (bitmapToSave != null) {
                                    isProcessingBg = true
                                    scope.launch(Dispatchers.IO) {
                                        val success = GalleryStorageManager.saveCapturedWork(
                                            context = context, 
                                            bitmap = bitmapToSave, 
                                            templateUri = imageUri,
                                            isTimeLapse = false
                                        )
                                        withContext(Dispatchers.Main) {
                                            isProcessingBg = false
                                            if (success) {
                                                Toast.makeText(context, context.getString(R.string.guardado_exitoso), Toast.LENGTH_SHORT).show()
                                                capturedBitmapForPreview = null 
                                            } else {
                                                Toast.makeText(context, context.getString(R.string.error_al_guardar), Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = currentColor),
                            shape = RoundedCornerShape(30.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            Icon(Icons.Default.Save, null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.guardado_exitoso), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun CameraPreviewComposable(
    modifier: Modifier = Modifier,
    showCamera: Boolean,
    onCameraReady: (CameraControl) -> Unit,
    onPreviewReady: (PreviewView) -> Unit,
    ) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    LaunchedEffect(Unit) { onPreviewReady(previewView) }
    LaunchedEffect(showCamera) {
        if (showCamera) try {
            val provider = ProcessCameraProvider.getInstance(context).get()
            val preview =
                Preview.Builder().build().apply { setSurfaceProvider(previewView.surfaceProvider) }
            val selector = CameraSelector.DEFAULT_BACK_CAMERA
            provider.unbindAll()
            val camera = provider.bindToLifecycle(lifecycleOwner, selector, preview)
            onCameraReady(camera.cameraControl)
        } catch (e: Exception) {
            Toast.makeText(context, "Error cámara: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    AndroidView(factory = { previewView }, modifier = modifier)
}
private fun getGridLetter(index: Int): String {
    var n = index
    var res = ""
    while (n >= 0) {
        res = (('A'.code + (n % 26)).toChar()) + res
        n = (n / 26) - 1
    }
    return res
}
@Composable
fun GridOverlay(type: String, gridSpacing: Float, modifier: Modifier = Modifier) {
    val gridColor = Color.Black.copy(alpha = 0.5f)
    val strokeWidth = 1.dp
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(
        color = gridColor.copy(alpha = 0.8f),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    )
    val safeSpacing = gridSpacing.coerceAtLeast(20f)
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        when (type) {
            "GRID" -> {
                var x = safeSpacing
                var col = 0
                while (x < width) {
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = strokeWidth.toPx()
                    )
                    val textPosX = x + 4.dp.toPx()
                    if (textPosX < width - 10.dp.toPx()) {
                        drawText(
                            textMeasurer = textMeasurer,
                            text = getGridLetter(col),
                            topLeft = Offset(textPosX, 4.dp.toPx()),
                            style = textStyle,
                            size = androidx.compose.ui.geometry.Size(100f, 100f) 
                        )
                    }
                    x += safeSpacing
                    col++
                }
                var y = safeSpacing
                var row = 1
                while (y < height) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = strokeWidth.toPx()
                    )
                    val textPosY = y + 4.dp.toPx()
                    if (textPosY < height - 10.dp.toPx()) {
                        drawText(
                            textMeasurer = textMeasurer,
                            text = row.toString(),
                            topLeft = Offset(4.dp.toPx(), textPosY),
                            style = textStyle,
                            size = androidx.compose.ui.geometry.Size(100f, 100f) 
                        )
                    }
                    y += safeSpacing
                    row++
                }
            }
            "TRIANGLES" -> {
                var x = -height
                while (x < width) {
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x + height, height),
                        strokeWidth = strokeWidth.toPx()
                    )
                    x += safeSpacing
                }
                var x2 = 0f
                while (x2 < width + height) {
                    drawLine(
                        color = gridColor,
                        start = Offset(x2, 0f),
                        end = Offset(x2 - height, height),
                        strokeWidth = strokeWidth.toPx()
                    )
                    x2 += safeSpacing
                }
            }
            "CIRCLES" -> {
                val center = Offset(width / 2f, height / 2f)
                val maxDimension = kotlin.math.max(width, height)
                var radius = safeSpacing
                var index = 1
                while (radius < maxDimension) {
                    val alpha = if (index % 5 == 0) 0.6f else 0.3f 
                    drawCircle(
                        color = gridColor.copy(alpha = alpha),
                        radius = radius,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = (if (index % 5 == 0) strokeWidth * 1.5f else strokeWidth).toPx()
                        )
                    )
                    radius += safeSpacing
                    index++
                }
                val radialLines = when {
                    safeSpacing < 100 -> 16
                    safeSpacing < 200 -> 12
                    else -> 8
                }
                for (i in 0 until radialLines) {
                    val angle = (i * 360f / radialLines) * (kotlin.math.PI / 180f).toFloat()
                    val lineEnd = Offset(
                        center.x + kotlin.math.cos(angle) * maxDimension,
                        center.y + kotlin.math.sin(angle) * maxDimension
                    )
                    drawLine(
                        color = gridColor.copy(alpha = 0.3f),
                        start = center,
                        end = lineEnd,
                        strokeWidth = strokeWidth.toPx()
                    )
                }
                val crossSize = 20.dp.toPx()
                drawLine(
                    color = gridColor,
                    start = Offset(center.x - crossSize, center.y),
                    end = Offset(center.x + crossSize, center.y),
                    strokeWidth = (strokeWidth * 2).toPx()
                )
                drawLine(
                    color = gridColor,
                    start = Offset(center.x, center.y - crossSize),
                    end = Offset(center.x, center.y + crossSize),
                    strokeWidth = (strokeWidth * 2).toPx()
                )
            }
        }
    }
}
@Composable
fun ColorMixingDialog(onDismiss: () -> Unit, activeColor: Color) {
    var selectedCategory by remember { mutableStateOf("BASICS") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Palette, null, tint = activeColor)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.guia_de_mezcla_profesional), fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryChip("BASICS", stringResource(R.string.basicos), selectedCategory == "BASICS") { selectedCategory = it }
                    CategoryChip("SKIN", stringResource(R.string.piel), selectedCategory == "SKIN") { selectedCategory = it }
                    CategoryChip("NATURE", stringResource(R.string.naturaleza), selectedCategory == "NATURE") { selectedCategory = it }
                }
                Spacer(Modifier.height(16.dp))
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (selectedCategory) {
                        "BASICS" -> {
                            MixingRecipeItem(stringResource(R.string.naranja_vivo), listOf("${stringResource(R.string.rojo)} 50%", "${stringResource(R.string.amarillo)} 50%"), Color(0xFFFF5722))
                            MixingRecipeItem(stringResource(R.string.violeta_real), listOf("${stringResource(R.string.azul)} 60%", "${stringResource(R.string.rojo)} 40%"), Color(0xFF673AB7))
                            MixingRecipeItem(stringResource(R.string.gris_neutro), listOf("${stringResource(R.string.negro)} 10%", "${stringResource(R.string.blanco)} 90%"), Color.Gray)
                        }
                        "SKIN" -> {
                            MixingRecipeItem(stringResource(R.string.piel_clara), listOf("${stringResource(R.string.blanco)} 80%", "${stringResource(R.string.rojo)} 10%", "${stringResource(R.string.amarillo)} 10%"), Color(0xFFFFCCBC))
                            MixingRecipeItem(stringResource(R.string.sombra_piel), listOf(stringResource(R.string.piel), "${stringResource(R.string.marron)} 20%", "${stringResource(R.string.azul)} 5%"), Color(0xFF8D6E63))
                        }
                        "NATURE" -> {
                            MixingRecipeItem(stringResource(R.string.verde_bosque), listOf("${stringResource(R.string.azul)} 50%", "${stringResource(R.string.amarillo)} 40%", "${stringResource(R.string.negro)} 10%"), Color(0xFF1B5E20))
                            MixingRecipeItem(stringResource(R.string.tierra), listOf("${stringResource(R.string.rojo)} 40%", "${stringResource(R.string.amarillo)} 40%", "${stringResource(R.string.azul)} 20%"), Color(0xFF5D4037))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cerrar).uppercase(), color = activeColor, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp)
    )
}
@Composable
fun CategoryChip(id: String, label: String, isSelected: Boolean, onClick: (String) -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick(id) },
        color = if (isSelected) Color(0xFFFF4081) else Color(0xFFF5F5F5),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
@Composable
fun MixingRecipeItem(name: String, ingredients: List<String>, previewColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(previewColor).border(1.dp, Color.LightGray, CircleShape))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(ingredients.joinToString(" + "), fontSize = 12.sp, color = Color.Gray)
        }
    }
}
