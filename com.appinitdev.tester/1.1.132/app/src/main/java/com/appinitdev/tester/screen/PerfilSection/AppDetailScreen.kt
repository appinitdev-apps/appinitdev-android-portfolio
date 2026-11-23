
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(appId: String, viewModel: AuthViewModel, onBack: () -> Unit) {
    val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current
    val myApps by viewModel.userApps.collectAsState()
    val communityApps by viewModel.communityApps.collectAsState()
    val messages by viewModel.chatMessages.collectAsState()
    var lastSeenMessagesCount by remember { mutableIntStateOf(0) }
    val isLoading by viewModel.isUploadingMessages.collectAsState()
    var showStatsModal by remember { mutableStateOf(false) }
    var showDeveloperModal by remember { mutableStateOf(false) }
    var showInstallationSteps by remember { mutableStateOf(false) }
    val app = remember(myApps, communityApps, appId) {
        (myApps + communityApps).find { it.id == appId }
    }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var calculatedPeekHeight by remember { mutableStateOf(340.dp) }
    val density = LocalDensity.current

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    if (app == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val isOwner = app.idPropietario == user?.uid
    val myStatus = app.testers[user?.uid]?.validado
    val urlLimpia = app.urlGoogleGroup.trim()
    val esUrlReal = android.util.Patterns.WEB_URL.matcher(urlLimpia).matches()
    val canAccessSheet = isOwner || myStatus == "Aceptado"

    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedTab, messages.size) {
        if (selectedTab == 1) {
            lastSeenMessagesCount = messages.size
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            icon = {
                Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), shape = CircleShape, modifier = Modifier.size(64.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = if (app.metodoTest == "google_group" || urlLimpia.isNotBlank()) stringResource(R.string.ha_aceptado_tu_solicitud) else stringResource(R.string.solicitud_enviada),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = if (app.metodoTest == "google_group" || urlLimpia.isNotBlank())
                        "${stringResource(R.string.ha_aceptado_tu_solicitud)}: ${app.nombre}"
                    else
                        stringResource(R.string.tu_solicitud_para_testear_esta_aplicaci_n_ha_sido_enviada_correctamente),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.entendido),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }

    DisposableEffect(appId) {
        viewModel.clearAppDetailState()
        onDispose {
            viewModel.clearAppDetailState()
        }
    }

    LaunchedEffect(appId, canAccessSheet, myStatus, isOwner) {
        viewModel.clearAppDetailState()
        if (canAccessSheet) {
            viewModel.listenToMessages(appId)
        }
        viewModel.loadBugReports(appId)
    }

    val solicitudesCount = app.testers.values.count { it.validado == "Solicitud" }
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val miData = app.testers[currentUserId]
    val misDias = (miData?.dias ?: 0).toString() + " " + stringResource(R.string.dias_label)

    if (showStatsModal) {
        ModalBottomSheet(onDismissRequest = { showStatsModal = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Text(
                    text = stringResource(R.string.monitorizaci_n),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(8.dp)
                )
                TestersChart(app = app, metaDias = 14)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showDeveloperModal) {
        com.appinitdev.tester.screen.CommunityAppsSection.DeveloperProfileModal(
            developerId = app.idPropietario,
            viewModel = viewModel,
            onDismiss = { showDeveloperModal = false }
        )
    }

    if (showInstallationSteps) {
        InstallationStepsBottomSheet(app = app, onDismiss = { showInstallationSteps = false })
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopSmallAppBar(
                title = "${app.nombre} ",
                onBack = onBack,
                actions = {
                    var showReport by remember { mutableStateOf(false) }
                    IconButton(onClick = { showReport = true }) {
                        Icon(Icons.Default.Report, stringResource(R.string.reportar), tint = MaterialTheme.colorScheme.error)
                    }

                    if (showReport) {
                        com.appinitdev.tester.screen.ReportDialog(
                            onDismiss = { showReport = false },
                            onSubmit = { reason ->
                                showReport = false
                                showNiceToast(context, context.getString(R.string.reporte_enviado), NiceToastType.SUCCESS)
                            }
                        )
                    }

                    IconButton(onClick = { showStatsModal = true }) {
                        Icon(imageVector = Icons.Default.BarChart, contentDescription = stringResource(R.string.estad_isticas))
                    }
                }
            )
        },
        sheetContent = {
            Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                if (isOwner) {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                viewModel.loadAndVerifyApps()
                            },
                            text = {
                                BadgedBox(
                                    badge = {
                                        if (solicitudesCount > 0) {
                                            Badge(containerColor = Color.Red, contentColor = Color.White) {
                                                Text(solicitudesCount.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Text(text = stringResource(R.string.solicitudes), modifier = Modifier.padding(horizontal = 4.dp))
                                }
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text(stringResource(R.string.chat_grupal)) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = {
                                selectedTab = 2
                                viewModel.loadBugReports(app.id)
                            },
                            text = { Text(stringResource(R.string.reportes)) }
                        )
                        Tab(
                            selected = selectedTab == 3,
                            onClick = {
                                selectedTab = 3
                                viewModel.loadAndVerifyApps()
                            },
                            text = { Text(text = stringResource(R.string.testers), modifier = Modifier.padding(horizontal = 4.dp)) }
                        )
                    }

                    when (selectedTab) {
                        0 -> RequestList(app, viewModel)
                        1 -> ChatGroup(app, viewModel, isLoading)
                        2 -> BugReportsList(app, viewModel)
                        3 -> AcceptedTestersList(app, viewModel)
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TabRow(selectedTabIndex = selectedTab) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text(stringResource(R.string.chat_grupal)) }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { 
                                    selectedTab = 1
                                    viewModel.loadBugReports(app.id)
                                },
                                text = { Text(stringResource(R.string.reportes)) }
                            )
                        }
                        
                        val statusTag = when (myStatus) {
                            "Aceptado" -> null
                            "Solicitud" -> stringResource(R.string.esperando_aceptacion_chat)
                            else -> stringResource(R.string.no_has_solicitado_chat)
                        }

                        if (selectedTab == 0 && statusTag != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = statusTag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        when (selectedTab) {
                            0 -> ChatGroup(app, viewModel, isLoading, isReadOnly = myStatus != "Aceptado" && !isOwner)
                            1 -> BugReportsList(app, viewModel)
                        }
                    }
                }
            }
        },
        sheetPeekHeight = if (canAccessSheet) calculatedPeekHeight else 0.dp,
        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetTonalElevation = 8.dp,
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val totalHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
                    .verticalScroll(rememberScrollState())
            ) {
                if (canAccessSheet) {
                    Box(modifier = Modifier.onSizeChanged { size ->
                        val contentHeight = with(density) { size.height.toDp() }
                        val remaining = totalHeight - contentHeight
                        if (remaining > 0.dp && Math.abs(remaining.value - calculatedPeekHeight.value) > 1f) {
                            calculatedPeekHeight = remaining
                        }
                    }) {
                        AppHeaderInfo(
                            app = app,
                            status = myStatus,
                            misDias = misDias,
                            viewModel = viewModel,
                            onDeveloperClick = { showDeveloperModal = true },
                            onInstallationStepsClick = { showInstallationSteps = true }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(calculatedPeekHeight)) 
                } else {
                    AppHeaderInfo(
                        app = app,
                        status = myStatus,
                        misDias = misDias,
                        viewModel = viewModel,
                        onDeveloperClick = { showDeveloperModal = true },
                        onInstallationStepsClick = { showInstallationSteps = true }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        when {
                            myStatus == "Solicitud" -> {
                                var showCancelDialog by remember { mutableStateOf(false) }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = stringResource(R.string.tu_solicitud_ha_sido_enviada_esperando_aprobaci_n_del_desarrollador),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    OutlinedButton(
                                        onClick = { showCancelDialog = true },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ExitToApp, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(stringResource(R.string.cancelar_solicitud_tester), fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (showCancelDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showCancelDialog = false },
                                        title = { Text(stringResource(R.string.confirmar_dejar_tester_titulo), fontWeight = FontWeight.Bold) },
                                        text = { Text(stringResource(R.string.confirmar_dejar_tester_msg)) },
                                        confirmButton = {
                                            Button(
                                                onClick = {
                                                    showCancelDialog = false
                                                    viewModel.leaveAppAsTester(app, context)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Text(stringResource(R.string.cancelar_solicitud_tester))
                                            }
                                        },
                                        dismissButton = {
                                            OutlinedButton(onClick = { showCancelDialog = false }) {
                                                Text(stringResource(R.string.cancelar))
                                            }
                                        }
                                    )
                                }
                            }
                            myStatus == "Rechazado" -> {
                                Text(text = stringResource(R.string.lo_sentimos_tu_solicitud_para_testear_esta_app_no_fue_aceptada), color = Color.Red, textAlign = TextAlign.Center)
                            }
                            else -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(stringResource(R.string.para_acceder_al_chat_y_las_descargas_debes_ser_tester))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    JoinButton {
                                        viewModel.requestJoinApp(app, context)
                                        showSuccessDialog = true
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AdmobBanner()
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestList(app: AppModel, viewModel: AuthViewModel) {
    val solicitudes = app.testers.values.filter { it.validado == "Solicitud" }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                text = stringResource(R.string.antes_de_aceptar_la_solicitud_copia_el_correo_y_pegalo_en_en_tus_correos_play_console),
                style = MaterialTheme.typography.bodyMedium, color = Color.Gray
            )
        }

        if (solicitudes.isEmpty()) {
            item { EmptySolicitudesState(stringResource(R.string.sin_solicitudes_por_el_momento)) }
        } else
            items(solicitudes) { tester ->
                ListItem(
                    headlineContent = { Text(tester.devName) },
                    supportingContent = { Text(tester.correoUser) },
                    trailingContent = {
                        val context = LocalContext.current
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalIconButton(
                                onClick = {
                                    clipboard.setPrimaryClip(ClipData.newPlainText("correo", tester.correoUser))
                                    showNiceToast(context, context.getString(R.string.correo_copiado), NiceToastType.INFO)
                                },
                                modifier = Modifier.size(36.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                            ) { Icon(imageVector = Icons.Default.ContentCopy, contentDescription = stringResource(R.string.copiar_correo), modifier = Modifier.size(18.dp)) }

                            FilledIconButton(
                                onClick = { viewModel.responseToTester(app, tester.idUser, true, context) },
                                modifier = Modifier.size(36.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White)
                            ) { Icon(imageVector = Icons.Default.Check, contentDescription = stringResource(R.string.aceptar), modifier = Modifier.size(20.dp)) }

                            OutlinedIconButton(
                                onClick = { viewModel.responseToTester(app, tester.idUser, false, context) },
                                modifier = Modifier.size(36.dp),
                                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                                colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = Color.Red)
                            ) { Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.rechazar), modifier = Modifier.size(20.dp)) }
                        }
                    }
                )
            }
        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}


@Composable
fun EmptySolicitudesState(titulo: String = "") {
    Column(modifier = Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(imageVector = Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = titulo, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeaderInfo(
    app: AppModel,
    status: String? = "Solicitud",
    misDias: String? = "Solicitud",
    viewModel: AuthViewModel,
    onDeveloperClick: () -> Unit,
    onInstallationStepsClick: () -> Unit
) {
    val context = LocalContext.current
    var showInfoSheet by remember { mutableStateOf(false) }

    if (showInfoSheet) {
        AppDetailBottomSheet(app = app, onDismiss = { showInfoSheet = false })
    }

    fun openUrl(url: String) {
        if (url.isNotBlank()) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(if (url.startsWith("http")) url else "https://$url")))
        }
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Inbox,
                        null,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.recursos_y_gu_a),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${stringResource(R.string.por)} ${app.nombreDesarrollador ?: "Dev"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onDeveloperClick() },
                        textDecoration = TextDecoration.Underline
                    )
                }
                if (misDias != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = misDias,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            val urlLimpia = app.urlGoogleGroup.trim()
            val esUrlReal = android.util.Patterns.WEB_URL.matcher(urlLimpia).matches()

            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            val isOwner = app.idPropietario == user?.uid
            val isAccepted = isOwner || status == "Aceptado"

            if (status == "Solicitud" && !esUrlReal) {
                Surface(
                    color = MaterialTheme.colorScheme.infoContainer(),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.tu_solicitud_ha_sido_enviada_esperando_aprobaci_n_del_desarrollador),
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (isAccepted) {
                if (urlLimpia.isNotEmpty() && esUrlReal) {
                    Button(
                        onClick = { openUrl(urlLimpia) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(Icons.Default.Group, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.google_group), fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { openUrl(app.urlWeb) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = app.urlWeb.isNotBlank(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Icon(Icons.Default.Language, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.link_web), fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = { openUrl(app.urlAndroid) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = app.urlAndroid.isNotBlank(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Icon(Icons.Default.Shop, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.link_play_store), fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                var isVerified by remember { mutableStateOf(false) }
                var verificationChecked by remember { mutableStateOf(false) }
                
                Button(
                    onClick = { 
                        isVerified = com.appinitdev.tester.Funciones.isAppInstalled(context, app.paquete)
                        verificationChecked = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isVerified) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isVerified) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = if (isVerified) BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.5f)) else null
                ) {
                    Icon(
                        imageVector = if (isVerified) Icons.Filled.CheckCircle else Icons.Filled.VerifiedUser,
                        null, 
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isVerified) stringResource(R.string.app_instalada) 
                               else if (verificationChecked) stringResource(R.string.app_no_encontrada)
                               else stringResource(R.string.verificar_instalacion),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(8.dp))
            }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onInstallationStepsClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Build, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.guia_instalacion_titulo), fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                    }

                    Button(
                        onClick = { showInfoSheet = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Info, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.info_app_titulo), fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                    }
                }

                if (status == "Aceptado") {
                    var showBugDialog by remember { mutableStateOf(false) }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { showBugDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Icon(Icons.Default.BugReport, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.reportar_problema), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    if (showBugDialog) {
                        com.appinitdev.tester.screen.BugReportDialog(onDismiss = { showBugDialog = false }, onSubmit = { device, steps, desc -> viewModel.submitBugReport(app.id, device, steps, desc); showBugDialog = false })
                    }
                }

                val myTesterData = app.testers[user?.uid]
                if (!isOwner && status == "Aceptado" && ((myTesterData?.dias ?: 0) >= 14 || myTesterData?.completado == true)) {
                    var showSubmitFeedbackDialog by remember { mutableStateOf(false) }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { showSubmitFeedbackDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Star, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.enviar_feedback_final), fontWeight = FontWeight.Bold)
                    }

                    if (showSubmitFeedbackDialog) {
                        Submit14DayFeedbackDialog(
                            appName = app.nombre,
                            onSubmit = { est, dis, com, sug ->
                                viewModel.submit14DayFeedback(app.id, est, dis, com, sug) {
                                    showNiceToast(context, context.getString(R.string.gracias_feedback_enviado), NiceToastType.SUCCESS)
                                    showSubmitFeedbackDialog = false
                                }
                            },
                            onDismiss = { showSubmitFeedbackDialog = false }
                        )
                    }
                }

                if (isOwner) {
                    var showViewFeedbacksModal by remember { mutableStateOf(false) }
                    var showQuestionnaireAssistantModal by remember { mutableStateOf(false) }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showViewFeedbacksModal = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Assignment, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.ver_feedbacks_14_dias), fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { showQuestionnaireAssistantModal = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.School, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.asistente_cuestionario_play_console), fontWeight = FontWeight.Bold)
                    }

                    if (showViewFeedbacksModal) {
                        View14DayFeedbacksModal(
                            app = app,
                            viewModel = viewModel,
                            onDismiss = { showViewFeedbacksModal = false }
                        )
                    }

                    if (showQuestionnaireAssistantModal) {
                        PlayConsoleQuestionnaireAssistantModal(
                            app = app,
                            viewModel = viewModel,
                            onDismiss = { showQuestionnaireAssistantModal = false }
                        )
                    }
                }

                if (!isOwner && (status == "Aceptado" || status == "Solicitud")) {
                    var showLeaveConfirm by remember { mutableStateOf(false) }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showLeaveConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (status == "Solicitud") stringResource(R.string.cancelar_solicitud_tester) else stringResource(R.string.dejar_de_ser_tester),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    if (showLeaveConfirm) {
                        AlertDialog(
                            onDismissRequest = { showLeaveConfirm = false },
                            icon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            title = {
                                Text(
                                    text = stringResource(R.string.confirmar_dejar_tester_titulo),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            text = {
                                Text(
                                    text = stringResource(R.string.confirmar_dejar_tester_msg),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showLeaveConfirm = false
                                        viewModel.leaveAppAsTester(app, context)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    )
                                ) {
                                    Text(stringResource(R.string.dejar_de_ser_tester))
                                }
                            },
                            dismissButton = {
                                OutlinedButton(
                                    onClick = { showLeaveConfirm = false }
                                ) {
                                    Text(stringResource(R.string.cancelar))
                                }
                            }
                        )
                    }
                }

                
                
            }
        }
    }

@Composable
fun InfoActionCard(title: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ColorScheme.infoContainer(): Color = primaryContainer.copy(alpha = 0.3f)

@Composable
fun TestersChart(app: AppModel, metaDias: Int = 14) {
    val listaTesters = remember(app.testers) { app.testers.values.filter { it.dias > 0 }.sortedByDescending { it.dias } }
    if (listaTesters.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).padding(16.dp)) {
        listaTesters.forEach { TesterBarRow(tester = it, metaDias = metaDias) }
    }
}

@Composable
private fun TesterBarRow(tester: TesterData, metaDias: Int) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = tester.devName.ifBlank { stringResource(R.string.sin_informaci_n) }, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            Text(text = "${tester.dias}/$metaDias " + stringResource(R.string.dias_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(MaterialTheme.colorScheme.outlineVariant, CircleShape)) {
            val progreso = (tester.dias.toFloat() / metaDias.toFloat()).coerceIn(0f, 1f)
            Box(modifier = Modifier.fillMaxWidth(progreso).fillMaxHeight().background(color = if (progreso >= 1f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary, shape = CircleShape))
        }
    }
}


@Composable
fun TextLink(label: String, url: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val tieneUrl = url.isNotBlank()
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false).clickable(enabled = tieneUrl) { onClick() }.padding(vertical = 4.dp)) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = if (tieneUrl) MaterialTheme.colorScheme.primary else Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = if (tieneUrl) label else stringResource(R.string.no_disponible), color = if (tieneUrl) MaterialTheme.colorScheme.primary else Color.Gray, fontSize = 16.sp, textDecoration = if (tieneUrl) TextDecoration.Underline else TextDecoration.None, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (tieneUrl) {
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = { clipboard.setPrimaryClip(ClipData.newPlainText("link", url)); showNiceToast(context, context.getString(R.string.correo_copiado), NiceToastType.INFO) }, modifier = Modifier.size(32.dp)) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun JoinButton(onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.quieres_probar_esta_aplicaci_n),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.enviar_solicitud_de_tester), fontWeight = FontWeight.Black)
            }
        }
    }
}


@Composable
fun ChatGroup(app: AppModel, viewModel: AuthViewModel, isLoading: Boolean, isReadOnly: Boolean = false) {
    var messageText by remember { mutableStateOf("") }
    val messages by viewModel.chatMessages.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val isKeyboardVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp

    LaunchedEffect(messages.size, isKeyboardVisible) { if (messages.isNotEmpty()) { listState.animateScrollToItem(messages.size - 1) } }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (isLoading) { item { Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } } }
            else if (messages.isEmpty()) { item { EmptySolicitudesState(stringResource(R.string.aun_no_hay_mensajes)) } }
            else { items(messages) { MessageBubble(it, isMe = it.senderId == currentUser?.uid) } }
        }
        if (!isReadOnly) {
            Surface(tonalElevation = 4.dp, shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).navigationBarsPadding().imePadding()) {
                    OutlinedTextField(value = messageText, onValueChange = { messageText = it }, modifier = Modifier.weight(1f), placeholder = { Text(stringResource(R.string.escribe)) }, shape = RoundedCornerShape(24.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant), maxLines = 4)
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(onClick = { if (messageText.isNotBlank()) { viewModel.sendMessage(app, messageText); messageText = "" } }, modifier = Modifier.size(48.dp), shape = CircleShape, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary, elevation = FloatingActionButtonDefaults.elevation(0.dp)) { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(20.dp)) }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSmallAppBar(title: String, onBack: () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    TopAppBar(
        title = {
            Text(
                text = title.take(32),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.volver)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        actions = actions
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcceptedTestersList(app: AppModel, viewModel: AuthViewModel) {
    val aceptados = app.testers.values.filter { it.validado == "Aceptado" }.sortedByDescending { it.dias }
    var selectedTester by remember { mutableStateOf<com.appinitdev.tester.model.TesterData?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }

    selectedTester?.let { TesterProfileModal(testerData = it, viewModel = viewModel, onDismiss = { selectedTester = null }) }
    if (showExportDialog) {
        ExportPlayConsoleDialog(app = app, onDismiss = { showExportDialog = false })
    }

    if (aceptados.isEmpty()) { EmptySolicitudesState(stringResource(R.string.a_n_no_hay_testers_aceptados)) }
    else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(text = stringResource(R.string.valora_a_los_testers_segun_tu_criterio_de_satisfaccion), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(bottom = 8.dp))
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(text = "${app.testers.size} " + stringResource(R.string.correos), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { showExportDialog = true }, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 12.dp), modifier = Modifier.height(36.dp)) {
                                    Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.play_console), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
            items(aceptados) { tester ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { selectedTester = tester }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    ListItem(colors = ListItemDefaults.colors(containerColor = Color.Transparent), headlineContent = { Text(text = " ${tester.devName}".take(24), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface) }, supportingContent = { Column { Text(text = " ${tester.correoUser}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(text = " ID: ${tester.idUser}".take(30), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)); Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) { Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer) { Text("${stringResource(R.string.d_ass)} ${tester.dias}", modifier = Modifier.padding(horizontal = 4.dp), fontSize = 12.sp) }; Spacer(Modifier.width(8.dp)); Text(text = formatLongToDate(tester.ultimaRevision), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) } } }, trailingContent = { Row(verticalAlignment = Alignment.CenterVertically) { Surface(color = Color(0xFFFFC107).copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(32.dp)) { Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.padding(6.dp)) }; Spacer(Modifier.width(4.dp)); val context = LocalContext.current; val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager; IconButton(onClick = { clipboard.setPrimaryClip(ClipData.newPlainText("correo", tester.correoUser)); showNiceToast(context, context.getString(R.string.correo_copiado), NiceToastType.INFO) }) { Icon(imageVector = Icons.Default.ContentCopy, contentDescription = stringResource(R.string.copiar_correo), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) } } })
                }
            }
            item { TestersChart(app, 14) }
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}


@Composable
fun BugReportsList(app: AppModel, viewModel: AuthViewModel) {
    val reports by viewModel.bugReports.collectAsState()
    val user = FirebaseAuth.getInstance().currentUser
    val isOwner = app.idPropietario == user?.uid

    if (reports.isEmpty()) {
        EmptySolicitudesState(stringResource(R.string.sin_reportes))
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(reports) { bug ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BugReport, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(bug.userName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.weight(1f))
                            Badge(
                                containerColor = when(bug.status) {
                                    "abierto" -> MaterialTheme.colorScheme.errorContainer
                                    "resuelto" -> Color(0xFFE8F5E9)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Text(bug.status.uppercase(), modifier = Modifier.padding(horizontal = 4.dp), fontSize = 10.sp)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(text = bug.description, style = MaterialTheme.typography.bodyMedium)
                        if (bug.stepsToReproduce.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.pasos_label), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(bug.stepsToReproduce, style = MaterialTheme.typography.bodySmall)
                        }
                        
                        if (isOwner && bug.status == "abierto") {
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                TextButton(onClick = { 
                                    viewModel.updateBugStatus(bug.id, app.id, "resuelto", bug.userId)
                                }) {
                                    Text(stringResource(R.string.marcar_resuelto))
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}


@Composable
fun CopyAllEmailsIcon(correos: List<String>) {
    if (correos.isEmpty()) return
    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    IconButton(onClick = { val texto = correos.joinToString(", "); clipboard.setPrimaryClip(ClipData.newPlainText("correos", texto)); showNiceToast(context, context.getString(R.string.correo_copiado) + " (${correos.size})", NiceToastType.INFO) }) {
        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = stringResource(R.string.copiar_correos))
    }
}

@Composable
fun ExportPlayConsoleDialog(app: AppModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    val validEmails = remember(app.testers) {
        app.testers.values
            .mapNotNull { it.correoUser?.trim() }
            .filter { it.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() }
            .distinct()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.exportar_play_console_titulo), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${stringResource(R.string.correos_validos_detectados)} ${validEmails.size}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.formato_play_console_desc), style = MaterialTheme.typography.bodySmall)
                    }
                }

                if (validEmails.isNotEmpty()) {
                    Text(
                        text = validEmails.take(5).joinToString(", ") + if (validEmails.size > 5) "..." else "",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                } else {
                    Text(stringResource(R.string.sin_testers_validos_registrados), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }
        },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (validEmails.isNotEmpty()) {
                            val text = validEmails.joinToString(", ")
                            clipboard.setPrimaryClip(ClipData.newPlainText("PlayConsoleEmails", text))
                            showNiceToast(context, context.getString(R.string.correos_copiados_portapapeles, validEmails.size), NiceToastType.SUCCESS)
                            onDismiss()
                        }
                    },
                    enabled = validEmails.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.copiar_formato_play_console), fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        if (validEmails.isNotEmpty()) {
                            val csvContent = "Email\n" + validEmails.joinToString("\n")
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Testers - ${app.nombre}")
                                putExtra(Intent.EXTRA_TEXT, csvContent)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Compartir CSV de Testers"))
                            onDismiss()
                        }
                    },
                    enabled = validEmails.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.compartir_como_csv), fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        },
        dismissButton = null
    )
}

@Composable
fun Submit14DayFeedbackDialog(
    appName: String,
    onSubmit: (ratingEstabilidad: Float, ratingDiseno: Float, comentarios: String, sugerencias: String) -> Unit,
    onDismiss: () -> Unit
) {
    var ratingEstabilidad by remember { mutableFloatStateOf(5f) }
    var ratingDiseno by remember { mutableFloatStateOf(5f) }
    var comentarios by remember { mutableStateOf("") }
    var sugerencias by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.reporte_final_14_dias_titulo, appName), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(stringResource(R.string.reporte_final_14_dias_desc), style = MaterialTheme.typography.bodySmall)

                Text(stringResource(R.string.estabilidad_de_la_app), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = if (star <= ratingEstabilidad) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { ratingEstabilidad = star.toFloat() }
                        )
                    }
                }

                Text(stringResource(R.string.diseno_usabilidad), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = if (star <= ratingDiseno) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { ratingDiseno = star.toFloat() }
                        )
                    }
                }

                OutlinedTextField(
                    value = comentarios,
                    onValueChange = { comentarios = it },
                    label = { Text(stringResource(R.string.pregunta_errores)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = sugerencias,
                    onValueChange = { sugerencias = it },
                    label = { Text(stringResource(R.string.pregunta_sugerencias)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(ratingEstabilidad, ratingDiseno, comentarios, sugerencias)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.enviar_reporte_final), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Text(stringResource(R.string.cancelar))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun View14DayFeedbacksModal(
    app: AppModel,
    viewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    val feedbacks by viewModel.feedbackReports14Days.collectAsState()

    LaunchedEffect(app.id) {
        viewModel.load14DayFeedbacks(app.id)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(stringResource(R.string.reportes_finales_14_dias_cant, feedbacks.size), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(12.dp))

            if (feedbacks.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.sin_reportes_finales_14_dias), color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                ) {
                    items(feedbacks) { fb ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(fb.userName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Row {
                                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                                        Text("${fb.ratingEstabilidad}/5", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text("${fb.modeloDispositivo} | ${fb.versionAndroid}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                if (fb.comentarios.isNotBlank()) {
                                    Text("${stringResource(R.string.errores_comentarios_label)} ${fb.comentarios}", style = MaterialTheme.typography.bodyMedium)
                                }
                                if (fb.sugerencias.isNotBlank()) {
                                    Text("${stringResource(R.string.sugerencias_label)} ${fb.sugerencias}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Text(stringResource(R.string.cerrar))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayConsoleQuestionnaireAssistantModal(
    app: AppModel,
    viewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val feedbacks by viewModel.feedbackReports14Days.collectAsState()
    val bugReports by viewModel.bugReports.collectAsState()

    LaunchedEffect(app.id) {
        viewModel.load14DayFeedbacks(app.id)
        viewModel.loadBugReports(app.id)
    }

    val totalTesters = app.testers.size
    val resolvedBugs = bugReports.count { it.status == "resuelto" }
    val totalFeedbacks = feedbacks.size
    val avgRating = if (feedbacks.isNotEmpty()) feedbacks.map { it.ratingEstabilidad }.average() else 5.0

    val respuestaReclutamiento = stringResource(R.string.pregunta_1_reclutamiento_text)
    val respuestaFacilidad = stringResource(R.string.pregunta_2_experiencia_text)
    val respuestaFeedback = stringResource(R.string.pregunta_3_feedback_text)
    val respuestaCambios = stringResource(R.string.pregunta_4_cambios_text)
    val respuestaPreparacion = stringResource(R.string.pregunta_5_preparacion_text)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.School, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(stringResource(R.string.asistente_cuestionario_play_console), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text(stringResource(R.string.asistente_cuestionario_subtitulo), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.metricas_de_tu_app, app.nombre), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.testers_registrados_cant, totalTesters), style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(R.string.bugs_corregidos_cant, resolvedBugs), style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(R.string.feedbacks_14_dias_promedio, totalFeedbacks, avgRating), style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(16.dp))

            QuestionAnswerCard(
                numero = stringResource(R.string.pregunta_1_reclutamiento_titulo),
                pregunta = stringResource(R.string.pregunta_1_reclutamiento_text),
                respuesta = respuestaReclutamiento,
                onCopy = {
                    clipboard.setPrimaryClip(ClipData.newPlainText("Pregunta1", respuestaReclutamiento))
                    showNiceToast(context, context.getString(R.string.copiar_respuesta), NiceToastType.SUCCESS)
                }
            )

            Spacer(Modifier.height(12.dp))

            QuestionAnswerCard(
                numero = stringResource(R.string.pregunta_2_experiencia_titulo),
                pregunta = stringResource(R.string.pregunta_2_experiencia_text),
                respuesta = respuestaFacilidad,
                onCopy = {
                    clipboard.setPrimaryClip(ClipData.newPlainText("Pregunta2", respuestaFacilidad))
                    showNiceToast(context, context.getString(R.string.copiar_respuesta), NiceToastType.SUCCESS)
                }
            )

            Spacer(Modifier.height(12.dp))

            QuestionAnswerCard(
                numero = stringResource(R.string.pregunta_3_feedback_titulo),
                pregunta = stringResource(R.string.pregunta_3_feedback_text),
                respuesta = respuestaFeedback,
                onCopy = {
                    clipboard.setPrimaryClip(ClipData.newPlainText("Pregunta3", respuestaFeedback))
                    showNiceToast(context, context.getString(R.string.copiar_respuesta), NiceToastType.SUCCESS)
                }
            )

            Spacer(Modifier.height(12.dp))

            QuestionAnswerCard(
                numero = stringResource(R.string.pregunta_4_cambios_titulo),
                pregunta = stringResource(R.string.pregunta_4_cambios_text),
                respuesta = respuestaCambios,
                onCopy = {
                    clipboard.setPrimaryClip(ClipData.newPlainText("Pregunta4", respuestaCambios))
                    showNiceToast(context, context.getString(R.string.copiar_respuesta), NiceToastType.SUCCESS)
                }
            )

            Spacer(Modifier.height(12.dp))

            QuestionAnswerCard(
                numero = stringResource(R.string.pregunta_5_preparacion_titulo),
                pregunta = stringResource(R.string.pregunta_5_preparacion_text),
                respuesta = respuestaPreparacion,
                onCopy = {
                    clipboard.setPrimaryClip(ClipData.newPlainText("Pregunta5", respuestaPreparacion))
                    showNiceToast(context, context.getString(R.string.copiar_respuesta), NiceToastType.SUCCESS)
                }
            )

            if (app.esDePago) {
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(stringResource(R.string.guia_apps_pago_licencias), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text(stringResource(R.string.guia_apps_pago_desc), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Text(stringResource(R.string.cerrar), fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun QuestionAnswerCard(
    numero: String,
    pregunta: String,
    respuesta: String,
    onCopy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(numero, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
            Text(pregunta, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Text(respuesta, modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.bodySmall)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.copiar_respuesta), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
