
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionAppsScreen(viewModel: AuthViewModel, navController: NavController) {
    val apps by viewModel.productionApps.collectAsState()
    val context = LocalContext.current


    
    LaunchedEffect(Unit) {
        viewModel.loadProductionApps()
    }

    
    val productionApps = remember(apps) {
        apps.filter { it.status == "produccion" }
            .sortedBy { it.nombre.lowercase() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.apps_en_producci_n_por_esta_herramienta)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.volver))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .padding(16.dp)) {
            Text(
                text = stringResource(R.string.apoya_a_la_comunidad),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.descarga_y_deja_una_rese_a_positiva_en_estas_aplicaciones_ya_publicadas),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (productionApps.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_hay_aplicaciones_en_producci_n_por_ahora), color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(productionApps) { app ->
                        ProductionAppItem(app) {
                            openPlayStore(context, app.paquete)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductionAppItem(app: AppModel, onClick: () -> Unit) {
    val formattedName = remember(app.nombre) {
        app.nombre.lowercase().replaceFirstChar { it.uppercase() }
    }

    
    OutlinedCard(
        onClick = onClick, 
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp), 
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = CardDefaults.outlinedCardBorder(enabled = true)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            Box(
                modifier = Modifier
                    .size(56.dp) 
                    .clip(MaterialTheme.shapes.small)
            ) {
                if (app.icono != null) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(app.icono)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Icono de ${app.nombre}",
                        modifier = Modifier.fillMaxSize(),
                        loading = { LetterLogo(formattedName, modifier = Modifier.fillMaxSize()) },
                        error = { LetterLogo(formattedName, modifier = Modifier.fillMaxSize()) }
                    )
                } else {
                    AsyncIdenticonAvatar(
                        appName = app.paquete.lowercase(),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formattedName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
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
            }

            
            if (app.icono != null) {
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.height(36.dp) 
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.descargar_o_hacer_rese_a),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}


fun openPlayStore(context: Context, packageName: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
