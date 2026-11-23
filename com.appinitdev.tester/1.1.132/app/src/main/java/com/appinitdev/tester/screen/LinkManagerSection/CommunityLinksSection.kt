@Composable
fun CommunityLinksSection(viewModel: AuthViewModel, navController: NavController) {
    val links by viewModel.globalLinks.collectAsState()
    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
    val context = androidx.compose.ui.platform.LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    val activity = context as? android.app.Activity
    val numApps by viewModel.productionCount.collectAsState()
    val numTesters by viewModel.testersCount.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.loadCommunityStats()
    }
    LaunchedEffect(Unit) {
        viewModel.startObservingGlobalLinks()
    }
    LaunchedEffect(Unit) {
        viewModel.iniciarBilling(context)
    }
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .clickable { navController.navigate("production_apps") }, 
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9) 
            ),
            border = BorderStroke(1.dp, Color(0xFF4CAF50))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF4CAF50), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$numApps " + stringResource(R.string.apps_en_producci_n),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = stringResource(R.string.apoya_con_una_rese_a_positiva_5_estrellas),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1B5E20)
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50)
                )
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .clickable { navController.navigate("ranking_screen") },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8EAF6) 
            ),
            border = BorderStroke(1.dp, Color(0xFF3F51B5)) 
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF3F51B5), CircleShape), 
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.ranking_de_testers) + "($numTesters)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A237E) 
                    )
                    Text(
                        text = stringResource(R.string.de_acuerdo_al_ranking_es_calculada_la_reputacion_de_cada_app),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF303F9F) 
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward, 
                    contentDescription = null,
                    tint = Color(0xFF3F51B5)
                )
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp)
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
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.ayuda_y_comunidad),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.encuentra_o_publica_enlaces_de_comunidades),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalIconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.size(44.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Añadir",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(links, key = { it.id }) { link ->
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val urlString = link.url.trim()
                            if (urlString.isNotEmpty()) {
                                try {
                                    val finalUrl =
                                        if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                                            "https://$urlString"
                                        } else {
                                            urlString
                                        }
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse(finalUrl)
                                    )
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    android.util.Log.e("LinkError", "URL inválida: ${link.url}")
                                }
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when (link.tag) {
                            "WhatsApp" -> Icons.AutoMirrored.Filled.Chat
                            "Telegram" -> Icons.AutoMirrored.Filled.Send
                            "Reddit" -> Icons.AutoMirrored.Filled.Chat
                            "Discord" -> Icons.AutoMirrored.Filled.Chat
                            else -> Icons.Default.Link
                        }
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = link.titulo,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = link.tag.uppercase(),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (link.ownerId == currentUserId) {
                            FilledTonalIconButton(
                                onClick = { viewModel.deleteGlobalLink(link.id) },
                                modifier = Modifier.size(36.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Borrar",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    val urlString = link.url.trim()
                                    if (urlString.isNotEmpty()) {
                                        try {
                                            val finalUrl =
                                                if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
                                                    "https://$urlString"
                                                } else {
                                                    urlString
                                                }
                                            val intent = android.content.Intent(
                                                android.content.Intent.ACTION_VIEW,
                                                android.net.Uri.parse(finalUrl)
                                            )
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            android.util.Log.e("LinkError", "URL inválida: ${link.url}")
                                        }
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (showAddDialog) {
        AddLinkDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { titulo, url, tag ->
                viewModel.addGlobalLink(titulo, url, tag)
                showAddDialog = false
            }
        )
    }
}
@Composable
fun AddLinkDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var titulo by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("WhatsApp") } 
    val tags = listOf("WhatsApp", "Telegram", "Reddit", "Discord", "Web")
    val isUrlValid = remember(url) {
        url.isNotBlank() && android.util.Patterns.WEB_URL.matcher(url).matches()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        title = { Text(stringResource(R.string.nuevo_enlace), fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                OutlinedTextField(
                    value = titulo, 
                    onValueChange = { titulo = it }, 
                    label = { Text(stringResource(R.string.t_tulo)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(R.string.url_ejemplo)) },
                    isError = url.isNotEmpty() && !isUrlValid,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    supportingText = {
                        if (url.isNotEmpty() && !isUrlValid) {
                            Text(stringResource(R.string.url_error_hint), color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                Text(stringResource(R.string.plataforma), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.forEach { t ->
                        FilterChip(
                            selected = tag == t,
                            onClick = { tag = t },
                            label = { Text(t) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val finalUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    "https://${url.trim()}"
                } else {
                    url.trim()
                }
                onConfirm(titulo, finalUrl, tag) },
                enabled = titulo.isNotBlank() && isUrlValid,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.guarder), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.cancelar)) } }
    )
}
