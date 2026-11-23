@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val prefsManager = remember { PrefsManager(context) }
    val scope = rememberCoroutineScope()
    val showFeaturesInitial by prefsManager.shouldShowFeatures.collectAsState(initial = null)
    val scrollState = rememberScrollState()
    var displayDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val isKeyboardOpen = WindowInsets.isImeVisible
    LaunchedEffect(isKeyboardOpen) {
        if (isKeyboardOpen) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }
    LaunchedEffect(Unit) {
        if (uiState !is AuthUiState.RegisterSuccess) {
            viewModel.clearState()
        }
    }
    val casiListo = stringResource(R.string.registro_casi_listo_por_favor_verifica_tu_correo_antes_de_iniciar_sesi_n_si_no_encuentras_el_correo_revisa_la_seccion_de_spam)
    var infoMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.RegisterSuccess) {
            infoMessage = casiListo
            viewModel.clearState()
        }
        when (uiState) {
            is AuthUiState.Success -> {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
                viewModel.clearState()
            }
            is AuthUiState.NeedsProfile -> {
                navController.navigate("edit_profile") {
                    popUpTo("login") { inclusive = true }
                }
                viewModel.clearState()
            }
            else -> {}
        }
    }
    LaunchedEffect(showFeaturesInitial) {
        if (showFeaturesInitial == true) {
            displayDialog = true
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Logo",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.pruebas_reales_usuarios_reales).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(48.dp))
            if (displayDialog) {
                TesterFeaturesDialog(
                    onDismiss = {
                        displayDialog = false
                        scope.launch {
                            prefsManager.markFeaturesAsRead()
                        }
                    }
                )
            }
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.bienvenido),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(R.string.correo_electr_nico)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Email, null) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.contrase_a)) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    if (uiState is AuthUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                    } else {
                        Button(
                            onClick = { viewModel.login(email.trim(), password) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.iniciar_sesi_n),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { navController.navigate("forgot_password") }) {
                        Text(
                            text = stringResource(R.string.olvid_mi_contrase_a),
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.no_tienes_cuenta),
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = { navController.navigate("register") }) {
                    Text(
                        text = stringResource(R.string.reg_strate),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (infoMessage != null) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = infoMessage!!,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
            if (uiState is AuthUiState.Error) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = (uiState as AuthUiState.Error).msg ?: stringResource(R.string.ocurri_un_error),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
