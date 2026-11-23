
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    viewModel: AuthViewModel,
    navController: NavController? = null,
    onBack: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()

    LaunchedEffect(notifications) {
        if (notifications.any { !it.isRead }) {
            viewModel.markAllNotificationsAsRead()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notificaciones)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.volver)) }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { viewModel.markAllNotificationsAsRead() }) {
                            Icon(Icons.Default.DoneAll, contentDescription = "Marcar todas como leídas", tint = MaterialTheme.colorScheme.primary)
                        }
                        TextButton(onClick = { viewModel.clearAllNotifications() }) {
                            Text(stringResource(R.string.limpiar_todo), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_tienes_notificaciones_aun), color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(notifications) { notif ->
                    NotificationItem(notification = notif, onClick = {
                        viewModel.markNotificationAsRead(notif.id)
                        if (!notif.appId.isNullOrBlank()) {
                            navController?.navigate("app_detail/${notif.appId}")
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: UserNotification, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(notification.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    if (!notification.isRead) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(notification.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Text(
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(notification.timestamp)),
                    style = MaterialTheme.typography.labelSmall, color = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointsHistoryScreen(viewModel: AuthViewModel, onBack: () -> Unit) {
    val history by viewModel.pointsHistory.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadPointsHistory() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.historial_de_puntos)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.volver)) }
                }
            )
        }
    ) { padding ->
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_hay_transacciones_registradas), color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(history) { PointsTransactionItem(it) }
            }
        }
    }
}

@Composable
fun PointsTransactionItem(transaction: PointsTransaction) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val isPositive = transaction.amount > 0
            Surface(
                color = if (isPositive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                shape = CircleShape, modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.reason, fontWeight = FontWeight.Bold)
                Text(
                    SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(transaction.timestamp)),
                    style = MaterialTheme.typography.labelSmall, color = Color.Gray
                )
            }
            Text(
                text = "${if (isPositive) "+" else ""}${transaction.amount}",
                fontWeight = FontWeight.Black,
                color = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828), fontSize = 18.sp
            )
        }
    }
}

@Composable
fun BugReportDialog(onDismiss: () -> Unit, onSubmit: (device: String, steps: String, desc: String) -> Unit) {
    val autoDevice = remember { 
        "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} (Android ${android.os.Build.VERSION.RELEASE})"
    }
    var device by remember { mutableStateOf(autoDevice) }
    var steps by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        title = { Text(stringResource(R.string.reportar_un_error), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                OutlinedTextField(
                    value = device, 
                    onValueChange = { device = it }, 
                    label = { Text(stringResource(R.string.dispositivo_modelo_android)) }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = {
                        IconButton(onClick = { device = autoDevice }) {
                            Icon(Icons.Default.Refresh, "Reset", modifier = Modifier.size(18.dp))
                        }
                    }
                )
                OutlinedTextField(value = steps, onValueChange = { steps = it }, label = { Text(stringResource(R.string.pasos_para_reproducir)) }, minLines = 2, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text(stringResource(R.string.descripci_n_del_error)) }, minLines = 3, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(device, steps, desc) }, enabled = device.isNotBlank() && desc.isNotBlank(), modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), shape = RoundedCornerShape(12.dp)) {
                Text(stringResource(R.string.enviar_reporte), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.cancelar)) } }
    )
}

@Composable
fun ReportDialog(onDismiss: () -> Unit, onSubmit: (reason: String) -> Unit) {
    var reason by remember { mutableStateOf("") }
    val reasons = listOf(
        stringResource(R.string.spam), 
        stringResource(R.string.contenido_inapropiado), 
        stringResource(R.string.desarrollador_inactivo), 
        stringResource(R.string.estafa), 
        stringResource(R.string.otro)
    )
    var selectedReason by remember { mutableStateOf(reasons[0]) }
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        title = { Text(stringResource(R.string.reportar_problema), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                reasons.forEach { r ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(48.dp).clickable { selectedReason = r }) {
                        RadioButton(selected = selectedReason == r, onClick = { selectedReason = r })
                        Spacer(Modifier.width(8.dp))
                        Text(r)
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text(stringResource(R.string.detalles_adicionales_opcional)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            }
        },
        confirmButton = { Button(onClick = { onSubmit("$selectedReason: $reason") }, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), shape = RoundedCornerShape(12.dp)) { Text(stringResource(R.string.enviar_reporte), fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.cancelar)) } }
    )
}

@Composable
fun StreakBadge(streaks: Int) {
    if (streaks == 0) return
    Surface(color = Color(0xFFFF5722), shape = CircleShape) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Whatshot, null, modifier = Modifier.size(16.dp), tint = Color.White)
            Spacer(Modifier.width(4.dp))
            Text("$streaks", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun ASOSimulator(appName: String, developer: String, iconUrl: String?) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp).background(Color.White, RoundedCornerShape(16.dp)).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.vista_previa_play_store), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Surface(modifier = Modifier.size(72.dp), shape = RoundedCornerShape(12.dp), tonalElevation = 2.dp) {
                Box(modifier = Modifier.fillMaxSize().background(Color.LightGray))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(appName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(developer, color = Color(0xFF01875f), fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.contiene_anuncios_compras_integradas), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = {}, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF01875f)), shape = RoundedCornerShape(8.dp)) {
            Text(stringResource(R.string.instalar), color = Color.White)
        }
    }
}

@Composable
fun TestScenariosCard(tasks: List<String>) {
    ElevatedCard(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ListAlt, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.misiones_de_testing), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            tasks.forEachIndexed { index, task ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("${index + 1}.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(8.dp))
                    Text(task, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
