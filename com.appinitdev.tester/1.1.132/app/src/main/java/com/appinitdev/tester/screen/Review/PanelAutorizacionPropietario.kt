
// PanelAutorizacionPropietario.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun PanelAutorizacionPropietario(
    appId: String,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
) {
    val userApps by viewModel.userApps.collectAsState()
    //val app = remember(userApps, appId) { userApps.find { it.id == appId } }

    // Estado para filtrar: 0 = Pendientes, 1 = Aprobadas
    var selectedFilter by remember { mutableIntStateOf(0) }
    val app by remember(userApps, appId) {
        derivedStateOf { userApps.find { it.id == appId } }
    }

    val resenasFiltradas by remember(app, selectedFilter) {
        derivedStateOf {
            val estadoBuscado = when(selectedFilter) {
                0 -> "pendiente"
                1 -> "aprobada"
                else -> "rechazada" // Nuevo estado
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

        // Filtramos las reseñas según el Tab seleccionado
//        val resenasFiltradas = remember(app, selectedFilter) {
//            val estadoBuscado = if (selectedFilter == 0) "pendiente" else "aprobada"
//            app.resenasProduccion.filter { it.value.estado == estadoBuscado }.toList()
//        }

        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            // --- SELECTOR DE TABS (Pendientes vs Aprobadas) ---
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
                            estadoActual = selectedFilter, // 0=Pendiente, 1=Aprobada, 2=Rechazada
                            onAprobar = {
                                viewModel.aprobarResenaProduccion(app!!.id, testerId, resena)
                            },
                            onRechazar = {
                                // Cambia el estado a "rechazada" en Firebase
                                viewModel.actualizarEstadoResena(app!!.id, testerId, "rechazada")
                            },
                            onReevaluar = {
                                // Devuelve el estado a "pendiente" para que aparezca en la primera pestaña
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
    testerId: String, // Necesario para identificar a quién pertenece la reseña
    estadoActual: Int, // 0: Pendiente, 1: Aprobada, 2: Rechazada
    onAprobar: () -> Unit,
    onRechazar: () -> Unit,
    onReevaluar: () -> Unit // Nueva acción para mover de Rechazado -> Pendiente
) {
    val context = LocalContext.current
    var showFullImage by remember { mutableStateOf(false) }
    var procesando by remember { mutableStateOf(false) }

    // Definimos colores de fondo según el estado para dar feedback visual rápido
    val backgroundColor = when (estadoActual) {
        0 -> Color(0xFFF1F8E9) // Verde muy claro (Pendiente)
        1 -> Color(0xFFE8EAF6) // Azul muy claro (Aprobada)
        else -> Color(0xFFFFEBEE) // Rojo muy claro (Rechazada)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // --- MINIATURA DE EVIDENCIA ---
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

                // --- INFORMACIÓN DEL TESTER ---
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

                // --- ACCIONES SEGÚN EL ESTADO ---
                Box(contentAlignment = Alignment.Center) {
//                    if (procesando) {
//                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
//                    } else {
                        when (estadoActual) {
                            0 -> { // ESTADO: PENDIENTE
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Botón Rechazar (Icono)
                                    IconButton(
                                        onClick = {
                                            procesando = true
                                            onRechazar()
                                        },
                                        modifier = Modifier.background(Color.White, RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.rechazar), tint = Color.Red)
                                    }
                                    // Botón Aprobar
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
                            1 -> { // ESTADO: APROBADA
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = stringResource(R.string.aprobadas),
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            2 -> { // ESTADO: RECHAZADA
                                // Botón para volver a poner en pendientes (Reevaluar)
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
                   // }
                }
            }
        }
    }

    // --- MODAL DE IMAGEN COMPLETA ---
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