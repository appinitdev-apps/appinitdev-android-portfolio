




fun obtenerDrawableMiniatura(themeName: String): Int {
    return when (themeName) {
        "x_anatomy" -> R.drawable.x_anatomy
        "x_anime"   -> R.drawable.x_anime
        "x_art"     -> R.drawable.x_art
        "x_car"     -> R.drawable.x_car
        "x_cosplay" -> R.drawable.x_cosplay
        "x_eye"     -> R.drawable.x_eye
        "x_flowers" -> R.drawable.x_flowers
        "x_manga"   -> R.drawable.x_manga
        "x_body"    -> R.drawable.x_body
        "x_food"    -> R.drawable.x_food
        "x_girl"    -> R.drawable.x_girl
        else        -> R.drawable.x_art 
    }
}




class PackDownloader(private val context: Context) {
    private val storage by lazy {
        try {
            FirebaseStorage.getInstance()
        } catch (e: IllegalStateException) {
            FirebaseApp.initializeApp(context)
            FirebaseStorage.getInstance()
        }
    }

    suspend fun downloadPack(packName: String, onProgress: (Int) -> Unit): Result<File> =
        withContext(Dispatchers.IO) {
            val tempFolder = File(context.filesDir, "packs_dibujo/${packName}_temp")
            val finalFolder = File(context.filesDir, "packs_dibujo/$packName")
            
            try {
                
                if (!tempFolder.exists()) { tempFolder.mkdirs() }

                val storageRef = storage.reference.child(packName)
                val listResult = storageRef.listAll().await()
                val totalFiles = listResult.items.size

                if (totalFiles == 0) {
                    tempFolder.deleteRecursively()
                    return@withContext Result.failure(Exception(context.getString(R.string.la_carpeta_remota_est_vac_a)))
                }

                
                listResult.items.forEachIndexed { index, storageFile ->
                    val localFile = File(tempFolder, storageFile.name)
                    
                    
                    if (!localFile.exists() || localFile.length() == 0L) {
                        storageFile.getFile(localFile).await()
                    }

                    val progress = ((index + 1) * 100) / totalFiles
                    withContext(Dispatchers.Main) { onProgress(progress) }
                }

                
                if (finalFolder.exists()) { finalFolder.deleteRecursively() }
                val success = tempFolder.renameTo(finalFolder)
                
                if (success) {
                    Result.success(finalFolder)
                } else {
                    Result.failure(Exception("Error al finalizar la descarga (Rename failed)"))
                }
            } catch (e: Exception) {
                tempFolder.deleteRecursively() 
                Result.failure(e)
            }
        }
}





class PackViewModel(context: Context) : ViewModel() {
    private val downloader = PackDownloader(context)

    
    private val _activeDownloads = MutableStateFlow<Map<String, Int>>(emptyMap())
    val activeDownloads: StateFlow<Map<String, Int>> = _activeDownloads

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    val showDownloadedModal = MutableStateFlow<String?>(null)
    val downloadedPacks = MutableStateFlow<List<String>>(emptyList())

    init { updateDownloadedPacksList(context) }

    fun updateDownloadedPacksList(context: Context) {
        val rootFolder = File(context.filesDir, "packs_dibujo")
        if (rootFolder.exists()) {
            val list = rootFolder.listFiles()
                ?.filter { it.isDirectory && !it.name.endsWith("_temp") && (it.listFiles()?.size ?: 0) > 0 }
                ?.map { it.name } ?: emptyList()
            downloadedPacks.value = list
        }
    }

    fun onPackClick(context: Context, packName: String) {
        
        if (_activeDownloads.value.containsKey(packName)) return

        val localPackFolder = File(context.filesDir, "packs_dibujo/$packName")
        val filesCount = localPackFolder.listFiles()?.size ?: 0

        if (localPackFolder.exists() && filesCount > 0) {
            showDownloadedModal.value = localPackFolder.absolutePath
        } else {
            startPackDownload(context, packName)
        }
    }

    private fun startPackDownload(context: Context, packName: String) {
        viewModelScope.launch(Dispatchers.Main) {
            
            _activeDownloads.value += (packName to 0)
            
            val resultado = downloader.downloadPack(packName) { progress ->
                _activeDownloads.value += (packName to progress)
            }
            
            resultado.onSuccess { folder ->
                _activeDownloads.value -= packName 
                updateDownloadedPacksList(context)
                showDownloadedModal.value = folder.absolutePath
            }.onFailure { error ->
                _activeDownloads.value -= packName
                _errorState.value = error.localizedMessage ?: context.getString(R.string.error)
            }
        }
    }

    fun clearError() {
        _errorState.value = null
    }

    fun closeModal() {
        showDownloadedModal.value = null
    }
}




@Composable
fun PackManagerScreen(
    viewModel: PackViewModel,
    onImageSelected: (android.net.Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listaDeTemasBase = remember {
        listOf("x_anatomy", "x_anime", "x_art", "x_car", "x_cosplay", "x_eye", "x_flowers", "x_manga", "x_body", "x_food", "x_girl")
    }

    val activeDownloads by viewModel.activeDownloads.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    val modalFolderPath by viewModel.showDownloadedModal.collectAsState()
    val downloadedPacks by viewModel.downloadedPacks.collectAsState()

    
    LaunchedEffect(errorState) {
        errorState?.let {
            
            viewModel.clearError()
        }
    }

    val yaDescargados = listaDeTemasBase.filter { downloadedPacks.contains(it) }
    val noDescargados = listaDeTemasBase.filter { !downloadedPacks.contains(it) }

    Box(modifier = modifier.wrapContentHeight()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            
            if (yaDescargados.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.tus_paquetes_guardados),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(yaDescargados) { tema ->
                            PackHorizontalCard(
                                themeName = tema,
                                onClick = { viewModel.onPackClick(context, tema) }
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }
            }

            
            Text(
                text = if (noDescargados.isEmpty()) stringResource(R.string.todo_el_cat_logo_est_listo) else stringResource(
                    R.string.cat_logo_disponible_para_descargar
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )


            if (noDescargados.isEmpty() && yaDescargados.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.tienes_todos_los_paquetes_disponibles_guardados_localmente),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            } else {
                noDescargados.forEach { tema ->
                    PackItemRowM3(
                        themeName = tema,
                        progress = activeDownloads[tema],
                        onClick = { viewModel.onPackClick(context, tema) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        
        modalFolderPath?.let { path ->
            val carpeta = File(path)
            val imagenesLocales = carpeta.listFiles()?.toList() ?: emptyList()

            AlertDialog(
                onDismissRequest = { viewModel.closeModal() },
                
                properties = DialogProperties(
                    usePlatformDefaultWidth = false
                ),
                icon = {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = {
                    Text(
                        text = carpeta.name.replace("x_", "").replace("_", " ").uppercase(),
                        style = MaterialTheme.typography.headlineSmall, 
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.im_genes_listas_en_el_almacenamiento_local),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            
                            modifier = Modifier.weight(1f, fill = false),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(imagenesLocales) { archivoImagen ->
                                AsyncImage(
                                    model = archivoImagen,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp)) 
                                        .clickable {
                                            onImageSelected(archivoImagen.toUri())
                                            viewModel.closeModal()
                                        },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    
                    TextButton(
                        onClick = { viewModel.closeModal() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.ok),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                
                shape = RoundedCornerShape(28.dp), 
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh, 
                tonalElevation = 6.dp 
            )
        }
    }
}


@Composable
fun PackHorizontalCard(themeName: String, onClick: () -> Unit) {
    val nombreEstetico = themeName.replace("x_", "").replace("_", " ").uppercase()
    val drawableResId = remember(themeName) { obtenerDrawableMiniatura(themeName) }

    Card(
        modifier = Modifier
            .width(150.dp)
            .height(125.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            AsyncImage(
                model = drawableResId,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.55f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = nombreEstetico,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.listo_offline),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}


@Composable
fun PackItemRowM3(themeName: String, progress: Int?, onClick: () -> Unit) {
    val nombreEstetico = themeName.replace("x_", "").replace("_", " ").uppercase()
    val drawableResId = remember(themeName) { obtenerDrawableMiniatura(themeName) }
    val isDownloading = progress != null

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(enabled = !isDownloading) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = {
                    Text(nombreEstetico, fontWeight = FontWeight.SemiBold)
                },
                supportingContent = {
                    Text(if (isDownloading) stringResource(R.string.descargando) else stringResource(R.string.paquete_remoto_firebase_cloud))
                },
                leadingContent = {
                    
                    AsyncImage(
                        model = drawableResId,
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                },
                trailingContent = {
                    if (isDownloading) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(28.dp)) {
                            CircularProgressIndicator(strokeWidth = 3.dp, modifier = Modifier.fillMaxSize())
                        }
                    } else {
                        Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )

            
            AnimatedVisibility(visible = isDownloading) {
                if (progress != null) {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)) {
                        LinearProgressIndicator(
                            progress = progress / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            Text(
                                text = "$progress%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}