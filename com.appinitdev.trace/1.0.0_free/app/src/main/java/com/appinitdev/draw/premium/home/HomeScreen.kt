
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onImageSelected: (Uri) -> Unit,
    onNavigateToGallery: () -> Unit,
    viewModel: PackViewModel
) {
    val context = LocalContext.current
    val database = remember { GalleryDatabase.getDatabase(context) }
    val recentTemplates by database.galleryDao().getAllImages().collectAsState(initial = emptyList())

    val activeColor = Color(0xFFFF4081) 
    var showAnimeDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = activeColor.copy(alpha = 0.1f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A1C1E)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToGallery,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(activeColor.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Collections, contentDescription = "Galería", tint = activeColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            
            WelcomeBanner(activeColor)

            
            if (recentTemplates.size >= 3) {
                Spacer(modifier = Modifier.height(24.dp))
                RecentTemplatesSection(
                    templates = recentTemplates.take(5),
                    onSelect = { onImageSelected(Uri.fromFile(File(it))) },
                    activeColor = activeColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = stringResource(R.string.c_mo_deseas_cargar_tu_imagen),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    item {
                        MainActionCard(
                            title = stringResource(R.string.imagen_de_la_galer_a),
                            icon = Icons.Default.PhotoLibrary,
                            color = Color(0xFF673AB7),
                            modifier = Modifier.width(230.dp),
                            onClick = { galleryLauncher.launch("image/*") }
                        )
                    }

                    item {
                        MainActionCard(
                            title = stringResource(R.string.buscar_imagen_en_red),
                            icon = Icons.Default.Language,
                            color = Color(0xFF00BFA5),
                            modifier = Modifier.width(230.dp),
                            onClick = { showAnimeDialog = true }
                        )
                    }

                    item {
                        MainActionCard(
                            title = stringResource(R.string.abrir_mi_galer_a_guardada),
                            icon = Icons.Default.FolderSpecial,
                            color = Color(0xFFFF9800),
                            modifier = Modifier.width(230.dp),
                            onClick = onNavigateToGallery
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                



            }


            
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = stringResource(R.string.explorar_categor_as),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
                Spacer(modifier = Modifier.height(8.dp))
                PackManagerScreen(
                    viewModel = viewModel,
                    onImageSelected = onImageSelected
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showAnimeDialog) {
        AnimeImageApp(
            onImageSelected = { url ->
                try {
                    if (url.isNotBlank()) onImageSelected(url.toUri())
                } catch (e: Exception) { e.printStackTrace() }
                showAnimeDialog = false
            },
            onDismissRequest = { showAnimeDialog = false }
        )
    }
}

@Composable
fun WelcomeBanner(activeColor: Color) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(activeColor, activeColor.copy(alpha = 0.8f))
                    )
                )
        ) {
            
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.1f),
                    radius = size.minDimension / 1.5f,
                    center = Offset(size.width * 0.9f, size.height * 0.2f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = size.minDimension / 2.5f,
                    center = Offset(size.width * 0.1f, size.height * 0.8f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.bienvenido_a_tu_app_de_dibujo),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.selecciona_una_imagen_de_tus_archivos_o_usa_un_enlace_de_internet_para_comenzar_a_trazar),
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun RecentTemplatesSection(
    templates: List<com.appinitdev.draw.premium.BD.GalleryImage>,
    onSelect: (String) -> Unit,
    activeColor: Color
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.seguir_dibujando),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E)
            )
            Text(
                text = stringResource(R.string.recientes),
                style = MaterialTheme.typography.labelMedium,
                color = activeColor,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            templates.forEach { template ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(85.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .size(85.dp)
                            .shadow(6.dp, RoundedCornerShape(20.dp))
                            .clickable { onSelect(template.localPath) },
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(File(template.localPath)),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFF333333),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
