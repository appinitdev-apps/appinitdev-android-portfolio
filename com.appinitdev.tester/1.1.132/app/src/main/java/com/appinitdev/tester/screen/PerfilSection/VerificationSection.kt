
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationSection(
    viewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsState()
    val icons by viewModel.fixedAppsIcons.collectAsState()
    // Verificamos el estado de instalación de cada app de la lista
    val appsStatus = remember(profile) {
        DevAppsConfig.developerApps.shuffled().map { app ->
            val instalada = com.appinitdev.tester.Funciones.isAppInstalled(context, app.paquete)
            Triple(app.nombre, app.paquete, instalada)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadFixedAppsIcons(DevAppsConfig.developerApps.map { it.paquete })
    }

    LaunchedEffect(appsStatus) {
        // Esto se ejecutará cada vez que appsStatus cambie
        // o al entrar a la pantalla por primera vez.
        viewModel.actualizarNivelPorInstalacionesFijas(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.centro_de_nivel)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(


                    innerPadding
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    stringResource(R.string.centro_de_nivel),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.tu_nivel_depende_de_cu_ntas_apps_de_esta_lista_tengas_instaladas),
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Card de Estado de Nivel
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.nivel_actual),
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                "${profile?.nivel?.toInt() ?: 1}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Button(onClick = { viewModel.actualizarNivelPorInstalacionesFijas(context) }) {
                            Icon(Icons.Default.CheckCircle, null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.sincronizar))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(appsStatus) { (nombre, paquete, instalada) ->
                        Log.e("Dataghj", "$nombre -> $paquete -> $instalada")
                        val iconoUrl = icons[paquete]
                        AppVerificationItem(nombre, paquete, instalada, iconoUrl)
                    }
                }
            }
        }
    }
}

@Composable
fun AppVerificationItem(
    nombre: String,
    paquete: String,
    estaInstalada: Boolean,
    iconoUrl: String? = null,
) {
    val context = LocalContext.current
    val formattedName = nombre.lowercase().replaceFirstChar { it.uppercase() }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (estaInstalada)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.surface,
        tonalElevation = if (estaInstalada) 0.dp else 2.dp,
        border = if (estaInstalada)
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        else null
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- NUEVO: Icono de la App o Logo de Letra ---
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                // Dentro de ProductionAppItem en ProductionAppsScreen.kt

                SubcomposeAsyncImage(

                    model = ImageRequest.Builder(LocalContext.current)

                        .data(iconoUrl)

                        .crossfade(true) // Hace que el cambio de letra a icono sea suave

                        .build(),

                    contentDescription = "Icono de ${nombre}",

                    modifier = Modifier

                        .fillMaxSize()

                        .clip(RoundedCornerShape(8.dp)),

                    loading = {
                        LetterLogo(formattedName, modifier = Modifier.fillMaxSize())
                    },
                    error = {
                        LetterLogo(formattedName, modifier = Modifier.fillMaxSize())
                    }

                )

                // Overlay: Si está instalada, mostramos un check pequeño en la esquina
                if (estaInstalada) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.BottomEnd // <--- Mueve la paloma a la esquina inferior derecha
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier
                                .size(18.dp) // Círculo un poco más pequeño para la esquina
                                .offset(x = -1.dp, y = -1.dp), // Sobresale un poquito para que se vea más moderno
                            shadowElevation = 2.dp // Sombra pequeña para que resalte del icono
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle, // CheckCircle se ve más limpio en esquinas
                                contentDescription = null,
                                tint = Color(0xFF2E7D32), // Verde intenso
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formattedName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (estaInstalada) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = paquete,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
            }

            if (!estaInstalada) {
                Button(
                    onClick = {
                        val intent =
                            android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                data = "https://play.google.com/store/apps/details?id=$paquete".toUri()
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text(
                        stringResource(R.string.instalar),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                ) {
                    Text(
                        text = stringResource(R.string.listo),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

