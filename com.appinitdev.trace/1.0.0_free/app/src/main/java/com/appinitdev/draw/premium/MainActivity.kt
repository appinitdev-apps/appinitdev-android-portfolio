
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            //Estado de navegación simple
            // -1 = Pantalla de selección de modo, true = Abre Cámara encendida, false = Cámara apagada
            var cameraStartState by rememberSaveable { mutableStateOf<Boolean?>(true) }
            var startWithGrid by rememberSaveable { mutableStateOf(false) } // 👈 NUEVO: Estado para iniciar con malla
            var startWithSketch by rememberSaveable { mutableStateOf(false) } // 👈 NUEVO: Estado para iniciar con boceto
            var currentStep by rememberSaveable { mutableIntStateOf(0) }
            var selectedImageUriStr by rememberSaveable { mutableStateOf<String?>(null) }
            val selectedImageUri = selectedImageUriStr?.let { Uri.parse(it) }
            var showExitCanvasDialog by rememberSaveable { mutableStateOf(false)}
            // Envuelve toda tu UI
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
                            // Si el segundo toque ocurre dentro de los 2 segundos, cierra la app
                            this@MainActivity.finish()
                        } else {
                            // Primer toque: Guardamos el tiempo actual y mandamos el aviso tipo Toast
                            lastBackPressTime = currentTime
                            Toast.makeText(context,
                                getString(R.string.presiona_una_vez_m_s_para_salir), Toast.LENGTH_SHORT).show()
                        }
                        // Si está en el Home (0), pregunta si desea salir
                        //showExitDialog = true
                    }

                    1 -> {
                        // Si está en Selección de modo (1), regresa al Home (0)
                        currentStep = 0
                    }

                    2 -> {
                        // Si está en el Lienzo (2), regresa al Home (0)

                        showExitCanvasDialog = true
                    }

                    3 -> {
                        // Si está en MyGalleryScreen (3), regresa al Home (0)
                        currentStep = 0
                    }
                }
            }

//            if (showExitDialog) {
//                AlertDialog(
//                    onDismissRequest = { showExitDialog = false },
//                    title = { Text(text = "Salir de la aplicación") },
//                    text = { Text(text = "¿Estás seguro de que deseas salir?") },
//                    confirmButton = {
//                        TextButton(onClick = {
//                            // 🔑 SOLUCIÓN: Usamos "this@MainActivity" para cerrar la app de forma segura
//                            this@MainActivity.finish()
//                        }) {
//                            Text(text = "Salir")
//                        }
//                    },
//                    dismissButton = {
//                        TextButton(onClick = { showExitDialog = false }) {
//                            Text(text = "Cancelar")
//                        }
//                    }
//                )
//            }

            if (showExitCanvasDialog) {
                AlertDialog(
                    onDismissRequest = { showExitCanvasDialog = false },
                    title = { Text(text = stringResource(R.string.terminaste_de_dibujar)) },
                    text = { Text(text = stringResource(R.string.si_sales_ahora_perder_s_los_ajustes_actuales_de_escala_rotaci_n_y_filtros_de_la_plantilla)) },
                    confirmButton = {
                        TextButton(onClick = {
                            showExitCanvasDialog = false
                            currentStep = 0 // Confirma el regreso al selector de modo
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
                    // 1. Iniciamos mostrando la pantalla de selección Pro
                    0 -> {


                            HomeScreen(
                                onImageSelected = { uri ->
                                    // Guardar imagen en la base de datos si es externa
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
                                                // Si se guardó con éxito, intentamos obtener la ruta local para que filtros como "Eliminar Fondo" funcionen
                                                scope.launch(Dispatchers.IO) {
                                                    val db = com.appinitdev.draw.premium.BD.GalleryDatabase.getDatabase(context)
                                                    val latest = db.galleryDao().getLatestImage()
                                                    withContext(Dispatchers.Main) {
                                                        selectedImageUriStr = (latest?.localPath?.let { Uri.fromFile(java.io.File(it)) } ?: uri).toString()
                                                        currentStep = 1 // 👈 CAMBIO: Ahora va a la selección de modo (1) en lugar del lienzo (2)
                                                    }
                                                }
                                            } else {
                                                selectedImageUriStr = uri.toString()
                                                currentStep = 1 // 👈 CAMBIO: Ahora va a la selección de modo (1)
                                            }
                                        }
                                    }
                                },
                                viewModel = mViewModel,
                                onNavigateToGallery = {
                                    currentStep = 3 // Abre el nuevo álbum de la App
                                }
                            )

                    }

                    1 -> {
                        DrawingModeScreen(
                            onModeSelected = { cameraOn, useGrid, useSketch ->
                                cameraStartState = cameraOn // Cambia el estado al dar Continuar
                                startWithGrid = useGrid // 👈 Captura si debe iniciar con malla
                                startWithSketch = useSketch // 👈 Captura si debe iniciar con boceto
                                currentStep = 2
                            },
                            onBack = {
                                // 👈 MEJORA: Si viene de seleccionar imagen, regresa al Home. 
                                // Si ya estaba dibujando y cambió de modo, regresa al lienzo.
                                currentStep = 0
                                // Nota: Forzamos 0 para que siempre regrese al Home si cancela la selección de modo inicial.
                            }
                        )
                    }

                    2 -> {
                        // 2. Cargamos el lienzo con la configuración elegida por el usuario
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
                                initialGridType = if (startWithGrid) "GRID" else "NONE", // 👈 Pasa el modo rejilla
                                initialFilter = if (startWithSketch) "SKETCH" else "NONE", // 👈 Pasa el modo boceto
                                initialTab = if (startWithSketch) 4 else -1, // 👈 ABRE FILTROS SI ES BOCETO
                                onBack = {
                                    showExitCanvasDialog = true // 👈 Ahora muestra el diálogo al pulsar la flecha superior
                                },
                                onChangeMode = {
                                    currentStep = 1 // Abre la selección de modo
                                }
                            )
                        }
                    }

                    3 -> {
                        MyGalleryScreen(
                            onImageSelectedForDrawing = { uri ->
                                selectedImageUriStr = uri.toString()
                                currentStep = 1 // 👈 CAMBIO: Ahora va a la selección de modo (1) en lugar del lienzo (2)
                            },
                            onBack = {
                                currentStep = 0 // Regresa al Home
                            }
                        )
                    }

                }
            }
        }

    }
}