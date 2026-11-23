
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityAppsSection(
    viewModel: AuthViewModel,
    navController: NavController,
    listState: LazyListState,
) {
    val isLoading by viewModel.isUploadingCommunityApps.collectAsState()
    val categoriasBase by viewModel.categoriasApps.collectAsState()
    val apps by viewModel.communityApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val user = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showFilterSheet by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf("priority") } // "priority", "recent", "reputation", "name"
    var filterByInstalled by remember { mutableStateOf("all") } // "all", "installed", "not_installed"
    var quickFilter by remember { mutableStateOf("all") } // "all", "google_group", "paid", "near_20"

    LaunchedEffect(Unit) {
        viewModel.loadCommunityApps(context)
    }

    val featuredApps = remember(apps) { apps.filter { it.isBoosted }.take(5) }

    val categorias = remember(categoriasBase, searchQuery, sortBy, filterByInstalled, quickFilter, context) {
        fun procesarLista(list: List<AppModel>): List<AppModel> {
            var res = if (searchQuery.isBlank()) list
            else list.filter {
                it.nombre.contains(searchQuery, ignoreCase = true) ||
                it.categoria.contains(searchQuery, ignoreCase = true)
            }

            res = when (filterByInstalled) {
                "installed" -> res.filter { Funciones.isAppInstalled(context, it.paquete) }
                "not_installed" -> res.filter { !Funciones.isAppInstalled(context, it.paquete) }
                else -> res
            }

            res = when (quickFilter) {
                "google_group" -> res.filter { it.metodoTest == "google_group" || it.urlGoogleGroup.isNotBlank() }
                "paid" -> res.filter { it.esDePago }
                "near_20" -> res.filter { it.testers.size in 5..19 }
                else -> res
            }

            return when (sortBy) {
                "priority" -> res.sortedWith(
                    compareByDescending<AppModel> { it.isBoosted }
                        .thenByDescending { it.reputacion }
                        .thenByDescending { it.fechaCreacion }
                )
                "reputation" -> res.sortedByDescending { it.reputacion }
                "name" -> res.sortedBy { it.nombre.lowercase() }
                "recent" -> res.sortedByDescending { it.fechaCreacion }
                else -> res
            }
        }

        com.appinitdev.tester.model.CategoriasApps(
            explorar = procesarLista(categoriasBase.explorar),
            aceptadas = procesarLista(categoriasBase.aceptadas),
            pendientes = procesarLista(categoriasBase.pendientes),
            rechazadas = procesarLista(categoriasBase.rechazadas)
        )
    }

    val tabs = remember(categorias) {
        val list = mutableListOf(
            Triple(1, R.string.mis_grupos_de_chat, categorias.aceptadas),
            Triple(0, R.string.explorar_nuevas_apps, categorias.explorar)
        )
        if (categorias.pendientes.isNotEmpty()) {
            list.add(Triple(2, R.string.solicitudes, categorias.pendientes))
        }
        if (categorias.rechazadas.isNotEmpty()) {
            list.add(Triple(3, R.string.rechazadas, categorias.rechazadas))
        }
        list
    }

    val initialPageIndex = remember(categoriasBase.aceptadas) {
        val misGruposIdx = tabs.indexOfFirst { it.first == 1 }
        val explorarIdx = tabs.indexOfFirst { it.first == 0 }
        if (categoriasBase.aceptadas.isEmpty() && explorarIdx >= 0) explorarIdx
        else if (misGruposIdx >= 0) misGruposIdx
        else 0
    }

    val pagerState = rememberPagerState(
        initialPage = initialPageIndex,
        pageCount = { tabs.size }
    )

    var initialTargetSet by remember { mutableStateOf(false) }
    LaunchedEffect(isLoading, categoriasBase.aceptadas) {
        if (!isLoading && !initialTargetSet) {
            initialTargetSet = true
            val explorarIdx = tabs.indexOfFirst { it.first == 0 }
            val misGruposIdx = tabs.indexOfFirst { it.first == 1 }
            val targetIndex = if (categoriasBase.aceptadas.isEmpty()) explorarIdx else misGruposIdx
            if (targetIndex >= 0 && pagerState.currentPage != targetIndex) {
                pagerState.scrollToPage(targetIndex)
            }
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }

    if (showFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                Text(stringResource(R.string.filtros), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                
                Text(stringResource(R.string.ordenar_por), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = sortBy == "priority", onClick = { sortBy = "priority" }, label = { Text(stringResource(R.string.recomendadas_prioridad)) })
                    FilterChip(selected = sortBy == "recent", onClick = { sortBy = "recent" }, label = { Text(stringResource(R.string.mas_recientes)) })
                    FilterChip(selected = sortBy == "reputation", onClick = { sortBy = "reputation" }, label = { Text(stringResource(R.string.mejor_reputacion)) })
                    FilterChip(selected = sortBy == "name", onClick = { sortBy = "name" }, label = { Text(stringResource(R.string.nombre)) })
                }
                
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.filtrar), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = filterByInstalled == "all", onClick = { filterByInstalled = "all" }, label = { Text(stringResource(R.string.todas)) })
                    FilterChip(selected = filterByInstalled == "installed", onClick = { filterByInstalled = "installed" }, label = { Text(stringResource(R.string.solo_instaladas)) })
                    FilterChip(selected = filterByInstalled == "not_installed", onClick = { filterByInstalled = "not_installed" }, label = { Text(stringResource(R.string.solo_no_instaladas)) })
                }
                
                Spacer(Modifier.height(32.dp))
                Button(onClick = { showFilterSheet = false }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.listo))
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                viewModel.loadCommunityApps()
                kotlinx.coroutines.delay(1000)
                isRefreshing = false
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- CARRUSEL DESTACADOS ---
        if (featuredApps.isNotEmpty() && tabs.getOrNull(pagerState.currentPage)?.first == 0 && searchQuery.isEmpty()) {
            Text(
                stringResource(R.string.apps_destacadas),
                modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            FeaturedCarousel(featuredApps, navController)
        }

        // --- BUSCADOR ---
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.buscar_por_nombre_o_categor_ia)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { showFilterSheet = true }) {
                BadgedBox(badge = { if (sortBy != "recent" || filterByInstalled != "all" || quickFilter != "all") Badge() }) {
                    Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.filtros))
                }
            }
        }

        // --- FILTROS RÁPIDOS (CHIPS) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = quickFilter == "all",
                onClick = { quickFilter = "all" },
                label = { Text(stringResource(R.string.chip_todas), fontSize = 12.sp) }
            )
            FilterChip(
                selected = quickFilter == "google_group",
                onClick = { quickFilter = if (quickFilter == "google_group") "all" else "google_group" },
                label = { Text(stringResource(R.string.chip_acceso_inmediato), fontSize = 12.sp) }
            )
            FilterChip(
                selected = quickFilter == "near_20",
                onClick = { quickFilter = if (quickFilter == "near_20") "all" else "near_20" },
                label = { Text(stringResource(R.string.chip_cerca_20_testers), fontSize = 12.sp) }
            )
            FilterChip(
                selected = quickFilter == "paid",
                onClick = { quickFilter = if (quickFilter == "paid") "all" else "paid" },
                label = { Text(stringResource(R.string.chip_de_pago), fontSize = 12.sp) }
            )
        }

        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 16.dp,
            divider = {},
            indicator = { tabPositions ->
                if (pagerState.currentPage < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, (_, labelRes, _) ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(stringResource(labelRes)) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val (_, _, listaAMostrar) = tabs[page]
            val tabListState = androidx.compose.foundation.lazy.rememberLazyListState()

            ListaDeAppsComponente(
                apps = listaAMostrar,
                isJoined = tabs[page].first == 1,
                user = user,
                viewModel = viewModel,
                navController = navController,
                tabTitle = stringResource(tabs[page].second),
                listState = tabListState,
                isLoading = isLoading
            )
        }
    }
}
}

@Composable
fun ListaDeAppsComponente(
    apps: List<AppModel>,
    isJoined: Boolean,
    user: com.google.firebase.auth.FirebaseUser?,
    viewModel: AuthViewModel,
    navController: NavController,
    tabTitle: String,
    listState: LazyListState,
    isLoading: Boolean
) {
    val context = LocalContext.current
    if (isLoading) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(5) { ShimmerAppItem() }
        }
    } else if (apps.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_se_encontraron_aplicaciones), color = Color.Gray)
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(apps, key = { it.id }) { app ->
                CommunityAppItem(
                    app = app,
                    isJoined = isJoined,
                    user = user,
                    onClick = {
                        navController.navigate("app_detail/${app.id}")
                    },
                    tabTitle = tabTitle
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun CommunityAppItem(
    app: AppModel,
    isJoined: Boolean,
    user: com.google.firebase.auth.FirebaseUser?,
    onClick: () -> Unit,
    tabTitle: String,
) {
    val currentUserId = user?.uid ?: ""
    val context = LocalContext.current
    val estaInstalada = remember(app.paquete) { Funciones.isAppInstalled(context, app.paquete) }
    val fechaLegible = remember(app.fechaCreacion) {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        sdf.format(java.util.Date(app.fechaCreacion))
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                    if (!app.icono.isNullOrBlank()) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(app.icono)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop,
                            error = {
                                AsyncIdenticonAvatar(
                                    appName = app.paquete.lowercase(),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        )
                    } else {
                        AsyncIdenticonAvatar(
                            appName = app.paquete.lowercase(),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
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
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = app.categoria.uppercase(),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = fechaLegible,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isJoined && !estaInstalada) {
                        FaltaInstalarBadge()
                    }
                    FilledTonalIconButton(
                        onClick = onClick,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = stringResource(R.string.ver_mas),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${app.allParticipants.size + 1} ${stringResource(R.string.testers)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (isJoined) {
                        val dias = app.testers[currentUserId]?.dias ?: 0
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${dias}D",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${stringResource(R.string.reputaci_n)} ${String.format("%.2f", app.reputacion)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FaltaInstalarBadge(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Surface(
        modifier = modifier.graphicsLayer(scaleX = scale, scaleY = scale),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = stringResource(R.string.falta_instalar).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperProfileModal(developerId: String, viewModel: AuthViewModel, onDismiss: () -> Unit) {
    var profile by remember { mutableStateOf<com.appinitdev.tester.model.UserProfile?>(null) }
    LaunchedEffect(developerId) { profile = viewModel.getProfileById(developerId) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) { Text(profile?.devName?.take(1)?.uppercase() ?: "?", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(profile?.devName ?: stringResource(R.string.revisando), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.desarrollador_verificado), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem(stringResource(R.string.nivel), profile?.nivel?.toInt()?.toString() ?: "0")
                StatItem(stringResource(R.string.reputaci_n), String.format("%.1f", profile?.reputacion ?: 0.0))
                StatItem(stringResource(R.string.puntos), profile?.puntos?.toString() ?: "0")
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text(stringResource(R.string.cerrar_perfil)) }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun FeaturedCarousel(apps: List<AppModel>, navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { apps.size })
    
    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 32.dp),
        pageSpacing = 16.dp,
        modifier = Modifier.padding(vertical = 12.dp)
    ) { page ->
        val app = apps[page]
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clickable { navController.navigate("app_detail/${app.id}") },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    if (!app.icono.isNullOrBlank()) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(app.icono)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop,
                            error = { AsyncIdenticonAvatar(appName = app.paquete, modifier = Modifier.fillMaxSize()) }
                        )
                    } else {
                        AsyncIdenticonAvatar(appName = app.paquete, modifier = Modifier.fillMaxSize())
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(app.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(app.categoria, style = MaterialTheme.typography.labelSmall)
                }
                Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color(0xFFFFD700))
            }
        }
    }
}
