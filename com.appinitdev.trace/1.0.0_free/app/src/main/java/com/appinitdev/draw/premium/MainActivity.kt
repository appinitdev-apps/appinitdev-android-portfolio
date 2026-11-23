class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            var cameraStartState by rememberSaveable { mutableStateOf<Boolean?>(true) }
            var startWithGrid by rememberSaveable { mutableStateOf(false) } 
            var startWithSketch by rememberSaveable { mutableStateOf(false) } 
            var currentStep by rememberSaveable { mutableIntStateOf(0) }
            var selectedImageUriStr by rememberSaveable { mutableStateOf<String?>(null) }
            val selectedImageUri = selectedImageUriStr?.let { Uri.parse(it) }
            var showExitCanvasDialog by rememberSaveable { mutableStateOf(false)}
            var lastBackPressTime by remember { mutableStateOf(0L) }
            var showExitDialog by remember { mutableStateOf(false) }
            val mViewModel: PackViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PackViewModel(applicationContext) as T
                }
            })
            BackHandler(enabled = true) {
                when (currentStep) {
                    0 -> {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastBackPressTime < 2000) {
                            this@MainActivity.finish()
                        } else {
                            lastBackPressTime = currentTime
                            Toast.makeText(context,
                                getString(R.string.presiona_una_vez_m_s_para_salir), Toast.LENGTH_SHORT).show()
                        }
                    }
                    1 -> {
                        currentStep = 0
                    }
                    2 -> {
                        showExitCanvasDialog = true
                    }
                    3 -> {
                        currentStep = 0
                    }
                }
            }
            if (showExitCanvasDialog) {
                AlertDialog(
                    onDismissRequest = { showExitCanvasDialog = false },
                    title = { Text(text = stringResource(R.string.terminaste_de_dibujar)) },
                    text = { Text(text = stringResource(R.string.si_sales_ahora_perder_s_los_ajustes_actuales_de_escala_rotaci_n_y_filtros_de_la_plantilla)) },
                    confirmButton = {
                        TextButton(onClick = {
                            showExitCanvasDialog = false
                            currentStep = 0 
                        }) {
                            Text(text = stringResource(R.string.s_salir))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showExitCanvasDialog = false }) {
                            Text(text = stringResource(R.string.no_seguir_dibujando))
                        }
                    }
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                when (currentStep) {
                    0 -> {
                            HomeScreen(
                                onImageSelected = { uri ->
                                    scope.launch(Dispatchers.IO) {
                                        val success = if (uri.toString().startsWith("http")) {
                                            GalleryStorageManager.saveUrlToInternalStorage(
                                                context,
                                                uri.toString()
                                            )
                                        } else {
                                            GalleryStorageManager.saveImageToInternalStorage(
                                                context,
                                                uri
                                            )
                                        }
                                        withContext(Dispatchers.Main) {
                                            if (success) {
                                                scope.launch(Dispatchers.IO) {
                                                    val db = com.appinitdev.draw.premium.BD.GalleryDatabase.getDatabase(context)
                                                    val latest = db.galleryDao().getLatestImage()
                                                    withContext(Dispatchers.Main) {
                                                        selectedImageUriStr = (latest?.localPath?.let { Uri.fromFile(java.io.File(it)) } ?: uri).toString()
                                                        currentStep = 1 
                                                    }
                                                }
                                            } else {
                                                selectedImageUriStr = uri.toString()
                                                currentStep = 1 
                                            }
                                        }
                                    }
                                },
                                viewModel = mViewModel,
                                onNavigateToGallery = {
                                    currentStep = 3 
                                }
                            )
                    }
                    1 -> {
                        DrawingModeScreen(
                            onModeSelected = { cameraOn, useGrid, useSketch ->
                                cameraStartState = cameraOn 
                                startWithGrid = useGrid 
                                startWithSketch = useSketch 
                                currentStep = 2
                            },
                            onBack = {
                                currentStep = 0
                            }
                        )
                    }
                    2 -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    WindowInsets.safeDrawing
                                        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top + WindowInsetsSides.Bottom)
                                        .asPaddingValues()
                                )
                        ) {
                            CameraPaintScreen(
                                startWithCameraOn = cameraStartState == true,
                                initialImageUri = selectedImageUri,
                                initialGridType = if (startWithGrid) "GRID" else "NONE", 
                                initialFilter = if (startWithSketch) "SKETCH" else "NONE", 
                                initialTab = if (startWithSketch) 4 else -1, 
                                onBack = {
                                    showExitCanvasDialog = true 
                                },
                                onChangeMode = {
                                    currentStep = 1 
                                }
                            )
                        }
                    }
                    3 -> {
                        MyGalleryScreen(
                            onImageSelectedForDrawing = { uri ->
                                selectedImageUriStr = uri.toString()
                                currentStep = 1 
                            },
                            onBack = {
                                currentStep = 0 
                            }
                        )
                    }
                }
            }
        }
    }
}