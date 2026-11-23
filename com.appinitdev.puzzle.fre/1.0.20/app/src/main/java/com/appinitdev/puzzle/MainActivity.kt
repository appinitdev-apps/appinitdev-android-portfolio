
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SoundEffectsManager.init(this)

        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            val db = remember {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "puzzle-db"
                )
                    .addCallback(PREPOPULATE_CALLBACK)
                    .fallbackToDestructiveMigration()
                    .build()
            }

            var showOnboarding by remember {
                mutableStateOf(!sharedPrefs.getBoolean("onboarding_seen", false))
            }
            var showSettings by remember { mutableStateOf(false) }
            var showProfile by remember { mutableStateOf(false) }
            var showDrawingCanvas by remember { mutableStateOf(false) }
            var showAIGenerator by remember { mutableStateOf(false) }
            var appSettings by remember { mutableStateOf(loadAppSettings(context)) }

            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    val isComplete = sharedPrefs.getBoolean("initial_download_complete", false)

                    if (!isComplete) {
                        val success = downloadInitialAssets(this@MainActivity, db)

                        if (success) {
                            sharedPrefs.edit().putBoolean("initial_download_complete", true).apply()
                            Log.d("Download", "Descarga inicial completada con éxito")
                        } else {
                            Log.d(
                                "Download",
                                "Sin conexión o error. Se reintentará en el próximo inicio."
                            )
                        }
                    }
                }
            }

            val puzzleDao = db.puzzleDao()
            val savedPuzzles by puzzleDao.getAllPuzzles().collectAsState(initial = emptyList())

            var currentScreen by remember { mutableStateOf(Screen.Gallery) }
            var selectedPuzzleId by remember { mutableIntStateOf(-1) }
            var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
            var showPixabay by remember { mutableStateOf(false) }
            var backPressedTime by remember { mutableLongStateOf(0L) }

            if (currentScreen != Screen.Gallery) {
                BackHandler {
                    currentScreen = Screen.Gallery
                }
            } else {
                BackHandler {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - backPressedTime < 2000) {
                        (context as? android.app.Activity)?.finish()
                    } else {
                        Toast.makeText(
                            context,
                            getString(R.string.presiona_otra_vez_para_salir),
                            Toast.LENGTH_SHORT
                        ).show()
                        backPressedTime = currentTime
                    }
                }
            }

            PuzzleTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (appSettings.customBackgroundPath != null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = File(appSettings.customBackgroundPath!!),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Capa de atenuación degradada (Scrim) para garantizar alto contraste y legibilidad
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.60f),
                                                Color.Black.copy(alpha = 0.45f),
                                                Color.Black.copy(alpha = 0.75f)
                                            )
                                        )
                                    )
                            )
                        }
                    }

                    if (showOnboarding) {
                        OnboardingDialog(
                            onDismiss = {
                                showOnboarding = false
                                sharedPrefs.edit().putBoolean("onboarding_seen", true).apply()
                            }
                        )
                    }

                    if (showSettings) {
                        SettingsDialog(
                            onDismiss = { showSettings = false },
                            onSettingsChanged = { updated ->
                                appSettings = updated
                            }
                        )
                    }

                    if (showProfile) {
                        val allStats by puzzleDao.getStatsForPuzzle(-1).collectAsState(initial = emptyList())
                        PlayerProfileDialog(
                            stats = allStats,
                            onDismiss = { showProfile = false }
                        )
                    }

                    if (showDrawingCanvas) {
                        DrawingCanvasDialog(
                            onDismiss = { showDrawingCanvas = false },
                            onDrawingFinished = { drawnBitmap ->
                                scope.launch(Dispatchers.IO) {
                                    val path = saveBitmapToInternalStorage(context, drawnBitmap)
                                    val newId = puzzleDao.insert(
                                        PuzzleEntity(imagePath = path, isCustomImage = true, category = "Mis Fotos")
                                    )

                                    selectedPuzzleId = newId.toInt()
                                    selectedBitmap = drawnBitmap
                                    currentScreen = Screen.ImagePuzzle
                                }
                            }
                        )
                    }

                    if (showAIGenerator) {
                        AIPuzzleGeneratorDialog(
                            onDismiss = { showAIGenerator = false },
                            onImageGenerated = { aiBitmap ->
                                scope.launch(Dispatchers.IO) {
                                    val path = saveBitmapToInternalStorage(context, aiBitmap)
                                    val newId = puzzleDao.insert(
                                        PuzzleEntity(imagePath = path, isCustomImage = true, category = "IA Generada")
                                    )

                                    selectedPuzzleId = newId.toInt()
                                    selectedBitmap = aiBitmap
                                    currentScreen = Screen.ImagePuzzle
                                }
                            }
                        )
                    }

                    val cropLauncher =
                        rememberLauncherForActivityResult(CropImageContract()) { result ->
                            if (result.isSuccessful) {
                                result.uriContent?.let { uri ->
                                    val bitmap = if (Build.VERSION.SDK_INT < 28) {
                                        @Suppress("DEPRECATION")
                                        android.provider.MediaStore.Images.Media.getBitmap(
                                            context.contentResolver,
                                            uri
                                        )
                                    } else {
                                        val source =
                                            ImageDecoder.createSource(context.contentResolver, uri)
                                        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                                            decoder.isMutableRequired = true
                                        }
                                    }

                                    scope.launch(Dispatchers.IO) {
                                        val path = saveBitmapToInternalStorage(context, bitmap)
                                        val newId = puzzleDao.insert(
                                            PuzzleEntity(imagePath = path, isCustomImage = true, category = "Mis Fotos")
                                        )

                                        selectedPuzzleId = newId.toInt()
                                        selectedBitmap = BitmapFactory.decodeFile(path)
                                        currentScreen = Screen.ImagePuzzle
                                    }
                                }
                            }
                        }

                    val galleryLauncher =
                        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                            uri?.let {
                                val options = CropImageContractOptions(
                                    it, CropImageOptions(
                                        fixAspectRatio = true, aspectRatioX = 1, aspectRatioY = 1,
                                        guidelines = CropImageView.Guidelines.ON
                                    )
                                )
                                cropLauncher.launch(options)
                            }
                        }

                    val cameraLauncher =
                        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { photoBitmap ->
                            photoBitmap?.let { bitmap ->
                                scope.launch(Dispatchers.IO) {
                                    val path = saveBitmapToInternalStorage(context, bitmap)
                                    val newId = puzzleDao.insert(
                                        PuzzleEntity(imagePath = path, isCustomImage = true, category = "Mis Fotos")
                                    )

                                    selectedPuzzleId = newId.toInt()
                                    selectedBitmap = bitmap
                                    currentScreen = Screen.ImagePuzzle
                                }
                            }
                        }

                    if (showPixabay) {
                        PixabaySearchDialog(
                            onDismiss = { showPixabay = false },
                            onImageSelected = { bitmap ->
                                showPixabay = false
                                scope.launch(Dispatchers.IO) {
                                    val path = saveBitmapToInternalStorage(context, bitmap)
                                    val newId = puzzleDao.insert(
                                        PuzzleEntity(imagePath = path, isCustomImage = true, category = "Naturaleza")
                                    )

                                    selectedPuzzleId = newId.toInt()
                                    selectedBitmap = BitmapFactory.decodeFile(path)
                                    currentScreen = Screen.ImagePuzzle
                                }
                            }
                        )
                    }

                    Scaffold(
                        containerColor = if (appSettings.customBackgroundPath != null) Color.Transparent else MaterialTheme.colorScheme.background
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (currentScreen) {
                                Screen.Gallery -> {
                                    GalleryScreen(
                                        puzzles = savedPuzzles,
                                        onSelect = { path ->
                                            val puzzle = savedPuzzles.find { it.imagePath == path }
                                            selectedPuzzleId = puzzle?.id ?: -1
                                            selectedBitmap = BitmapFactory.decodeFile(path)
                                            currentScreen = Screen.ImagePuzzle
                                        },
                                        onAddClick = { galleryLauncher.launch("image/*") },
                                        onCameraClick = { cameraLauncher.launch(null) },
                                        onPixabayClick = { showPixabay = true },
                                        onNumberModeClick = { currentScreen = Screen.NumberPuzzle },
                                        onOpenDrawingCanvas = { showDrawingCanvas = true },
                                        onOpenAIGenerator = { showAIGenerator = true },
                                        onOpenTutorial = { showOnboarding = true },
                                        onOpenSettings = { showSettings = true },
                                        onOpenProfile = { showProfile = true },
                                        db = db
                                    )
                                }

                                Screen.ImagePuzzle -> {
                                    val statsHistory by puzzleDao.getStatsForPuzzle(selectedPuzzleId)
                                        .collectAsState(initial = emptyList())

                                    Column {
                                        IconButton(onClick = { currentScreen = Screen.Gallery }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Regresar",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        if (selectedBitmap != null) {
                                            PuzzleApp(
                                                originalBitmap = selectedBitmap!!,
                                                statsHistory = statsHistory,
                                                puzzleId = selectedPuzzleId,
                                                onVictory = { moves, time, n ->
                                                    PlayerProfileManager.addXp(context, n * 50)
                                                    scope.launch(Dispatchers.IO) {
                                                        puzzleDao.insertStat(
                                                            PuzzleStatEntity(
                                                                puzzleId = selectedPuzzleId,
                                                                n = n, moves = moves, time = time
                                                            )
                                                        )
                                                    }
                                                },
                                                onCheckProgress = { currentN ->
                                                    puzzleDao.getProgress(selectedPuzzleId, currentN)
                                                },
                                                onSaveProgress = { currentN, moves, time, piecesList ->
                                                    val stateString =
                                                        piecesList.joinToString(",") { it.id.toString() }
                                                    scope.launch(Dispatchers.IO) {
                                                        puzzleDao.insertOrUpdateProgress(
                                                            PuzzleProgressEntity(
                                                                puzzleId = selectedPuzzleId,
                                                                n = currentN, moves = moves,
                                                                time = time, state = stateString
                                                            )
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }

                                Screen.NumberPuzzle -> {
                                    Column {
                                        IconButton(onClick = { currentScreen = Screen.Gallery }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Regresar",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        NumberPuzzleScreen(puzzleDao = puzzleDao)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundEffectsManager.release()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GalleryScreen(
    puzzles: List<PuzzleEntity>,
    onSelect: (String) -> Unit,
    onAddClick: () -> Unit,
    onCameraClick: () -> Unit,
    db: AppDatabase,
    onPixabayClick: () -> Unit,
    onNumberModeClick: () -> Unit,
    onOpenDrawingCanvas: () -> Unit,
    onOpenAIGenerator: () -> Unit,
    onOpenTutorial: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var showSecretChallengeInput by remember { mutableStateOf(false) }

    var selectedIds by rememberSaveable { mutableStateOf(setOf<Int>()) }
    val isSelectionMode by remember { derivedStateOf { selectedIds.isNotEmpty() } }

    var selectedCategory by rememberSaveable { mutableStateOf("Todas") }
    val categories = listOf("Todas", "Mis Fotos", "Naturaleza", "Arte", "IA Generada")

    val customPuzzles = remember(puzzles) { puzzles.filter { it.isCustomImage } }

    val filteredPuzzles = remember(customPuzzles, selectedCategory) {
        if (selectedCategory == "Todas") {
            customPuzzles
        } else {
            customPuzzles.filter { it.category == selectedCategory }
        }
    }

    if (showSecretChallengeInput) {
        SecretMessageInputDialog(
            initialMessage = "",
            onDismiss = { showSecretChallengeInput = false },
            onMessageSaved = {
                showSecretChallengeInput = false
                onNumberModeClick()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isSelectionMode) "${selectedIds.size} " + stringResource(R.string.seleccionados) else stringResource(
                    R.string.mis_rompecabezas
                ),
                style = MaterialTheme.typography.headlineSmall
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenProfile) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "Perfil",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onOpenSettings) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Ajustes",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onOpenTutorial) {
                    Icon(
                        Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = "Tutorial",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                if (isSelectionMode) {
                    IconButton(onClick = {
                        scope.launch(Dispatchers.IO) {
                            val toDelete = customPuzzles.filter { it.id in selectedIds }
                            toDelete.forEach { puzzle ->
                                val file = File(puzzle.imagePath)
                                if (file.exists()) file.delete()
                            }
                            db.puzzleDao().deleteMultiple(selectedIds.toList())
                            selectedIds = emptySet()
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color.Red)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            lazyListItems(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category, fontWeight = FontWeight.Medium) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                GalleryActionButton(
                    label = stringResource(R.string.generar_por_ia),
                    icon = Icons.Default.AutoAwesome,
                    enabled = !isSelectionMode,
                    onClick = onOpenAIGenerator
                )
            }

            item {
                GalleryActionButton(
                    label = stringResource(R.string.tomar_foto),
                    icon = Icons.Default.CameraAlt,
                    enabled = !isSelectionMode,
                    onClick = onCameraClick
                )
            }

            item {
                GalleryActionButton(
                    label = stringResource(R.string.nueva_imagen),
                    icon = Icons.Default.Add,
                    enabled = !isSelectionMode,
                    onClick = onAddClick
                )
            }

            item {
                GalleryActionButton(
                    label = stringResource(R.string.pinta_tu_puzzle),
                    icon = Icons.Default.Brush,
                    enabled = !isSelectionMode,
                    onClick = onOpenDrawingCanvas
                )
            }

            item {
                GalleryActionButton(
                    label = stringResource(R.string.desde_la_red),
                    icon = Icons.Default.Search,
                    enabled = !isSelectionMode,
                    onClick = onPixabayClick
                )
            }

            item {
                GalleryActionButton(
                    label = stringResource(R.string.modo_n_meros),
                    icon = Icons.Default.List,
                    enabled = !isSelectionMode,
                    onClick = onNumberModeClick
                )
            }

            item {
                GalleryActionButton(
                    label = stringResource(R.string.desafio_secreto),
                    icon = Icons.Default.CardGiftcard,
                    enabled = !isSelectionMode,
                    onClick = { showSecretChallengeInput = true }
                )
            }

            items(filteredPuzzles, key = { it.id }) { puzzle ->
                val isSelected = selectedIds.contains(puzzle.id)

                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .combinedClickable(
                            onClick = {
                                if (isSelectionMode) {
                                    selectedIds =
                                        if (isSelected) selectedIds - puzzle.id else selectedIds + puzzle.id
                                } else {
                                    onSelect(puzzle.imagePath)
                                }
                            },
                            onLongClick = {
                                if (!isSelectionMode) selectedIds = setOf(puzzle.id)
                            }
                        ),
                    border = if (isSelected) BorderStroke(
                        3.dp,
                        MaterialTheme.colorScheme.primary
                    ) else null
                ) {
                    Box(Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = File(puzzle.imagePath),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(if (isSelected) 0.5f else 1f)
                        )

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 1.dp.toPx()
                            val color = Color.White.copy(alpha = 0.7f)

                            drawLine(
                                color = color,
                                start = Offset(size.width / 3, 0f),
                                end = Offset(size.width / 3, size.height),
                                strokeWidth = strokeWidth
                            )
                            drawLine(
                                color = color,
                                start = Offset(2 * size.width / 3, 0f),
                                end = Offset(2 * size.width / 3, size.height),
                                strokeWidth = strokeWidth
                            )

                            drawLine(
                                color = color,
                                start = Offset(0f, size.height / 3),
                                end = Offset(size.width, size.height / 3),
                                strokeWidth = strokeWidth
                            )
                            drawLine(
                                color = color,
                                start = Offset(0f, 2 * size.height / 3),
                                end = Offset(size.width, 2 * size.height / 3),
                                strokeWidth = strokeWidth
                            )
                        }

                        if (isSelected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    BackHandler(enabled = isSelectionMode) {
        selectedIds = emptySet()
    }
}

suspend fun downloadUnsplashImage(context: Context, url: String): Bitmap? {
    val loader = coil.ImageLoader(context)
    val request = coil.request.ImageRequest.Builder(context)
        .data(url)
        .allowHardware(false)
        .build()

    val result = loader.execute(request)

    return when (result) {
        is coil.request.SuccessResult -> {
            (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
        }

        is coil.request.ErrorResult -> {
            Log.e("DownloadError", "Error al descargar: ${result.throwable.message}")
            result.throwable.printStackTrace()
            null
        }
    }
}

suspend fun downloadInitialAssets(context: Context, database: AppDatabase): Boolean {
    return try {
        val response = RetrofitClient.service.searchPhotos(
            apiKey = com.appinitdev.puzzle.red.PixabayConfig.API_KEY,
            query = "nature",
            imageType = "photo",
            perPage = 9
        )

        if (response.hits.isNotEmpty()) {
            response.hits.forEach { photo ->
                val bitmap = downloadUnsplashImage(context, photo.largeImageURL)
                bitmap?.let {
                    val path = saveBitmapToInternalStorage(context, it)
                    database.puzzleDao().insert(
                        PuzzleEntity(imagePath = path, isCustomImage = true, category = "Naturaleza")
                    )
                }
            }
            true
        } else false
    } catch (e: Exception) {
        Log.e("Download", "Error en descarga inicial: ${e.message}")
        false
    }
}

@Composable
fun GalleryActionButton(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray
    val containerColor = if (enabled) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    OutlinedCard(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(8.dp)
                    .alpha(if (enabled) 1f else 0.5f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = contentColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

enum class Screen { Gallery, ImagePuzzle, NumberPuzzle }
