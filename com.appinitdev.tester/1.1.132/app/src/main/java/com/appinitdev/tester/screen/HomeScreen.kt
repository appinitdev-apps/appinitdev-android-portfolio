
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: AuthViewModel) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val userApps by viewModel.userApps.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var appToManage by remember { mutableStateOf<AppModel?>(null) }
    var appToEdit by remember { mutableStateOf<AppModel?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    var isTimeOut by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(5000) 
        isTimeOut = true
    }

    LaunchedEffect(isTimeOut, profile) {
        if (isTimeOut) {
            val currentProfile = profile
            if (currentProfile != null) {
                val isNameEmpty = currentProfile.devName.trim().isEmpty()
                val isContactEmpty = currentProfile.contacto.trim().isEmpty()
                if (isNameEmpty || isContactEmpty) {
                    navController.navigate("edit_profile") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            }
        }
    }

    val listState = rememberLazyListState()
    var showReputationInfo by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val communityApps by viewModel.communityApps.collectAsState()
    val activeAppsInTest = remember(userApps) { userApps.filter { it.status == "test" } }
    var showRecommendationsPopup by rememberSaveable { mutableStateOf(false) }
    var hasCheckedPopup by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
        viewModel.loadAndVerifyApps()
        viewModel.checkStreaks()
        viewModel.startObservingNotifications()
        viewModel.loadFeaturedApps()
        viewModel.startObservingCategorias(context)
        viewModel.loadCommunityApps(context)
        viewModel.debugCheckTestAppsMovedToProductionToday(context)
    }

    LaunchedEffect(activeAppsInTest) {
        if (activeAppsInTest.isNotEmpty() && !hasCheckedPopup) {
            hasCheckedPopup = true
            if (com.appinitdev.tester.Funciones.shouldShowInstallAppsPromptToday(context)) {
                showRecommendationsPopup = true
                com.appinitdev.tester.Funciones.saveInstallAppsPromptShownToday(context)
                triggerActiveAppNotification(context, activeAppsInTest.size)
            }
        }
    }

    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val recommendedApps = remember(communityApps, currentUserId) {
        communityApps.filter { app ->
            app.status == "test" &&
            app.idPropietario != currentUserId &&
            app.testers[currentUserId] == null &&
            (app.metodoTest == "google_group" || app.urlGoogleGroup.contains("groups.google.com"))
        }.sortedWith(
            compareByDescending<AppModel> { it.isBoosted }
                .thenByDescending { it.reputacion }
                .thenByDescending { it.fechaCreacion }
        ).take(5)
    }

    if (showRecommendationsPopup && activeAppsInTest.isNotEmpty()) {
        ActiveAppsRecommendationsDialog(
            activeAppsCount = activeAppsInTest.size,
            recommendedApps = recommendedApps,
            onTestAppClick = { app ->
                showRecommendationsPopup = false
                navController.navigate("app_detail/${app.id}")
            },
            onGoToReviewsClick = {
                showRecommendationsPopup = false
                selectedTab = 2
            },
            onDismiss = {
                showRecommendationsPopup = false
            }
        )
    }

    val app20DaysPrompt by viewModel.app20DaysPrompt.collectAsState()
    val app28DaysAlert by viewModel.app28DaysAlert.collectAsState()

    app20DaysPrompt?.let { app ->
        App20DaysPromptDialog(
            app = app,
            onKeepActive = { viewModel.keepAppActive20Days(app.id) },
            onHide = { viewModel.hideAppManual(app.id) },
            onMarkProduction = { viewModel.markAppAsProductionManual(app.id) },
            onCancel = { viewModel.dismiss20DaysPrompt(app.id) }
        )
    }

    app28DaysAlert?.let { app ->
        App28DaysAlertDialog(
            app = app,
            onDismiss = { viewModel.dismiss28DaysAlert() }
        )
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionState = rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
        val isGranted = permissionState.status.isGranted
        LaunchedEffect(permissionState) {
            if (!isGranted) {
                permissionState.launchPermissionRequest()
                viewModel.registrarTokenFCM()
            }
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.AppActionSuccess) {
            mostrarIntersticial(context)
            viewModel.clearState()
        }
    }

    if (showAddSheet) {
        AddAppBottomSheet(
            viewModel = viewModel,
            onDismiss = { showAddSheet = false }
        )
    }

    if (appToManage != null) {
        ManageAppBottomSheet(
            app = appToManage!!,
            viewModel = viewModel,
            onDismiss = { appToManage = null },
            onEdit = { appToEdit = appToManage }
        )
    }

    if (appToEdit != null) {
        AddAppBottomSheet(
            viewModel = viewModel,
            appToEdit = appToEdit,
            onDismiss = { appToEdit = null }
        )
    }

    val isExpanded by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            





        },
        floatingActionButton = {
            if (selectedTab != 2)
                ExtendedFloatingActionButton(
                    onClick = { showAddSheet = true },
                    expanded = isExpanded,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.registrar_nueva_app), fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        AdmobBanner()
                    }
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 0.dp
                    ) {
                        val items = listOf(
                            Triple(0, R.string.perfil, Icons.Default.Person),
                            Triple(1, R.string.test, Icons.Default.Build),
                            Triple(2, R.string.comunidades, Icons.Default.Group),
                            Triple(3, R.string.rese_as, Icons.Default.Stars)
                        )

                        items.forEach { (index, labelRes, icon) ->
                            NavigationBarItem(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                label = { Text(stringResource(labelRes), fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) },
                                icon = { 
                                    Icon(
                                        imageVector = icon, 
                                        contentDescription = null,
                                        tint = if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ) 
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val appsNoInstaladas by viewModel.appsNoInstaladas.collectAsState()
            if (appsNoInstaladas.isNotEmpty() && selectedTab == 1) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.tienes_apps_aceptadas_sin_instalar, appsNoInstaladas.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    profile?.let {
                        PullToRefreshBox(
                            isRefreshing = isRefreshing,
                            onRefresh = {
                                isRefreshing = true
                                coroutineScope.launch {
                                    viewModel.fetchUserProfile()
                                    viewModel.loadAndVerifyApps()
                                    delay(1000)
                                    isRefreshing = false
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column {
                                UserProfileCard(
                                    profile = it,
                                    viewModel = viewModel,
                                    onEditClick = { navController.navigate("edit_profile") },
                                    onReputationClick = { showReputationInfo = true },
                                    navController
                                )
                                LazyColumn(state = listState) {
                                    items(userApps) { app ->
                                        AppItemCard(
                                            app,
                                            viewModel,
                                            navController,
                                            onEditClick = { appToManage = it },
                                            onReputationClick = { showReputationInfo = true }
                                        )
                                    }
                                    item { Spacer(modifier = Modifier.height(80.dp)) }
                                }
                            }
                        }
                    } ?: Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp)
                    ) {
                        com.appinitdev.tester.util.ShimmerUserProfileCard()
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            repeat(3) {
                                com.appinitdev.tester.util.ShimmerAppItem()
                            }
                        }
                    }
                }
                1 -> CommunityAppsSection(viewModel = viewModel, navController = navController, listState = listState)
                2 -> CommunityLinksSection(viewModel = viewModel, navController)
                3 -> ReviewAppsScreen(viewModel = viewModel, navController = navController, listState = listState)
            }
        }
    }

    if (showReputationInfo) {
        ReputationExplanationDialog(
            profile = profile,
            onDismiss = { showReputationInfo = false }
        )
    }
}

@Composable
fun AppItemCard(
    app: AppModel,
    viewModel: AuthViewModel,
    navController: NavController,
    onEditClick: (AppModel) -> Unit,
    onReputationClick: () -> Unit,
) {
    val solicitudesCount = app.testers.values.count { it.validado == "Solicitud" }
    val tienePendientes = app.resenasProduccion.count { it.value.estado == "pendiente" }
    val fechaFormateada = remember(app.fechaCreacion) {
        java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            .format(java.util.Date(app.fechaCreacion))
    }
    OutlinedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { navController.navigate("app_detail/${app.id}") }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.size(56.dp)
                ) {
                    if (!app.icono.isNullOrBlank()) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(app.icono)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop,
                            error = { AsyncIdenticonAvatar(appName = app.paquete.lowercase(), modifier = Modifier.fillMaxSize()) }
                        )
                    } else {
                        AsyncIdenticonAvatar(appName = app.paquete.lowercase(), modifier = Modifier.fillMaxSize())
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = app.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = app.paquete, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Icon(Icons.Default.Group, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${app.testers.size}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(app.status)
                Box(modifier = Modifier.clickable(onClick = onReputationClick)) { ReputationBadge(app.reputacion) }
                if (solicitudesCount > 0) { PendingRequestsBadge(solicitudesCount) }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(text = fechaFormateada, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.weight(1f))
                val context = LocalContext.current
                if (app.urlAndroid.isNotBlank()) {
                    FilledTonalIconButton(
                        onClick = {
                            val urlLimpia = app.urlAndroid.trim()
                            if (android.util.Patterns.WEB_URL.matcher(urlLimpia).matches()) {
                                try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlLimpia))) } catch (_: Exception) {}
                            }
                        },
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Link, contentDescription = stringResource(R.string.url_play_store), modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                FilledTonalIconButton(
                    onClick = { onEditClick(app) },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = stringResource(R.string.gestionar_app), modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                BadgedBox(badge = { if (tienePendientes > 0) { Badge { Text(text = tienePendientes.toString()) } } }) {
                    FilledTonalIconButton(
                        onClick = { navController.navigate("autorizar_resenas/${app.id}") },
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (tienePendientes > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = if (tienePendientes > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = stringResource(R.string.rese_as), modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = { navController.navigate("app_detail/${app.id}") }, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = stringResource(R.string.detalle), modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun PendingRequestsBadge(count: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
    val scale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 1.20f, animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1000, easing = FastOutLinearInEasing), repeatMode = RepeatMode.Reverse), label = "scale_anim")
    val bgColor by infiniteTransition.animateColor(initialValue = Color(0xFFE3F2FD), targetValue = Color(0xFFBBDEFB), animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1000, easing = FastOutLinearInEasing), repeatMode = RepeatMode.Reverse), label = "color_anim")
    val primaryColor = Color(0xFF1565C0)
    Surface(color = bgColor, shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.4f)), modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp), tint = primaryColor)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "$count", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val isProduccion = status == "produccion"
    val containerColor = if (isProduccion) Color(0xFFE8F5E9) else Color(0xFFFFF8E1)
    val contentColor = if (isProduccion) Color(0xFF2E7D32) else Color(0xFFF57F17)
    Surface(color = containerColor, contentColor = contentColor, shape = RoundedCornerShape(8.dp)) {
        Text(text = status.uppercase(), modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
    }
}

@Composable
fun ReputationBadge(reputacion: Double) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), contentColor = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Icon(Icons.Default.Star, null, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "REP ${String.format("%.2f", reputacion)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

fun triggerActiveAppNotification(context: Context, activeCount: Int) {
    try {
        val channelId = "active_app_testing_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Recordatorios de Testeo",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, com.appinitdev.tester.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = android.graphics.BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(largeIcon)
            .setContentTitle(context.getString(R.string.tienes_apps_aceptadas_sin_instalar, activeCount))
            .setContentText(context.getString(R.string.sigue_probando_apps_y_sube_tu_reputaci_n))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    } catch (_: Exception) {}
}

@Composable
fun ActiveAppsRecommendationsDialog(
    activeAppsCount: Int,
    recommendedApps: List<AppModel>,
    onTestAppClick: (AppModel) -> Unit,
    onGoToReviewsClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {   },
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        ),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        icon = {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = stringResource(R.string.popup_reputacion_titulo),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Tienes $activeAppsCount app(s) activa(s) en testeo. " + stringResource(R.string.popup_reputacion_msg),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(12.dp),
                        lineHeight = 18.sp
                    )
                }

                if (recommendedApps.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.apps_recomendadas_acceso_inmediato),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recommendedApps.forEach { app ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onTestAppClick(app) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(40.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ) {
                                        if (!app.icono.isNullOrBlank()) {
                                            SubcomposeAsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(app.icono)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                                                contentScale = ContentScale.Crop,
                                                error = { AsyncIdenticonAvatar(appName = app.paquete.lowercase(), modifier = Modifier.fillMaxSize()) }
                                            )
                                        } else {
                                            AsyncIdenticonAvatar(appName = app.paquete.lowercase(), modifier = Modifier.fillMaxSize())
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = app.nombre,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = app.categoria,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    FilledTonalButton(
                                        onClick = { onTestAppClick(app) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(stringResource(R.string.probar_app), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = onGoToReviewsClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.ganar_puntos_resenas), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.cerrar), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun App20DaysPromptDialog(
    app: AppModel,
    onKeepActive: () -> Unit,
    onHide: () -> Unit,
    onMarkProduction: () -> Unit,
    onCancel: () -> Unit
) {
    val dias = remember(app.fechaCreacion) {
        ((System.currentTimeMillis() - app.fechaCreacion) / (24 * 60 * 60 * 1000L)).toInt()
    }
    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = stringResource(R.string.app_20_dias_titulo),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.app_20_dias_msg, app.nombre, dias),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(R.string.que_deseas_hacer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onKeepActive,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.seguir_en_prueba), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onMarkProduction,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(stringResource(R.string.ya_paso_a_produccion), fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onHide,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Text(stringResource(R.string.ocultar_app), fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.cancelar), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        dismissButton = null
    )
}

@Composable
fun App28DaysAlertDialog(
    app: AppModel,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = stringResource(R.string.app_28_dias_titulo),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = stringResource(R.string.app_28_dias_msg, app.nombre),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.entendido), fontWeight = FontWeight.Bold)
            }
        }
    )
}
