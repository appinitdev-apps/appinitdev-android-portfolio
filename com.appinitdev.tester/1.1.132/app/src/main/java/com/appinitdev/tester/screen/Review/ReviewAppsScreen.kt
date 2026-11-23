@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewAppsScreen(viewModel: AuthViewModel, navController: NavController, listState: LazyListState) {
    val apps by viewModel.featuredApps.collectAsState()
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val isUploading by viewModel.isUploading.collectAsState()
    val isVerifyingOcr by viewModel.isVerifyingOcr.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var selectedAppForReview by remember { mutableStateOf<AppModel?>(null) }
    var showVideoTutorial by remember { mutableStateOf(false) }
    val activity = context as? android.app.Activity
    val uiState by viewModel.uiState.collectAsState()
    val mensajeExito = stringResource(R.string.paquete_de_30_puntos_0_25_rep_extra)
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.PurchaseSuccess) {
            showNiceToast(context, mensajeExito, NiceToastType.SUCCESS)
            viewModel.clearState()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.loadFeaturedApps()
    }
    LaunchedEffect(Unit) {
        viewModel.iniciarBilling(context)
    }
    val productionApps = remember(apps) {
        val filtered = apps.filter { it.status == "produccion" }
        val nuevasParaMi = filtered.filter { it.resenasProduccion[currentUserId] == null }.shuffled()
        val gestionadasPorMi = filtered.filter { it.resenasProduccion[currentUserId] != null }
            .sortedWith(compareBy<AppModel> { app ->
                val miResena = app.resenasProduccion[currentUserId]
                when (miResena?.estado) {
                    "pendiente" -> 1
                    "aprobada" -> 2
                    "rechazada" -> 3
                    else -> 4
                }
            })
        val listaFinal = nuevasParaMi + gestionadasPorMi
        listaFinal
    }
    val successMessage = stringResource(R.string.rese_a_enviada_a_revisi_n)
    if (showUploadDialog) {
        selectedAppForReview?.let { app ->
            UploadReviewDialog(
                app = app,
                isUploading = isUploading,
                viewModel = viewModel,
                onDismiss = { showUploadDialog = false },
                onConfirm = { uri ->
                    viewModel.verificarResenaConOcr(context, uri, app.nombre) { success, error ->
                        if (success) {
                            viewModel.subirEvidenciaResena(
                                appId = app.id,
                                uri = uri,
                                appName = app.nombre,
                                userName = userProfile?.devName ?: "Usuario",
                                onSuccess = {
                                    showNiceToast(context, successMessage, NiceToastType.SUCCESS)
                                    showUploadDialog = false
                                },
                                token = userProfile?.fcmToken,
                                tokenApp = app.fcmToken
                            )
                        } else {
                            showNiceToast(context, error ?: "Error de verificación", NiceToastType.ERROR)
                        }
                    }
                }
            )
        }
    }
    if (showVideoTutorial) {
        VideoDialog(onDismiss = { showVideoTutorial = false })
    }
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                viewModel.loadFeaturedApps()
                delay(1000)
                isRefreshing = false
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp), 
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFA000),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        stringResource(R.string.gana_10_puntos),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6F00)
                    )
                    Text(
                        text = stringResource(R.string.por_cada_rese_a_verificada),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF5D4037) 
                    )
                }
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .clickable {
                    activity?.let {
                        viewModel.lanzarFlujoPago(it, "puntos_30_pack")
                    }
                },
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
            border = BorderStroke(1.dp, Color(0xFFFFC107))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            Color(0xFFFFA000),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.paquete_de_30_puntos_0_25_rep_extra),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF5D4037), 
                        fontSize = 16.sp
                    )
                    Text(
                        text = stringResource(R.string.consumible_compra_cuantas_veces_quieras_sube_tu_rep_y_aparrece_en_los_primeros_lugares),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF795548), 
                        lineHeight = 14.sp
                    )
                }
                Surface(
                    color = Color(0xFFFFC107),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$0.99",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
        TextButton(onClick = { showVideoTutorial = true }) {
            Icon(Icons.Default.PlayCircle, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.ver_video_tutorial_de_ejemplo))
        }
        if (apps.isEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                items(5) { com.appinitdev.tester.util.ShimmerAppItem() }
            }
        } else if (productionApps.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.no_hay_apps_en_producci_n_disponibles),
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(productionApps, key = { it.id }) { app ->
                    val miResena = app.resenasProduccion[currentUserId]
                    ProductionAppRewardCard(
                        app = app,
                        estadoResena = miResena?.estado,
                        onDownload = { openPlayStore(context, app.paquete) },
                        onUploadReview = {
                            selectedAppForReview = app
                            showUploadDialog = true
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
}
@Composable
fun ProductionAppRewardCard(
    app: AppModel,
    estadoResena: String?, 
    onDownload: () -> Unit,
    onUploadReview: () -> Unit,
) {
    val context = LocalContext.current
    var isInstalled by remember(app.paquete) {
        mutableStateOf(Funciones.isAppInstalled(context, app.paquete))
    }
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(app.icono)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        error = { LetterLogo(app.nombre, modifier = Modifier.fillMaxSize()) }
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = app.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = app.paquete,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (estadoResena) {
                            "aprobada" -> {
                                Surface(
                                    color = Color(0xFFE8F5E9),
                                    contentColor = Color(0xFF2E7D32),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string._10_puntos).uppercase(),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            "pendiente" -> {
                                Surface(
                                    color = Color(0xFFFFF3E0),
                                    contentColor = Color(0xFFF57C00),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.revisando).uppercase(),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            "rechazada" -> {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.Rechazada).uppercase(),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            else -> {
                                Surface(
                                    color = if (isInstalled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = if (isInstalled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (isInstalled) stringResource(R.string.instalada).uppercase() else stringResource(R.string.puntos_disponibles_10).uppercase(),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            when (estadoResena) {
                "aprobada" -> {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${stringResource(R.string._10_puntos)} - ${stringResource(R.string.completado)}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
                "pendiente" -> {
                    Surface(
                        color = Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFF57C00),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${stringResource(R.string.revisando)} (${stringResource(R.string.rese_a_enviada_a_revisi_n)})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF57C00)
                            )
                        }
                    }
                }
                else -> {
                    if (estadoResena == "rechazada") {
                        Text(
                            text = stringResource(R.string.rese_a_no_cumple_los_criterios_1_captura_pantalla_de_rese_a_2_rese_a_positiva_3_rese_a_presente_en_la_play_store),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }
                    if (isInstalled) {
                        Button(
                            onClick = onUploadReview,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (estadoResena == "rechazada") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = if (estadoResena == "rechazada") Icons.Default.Warning else Icons.Default.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (estadoResena == "rechazada") stringResource(R.string.reintentar_resena) else stringResource(R.string.subir_rese_a),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDownload,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.descargar),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            FilledTonalButton(
                                onClick = {
                                    val instalada = Funciones.isAppInstalled(context, app.paquete)
                                    if (instalada) {
                                        isInstalled = true
                                        showNiceToast(
                                            context,
                                            context.getString(R.string.app_verificada_instancia),
                                            NiceToastType.SUCCESS
                                        )
                                    } else {
                                        showNiceToast(
                                            context,
                                            context.getString(R.string.app_no_detectada_descargar),
                                            NiceToastType.WARNING
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.verificar),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun UploadReviewDialog(
    app: AppModel,
    isUploading: Boolean,
    viewModel: AuthViewModel, 
    onDismiss: () -> Unit,
    onConfirm: (Uri) -> Unit,
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isExpanded by remember { mutableStateOf(false) }
    val verifyingOcrState by viewModel.isVerifyingOcr.collectAsState()
    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
            imageUri = uri
        }
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        title = { Text(stringResource(R.string.reclamar_recompensa), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)) {
                    Text(
                        text = stringResource(R.string._1_descarga) + " ${app.nombre}.\n" +
                                stringResource(R.string._2_deja_una_rese_a_de_5_estrellas_si_te_gusta) +
                                stringResource(R.string._3_sube_la_captura_de_pantalla_aqu),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                        lineHeight = 20.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
                if (imageUri != null) {
                    Box(modifier = Modifier.size(150.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Preview",
                            modifier = Modifier.fillMaxSize()
                        )
                        if (verifyingOcrState) {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color.White)
                            }
                        }
                    }
                    TextButton(onClick = { if (!verifyingOcrState) launcher.launch("image/*") }) {
                        Icon(Icons.Default.Image, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.cambiar_imagen))
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            onClick = { isExpanded = !isExpanded },
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Icon(if (isExpanded) Icons.Default.Info else Icons.Default.PlayCircle, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (isExpanded) stringResource(R.string.ocultar_ejemplo) else stringResource(R.string.ver_ejemplo),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        AnimatedVisibility(visible = isExpanded) {
                            Image(
                                painter = painterResource(id = R.drawable.resena),
                                contentDescription = "Ejemplo de captura",
                                modifier = Modifier
                                    .padding(top = 12.dp)
                                    .size(240.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.seleccionar_captura), fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp).padding(end = 16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Button(
                    onClick = { imageUri?.let { onConfirm(it) } },
                    enabled = imageUri != null,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.enviar_evidencia), fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.cancelar)) }
        }
    )
}
fun openPlayStore(context: Context, packageName: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}