@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyGalleryScreen(
    onImageSelectedForDrawing: (Uri) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { GalleryDatabase.getDatabase(context) }
    val imagesList by database.galleryDao().getAllImages().collectAsState(initial = emptyList())
    val capturedWorksFull by database.galleryDao().getAllCapturedWorks().collectAsState(initial = emptyList())
    val capturedWorks = remember(capturedWorksFull) {
        capturedWorksFull.filter { !it.isTimeLapse || it.sessionId == null }
            .plus(
                capturedWorksFull.filter { it.isTimeLapse && it.sessionId != null }
                    .groupBy { it.sessionId }
                    .values
                    .map { it.first() } 
            ).sortedByDescending { it.addedAt }
    }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.plantillas), stringResource(R.string.mis_trabajos))
    var viewerImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedTimeLapseFrames by remember { mutableStateOf<List<CapturedWork>?>(null) }
    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                TopAppBar(
                    title = { Text(stringResource(R.string.mi_galer_a_guardada), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(R.string.volver))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            height = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, fontSize = 14.sp, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                0 -> {
                    if (imagesList.isEmpty()) {
                        EmptyGalleryView(stringResource(R.string.a_n_no_has_guardado_ninguna_imagen_importa_fotos_desde_el_inicio_para_verlas_aqu))
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(imagesList) { item ->
                                GalleryItemCard(
                                    imageItem = item,
                                    onSelect = {
                                        val fileUri = Uri.fromFile(File(item.localPath))
                                        onImageSelectedForDrawing(fileUri)
                                    },
                                    onPreview = {
                                        viewerImageUri = Uri.fromFile(File(item.localPath))
                                    },
                                    onShare = {
                                        GalleryStorageManager.shareImage(context, item.localPath)
                                    },
                                    onDownload = {
                                        scope.launch {
                                            val success = GalleryStorageManager.downloadToPublicGallery(context, item.localPath)
                                            if (success) {
                                                Toast.makeText(context, context.getString(R.string.imagen_guardada_en_carpeta_pictures_artytracepro), Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    onDelete = {
                                        scope.launch {
                                            File(item.localPath).delete()
                                            database.galleryDao().deleteImage(item)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    if (capturedWorks.isEmpty()) {
                        EmptyGalleryView(stringResource(R.string.aun_no_has_capturado_ningun_trabajo))
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(capturedWorks) { work ->
                                CapturedWorkCard(
                                    work = work,
                                    onPreview = {
                                        if (work.isTimeLapse && work.sessionId != null) {
                                            val frames = capturedWorksFull.filter { it.sessionId == work.sessionId }.sortedBy { it.addedAt }
                                            selectedTimeLapseFrames = frames
                                        } else {
                                            viewerImageUri = Uri.fromFile(File(work.workPath))
                                        }
                                    },
                                    onViewReference = {
                                        work.templatePath?.let { path ->
                                            viewerImageUri = Uri.parse(path)
                                        }
                                    },
                                    onShare = {
                                        GalleryStorageManager.shareImage(context, work.workPath)
                                    },
                                    onDownload = {
                                        scope.launch {
                                            val success = GalleryStorageManager.downloadToPublicGallery(context, work.workPath)
                                            if (success) {
                                                Toast.makeText(context, context.getString(R.string.imagen_guardada_en_carpeta_pictures_artytracepro), Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    onDelete = {
                                        scope.launch {
                                            if (work.isTimeLapse && work.sessionId != null) {
                                                val sessionWorks = capturedWorksFull.filter { it.sessionId == work.sessionId }
                                                sessionWorks.forEach {
                                                    File(it.workPath).delete()
                                                    database.galleryDao().deleteCapturedWork(it)
                                                }
                                            } else {
                                                File(work.workPath).delete()
                                                database.galleryDao().deleteCapturedWork(work)
                                            }
                                        }
                                    },
                                    sessionCount = if (work.isTimeLapse && work.sessionId != null) {
                                        capturedWorksFull.count { it.sessionId == work.sessionId }
                                    } else 0
                                )
                            }
                        }
                    }
                }
            }
        }
        viewerImageUri?.let { uri ->
            ImageViewerModal(uri = uri, onDismiss = { viewerImageUri = null })
        }
        selectedTimeLapseFrames?.let { frames ->
            TimeLapseViewerModal(
                frames = frames,
                onDismiss = { selectedTimeLapseFrames = null },
                onShareFrame = { frameToShare ->
                    GalleryStorageManager.shareImage(context, frameToShare.workPath)
                }
            )
        }
    }
}
@Composable
fun TimeLapseViewerModal(
    frames: List<CapturedWork>,
    onDismiss: () -> Unit,
    onShareFrame: (CapturedWork) -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }
    LaunchedEffect(isPlaying, frames.size, currentIndex) {
        if (isPlaying && frames.size > 1) {
            delay(400) 
            currentIndex = (currentIndex + 1) % frames.size
        }
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (frames.isNotEmpty()) {
                val currentWork = frames[currentIndex.coerceIn(0, frames.lastIndex)]
                Image(
                    painter = rememberAsyncImagePainter(model = File(currentWork.workPath)),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 120.dp, top = 60.dp),
                    contentScale = ContentScale.Fit
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.Red.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Timelapse, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "TimeLapse (${currentIndex + 1}/${frames.size})",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cerrar), tint = Color.White)
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.85f))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (frames.size > 1) {
                        Slider(
                            value = currentIndex.toFloat(),
                            onValueChange = { 
                                isPlaying = false
                                currentIndex = it.toInt().coerceIn(0, frames.lastIndex) 
                            },
                            valueRange = 0f..(frames.size - 1).toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFF4081),
                                activeTrackColor = Color(0xFFFF4081)
                            ),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                isPlaying = false
                                currentIndex = if (currentIndex > 0) currentIndex - 1 else frames.lastIndex
                            }
                        ) {
                            Icon(Icons.Default.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        FloatingActionButton(
                            onClick = { isPlaying = !isPlaying },
                            containerColor = Color(0xFFFF4081),
                            contentColor = Color.White,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                null,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                isPlaying = false
                                currentIndex = (currentIndex + 1) % frames.size
                            }
                        ) {
                            Icon(Icons.Default.SkipNext, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        IconButton(
                            onClick = { onShareFrame(currentWork) }
                        ) {
                            Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun ImageViewerModal(uri: Uri, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = uri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cerrar), tint = Color.White)
            }
        }
    }
}
@Composable
fun EmptyGalleryView(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.LightGray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = Color.Gray,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 48.dp),
                lineHeight = 22.sp
            )
        }
    }
}
@Composable
fun GalleryItemCard(
    imageItem: GalleryImage,
    onSelect: () -> Unit,
    onPreview: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Image(
                    painter = rememberAsyncImagePainter(model = File(imageItem.localPath)),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clickable { onPreview() },
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart),
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.plantillas),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(46.dp)
                        .clickable { onSelect() },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 6.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Brush, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionIconButton(icon = Icons.Default.Share, color = Color(0xFF5C6BC0), onClick = onShare)
                ActionIconButton(icon = Icons.Default.Download, color = Color(0xFF66BB6A), onClick = onDownload)
                ActionIconButton(icon = Icons.Default.Delete, color = Color(0xFFEF5350), onClick = onDelete)
            }
        }
    }
}
@Composable
fun CapturedWorkCard(
    work: CapturedWork,
    onPreview: () -> Unit,
    onViewReference: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    sessionCount: Int = 0
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Image(
                        painter = rememberAsyncImagePainter(model = File(work.workPath)),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clickable { onPreview() },
                        contentScale = ContentScale.Crop
                    )
                    if (sessionCount > 1) {
                        Surface(
                            modifier = Modifier.padding(10.dp).align(Alignment.TopStart),
                            color = Color.Red.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PlayCircle, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "TimeLapse ($sessionCount)",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActionIconButton(icon = Icons.Default.Share, color = Color(0xFF5C6BC0), onClick = onShare)
                    ActionIconButton(icon = Icons.Default.Download, color = Color(0xFF66BB6A), onClick = onDownload)
                    ActionIconButton(icon = Icons.Default.Delete, color = Color(0xFFEF5350), onClick = onDelete)
                }
            }
            work.templatePath?.let { path ->
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .size(50.dp)
                        .align(Alignment.TopEnd)
                        .clickable { onViewReference() },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(2.dp, Color.White),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = path),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Visibility, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun ActionIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.1f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
    }
}
