@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelAutorizacionPropietario(
    appId: String,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
) {
    val userApps by viewModel.userApps.collectAsState()
    var selectedFilter by remember { mutableIntStateOf(0) }
    val app by remember(userApps, appId) {
        derivedStateOf { userApps.find { it.id == appId } }
    }
    val resenasFiltradas by remember(app, selectedFilter) {
        derivedStateOf {
            val estadoBuscado = when(selectedFilter) {
                0 -> "pendiente"
                1 -> "aprobada"
                else -> "rechazada" 
            }
            app?.resenasProduccion?.filter { it.value.estado == estadoBuscado }?.toList() ?: emptyList()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.gesti_n_de_rese_as), style = MaterialTheme.typography.titleLarge)
                        Text(
                            app?.nombre ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        if (app == null) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            TabRow(selectedTabIndex = selectedFilter) {
                Tab(selected = selectedFilter == 0, onClick = { selectedFilter = 0 }) {
                    val count = app!!.resenasProduccion.values.count { it.estado == "pendiente" }
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.pendientes))
                        if (count > 0) {
                            Badge(Modifier.padding(start = 4.dp)) { Text(count.toString()) }
                        }
                    }
                }
                Tab(selected = selectedFilter == 1, onClick = { selectedFilter = 1 }) {
                    Text(stringResource(R.string.aprobadas), modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedFilter == 2, onClick = { selectedFilter = 2 }) {
                    Text(stringResource(R.string.rechazadas), modifier = Modifier.padding(16.dp))
                }
            }
            if (resenasFiltradas.isEmpty()) {
                Box(Modifier
                    .fillMaxSize()
                    .weight(1f), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_hay_rese_as_en_esta_categor_a), color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(resenasFiltradas) { (testerId, resena) ->
                        ResenaItemCard(
                            resena = resena,
                            testerId = testerId,
                            estadoActual = selectedFilter, 
                            onAprobar = {
                                viewModel.aprobarResenaProduccion(app!!.id, testerId, resena)
                            },
                            onRechazar = {
                                viewModel.actualizarEstadoResena(app!!.id, testerId, "rechazada")
                            },
                            onReevaluar = {
                                viewModel.actualizarEstadoResena(app!!.id, testerId, "pendiente")
                            }
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun ResenaItemCard(
    resena: ResenaProduccion,
    testerId: String, 
    estadoActual: Int, 
    onAprobar: () -> Unit,
    onRechazar: () -> Unit,
    onReevaluar: () -> Unit 
) {
    val context = LocalContext.current
    var showFullImage by remember { mutableStateOf(false) }
    var procesando by remember { mutableStateOf(false) }
    val backgroundColor = when (estadoActual) {
        0 -> Color(0xFFF1F8E9) 
        1 -> Color(0xFFE8EAF6) 
        else -> Color(0xFFFFEBEE) 
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = resena.evidenciaUrl,
                    contentDescription = "Evidencia",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                        .clickable { showFullImage = true },
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        resena.userName ?: "Tester",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1A237E)
                    )
                    Text(
                        text = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm")
                            .format(java.util.Date(resena.fecha)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Box(contentAlignment = Alignment.Center) {
                        when (estadoActual) {
                            0 -> { 
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = {
                                            procesando = true
                                            onRechazar()
                                        },
                                        modifier = Modifier.background(Color.White, RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.rechazar), tint = Color.Red)
                                    }
                                    Button(
                                        onClick = {
                                            procesando = true
                                            onAprobar()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(stringResource(R.string.aprobar))
                                    }
                                }
                            }
                            1 -> { 
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = stringResource(R.string.aprobadas),
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            2 -> { 
                                OutlinedButton(
                                    onClick = {
                                        procesando = true
                                        onReevaluar()
                                    },
                                    border = BorderStroke(1.dp, Color.Gray),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(stringResource(R.string.revisar_de_nuevo), fontSize = 12.sp)
                                }
                            }
                        }
                }
            }
        }
    }
    if (showFullImage) {
        Dialog(onDismissRequest = { showFullImage = false }) {
            Card(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = resena.evidenciaUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 450.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { showFullImage = false }) {
                        Text(stringResource(R.string.cerrar))
                    }
                }
            }
        }
    }
}