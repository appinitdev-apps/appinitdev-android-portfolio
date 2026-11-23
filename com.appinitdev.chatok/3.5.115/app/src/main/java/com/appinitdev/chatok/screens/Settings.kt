
@Composable
fun Settings(navController: NavController, state: AppState, viewModel: ChatViewModel) {
    val context = LocalContext.current
    val privacyPrefs = remember { context.getSharedPreferences("privacy_prefs", Context.MODE_PRIVATE) }
    
    var biometricLock by remember {
        mutableStateOf(privacyPrefs.getBoolean("biometric_lock", false))
    }
    var rr by remember {
        mutableStateOf(state.userData?.pref?.rr ?: true)
    }
    var ov by remember {
        mutableStateOf(state.userData?.pref?.online ?: true)
    }
    var autoTranslate by remember {
        mutableStateOf(privacyPrefs.getBoolean("auto_translate", false))
    }
    var isVendedora by remember {
        mutableStateOf(state.userData?.pref?.verificada ?: false)
    }
    var showVerificationDialog by remember { mutableStateOf(false) }

    val brush = Brush.linearGradient(listOf(Color(0xFF238CDD), Color(0xFF1952C4)))

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.blurry_gradient_haikei),
            contentDescription = "",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Dark Overlay
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBackIosNew, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.configuracionesq),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // Section 1: Customization
                Text(
                    text = stringResource(R.string.apariencia),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF238CDD),
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )

                SettingOption(
                    title = stringResource(R.string.tienda_monedas),
                    description = stringResource(R.string.tienda_monedas_desc),
                    onClick = { navController.navigate("store") },
                    painter = painterResource(R.drawable.correct) // Need a coin icon ideally
                )
                
                SettingOption(
                    title = stringResource(R.string.personalizaci_n1),
                    description = stringResource(R.string.personaliza_tu_pantalla_de_chat),
                    onClick = { navController.navigate("cus") },
                    painter = painterResource(R.drawable.equalizer)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Section 2: Account & Security
                Text(
                    text = stringResource(R.string.cuenta_seguridad),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF238CDD),
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )

                SettingOption(
                    title = stringResource(R.string.usuarios_bloqueados),
                    description = stringResource(R.string.ver_y_administrar_usuarios_bloqueados),
                    onClick = { navController.navigate("blck") },
                    painter = painterResource(id = R.drawable.block_user_6380092)
                )
                
                SettingOption(
                    title = stringResource(R.string.mensajes_programadosw),
                    description = stringResource(R.string.administrar_mensajes_programados),
                    onClick = { navController.navigate("scheduled") },
                    painter = painterResource(id = R.drawable.message_time)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Section 3: Privacy
                Text(
                    text = stringResource(R.string.privacidad),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF238CDD),
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )

                SettingSwitch(
                    title = stringResource(R.string.confirmaciones_de_lectura),
                    description = stringResource(R.string.activar_o_desactivar_los_recibos_de_lectura),
                    checked = rr,
                    onCheckedChange = {
                        rr = it
                        state.userData?.let { user ->
                            viewModel.updatePref(user, user.pref.copy(rr = it))
                        }
                    },
                    painter = painterResource(id = R.drawable.correct)
                )

                SettingSwitch(
                    title = stringResource(R.string.online_visibility),
                    description = stringResource(R.string.manage_online_visibility),
                    checked = ov,
                    onCheckedChange = {
                        ov = it
                        state.userData?.let { user ->
                            viewModel.updatePref(user, user.pref.copy(online = it))
                            viewModel.updateStatus(it, v = true)
                        }
                    },
                    painter = painterResource(id = R.drawable.eye)
                )

                SettingSwitch(
                    title = stringResource(R.string.traduccion_automatica),
                    description = stringResource(R.string.traduccion_automatica_desc),
                    checked = autoTranslate,
                    onCheckedChange = {
                        autoTranslate = it
                        privacyPrefs.edit().putBoolean("auto_translate", it).apply()
                    },
                    painter = painterResource(id = R.drawable.correct) // Replace with translation icon if available
                )

                SettingSwitch(
                    title = stringResource(id = R.string.eres_vendedora),
                    description = stringResource(R.string.vendedora_desc),
                    checked = isVendedora,
                    onCheckedChange = {
                        isVendedora = it
                        viewModel.toggleVendedora(it)
                    },
                    painter = painterResource(id = R.drawable.correct)
                )

                if (BiometricHelper.isBiometricAvailable(context)) {
                    SettingSwitch(
                        title = stringResource(R.string.bloqueo_biometrico),
                        description = stringResource(R.string.bloqueo_biometrico_desc),
                        checked = biometricLock,
                        onCheckedChange = {
                            biometricLock = it
                            privacyPrefs.edit().putBoolean("biometric_lock", it).apply()
                        },
                        painter = painterResource(id = R.drawable.block_user_6380092)
                    )
                }

                if (state.userData?.pref?.isVerified != true) {
                    SettingOption(
                        title = stringResource(R.string.verificar_cuenta),
                        description = stringResource(R.string.solicitar_insignia_desc),
                        onClick = { showVerificationDialog = true },
                        painter = painterResource(id = R.drawable.correct)
                    )
                }
                
                if (state.userData?.isAdmin == true) {
                    SettingOption(
                        title = stringResource(R.string.panel_administracion),
                        description = stringResource(R.string.panel_administracion_desc),
                        onClick = { navController.navigate("admin") },
                        painter = painterResource(id = R.drawable.equalizer)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
                AdBanner()
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (showVerificationDialog) {
            com.appinitdev.chatok.Dialogs.VerificationRequestDialog(
                state = state,
                onDismiss = { showVerificationDialog = false },
                onSubmit = { msg, uris ->
                    viewModel.submitVerificationRequest(msg, uris)
                    showVerificationDialog = false
                    android.widget.Toast.makeText(navController.context, navController.context.getString(R.string.reporte_enviado), android.widget.Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}

@Composable
fun SettingOption(
    painter: Painter,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = Color(0xFF238CDD)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun SettingSwitch(
    painter: Painter,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = Color(0xFF238CDD)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF238CDD),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                )
            )
        }
    }
}
