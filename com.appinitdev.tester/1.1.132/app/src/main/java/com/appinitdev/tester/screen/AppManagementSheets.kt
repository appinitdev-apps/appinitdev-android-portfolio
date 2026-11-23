
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppBottomSheet(
    viewModel: AuthViewModel,
    appToEdit: AppModel? = null,
    onDismiss: () -> Unit
) {
    val draft by viewModel.draftApp.collectAsState()
    
    
    var step by remember { mutableIntStateOf(if (appToEdit == null) draft.step else 1) }
    var nombre by remember { mutableStateOf(appToEdit?.nombre ?: draft.nombre) }
    var paquete by remember { mutableStateOf(appToEdit?.paquete ?: draft.paquete) }
    var categoria by remember { mutableStateOf(appToEdit?.categoria ?: draft.categoria) }
    var paisesAgregados by remember { mutableStateOf(appToEdit?.paisesAgregados ?: draft.paisesAgregados) }
    var metodoTest by remember { mutableStateOf(appToEdit?.metodoTest ?: draft.metodoTest) }
    var urlGoogleGroup by remember { mutableStateOf(appToEdit?.urlGoogleGroup ?: draft.urlGoogleGroup) }
    var instrucciones by remember { mutableStateOf(appToEdit?.instrucciones ?: draft.instrucciones) }
    var contactoDirecto by remember { mutableStateOf(appToEdit?.contactoDirecto ?: draft.contactoDirecto) }
    var comunidadUrl by remember { mutableStateOf(appToEdit?.comunidadUrl ?: draft.comunidadUrl) }
    var notas by remember { mutableStateOf(appToEdit?.notas ?: draft.notas) }
    var descripcion by remember { mutableStateOf(appToEdit?.descripcion ?: draft.descripcion) }
    var cuentaPrueba by remember { mutableStateOf(appToEdit?.cuentaPrueba ?: draft.cuentaPrueba) }
    var esDePago by remember { mutableStateOf(appToEdit?.esDePago ?: draft.esDePago) }
    var iconoUri by remember { mutableStateOf<android.net.Uri?>(null) }

    var showReputationAviso by remember { mutableStateOf(appToEdit == null) }

    
    LaunchedEffect(step, nombre, paquete, categoria, paisesAgregados, metodoTest, urlGoogleGroup, instrucciones, contactoDirecto, comunidadUrl, notas, cuentaPrueba, esDePago) {
        if (appToEdit == null) {
            viewModel.updateDraft {
                copy(
                    step = step, nombre = nombre, paquete = paquete, categoria = categoria,
                    paisesAgregados = paisesAgregados, metodoTest = metodoTest, urlGoogleGroup = urlGoogleGroup,
                    instrucciones = instrucciones, contactoDirecto = contactoDirecto, comunidadUrl = comunidadUrl,
                    notas = notas, descripcion = descripcion, cuentaPrueba = cuentaPrueba, esDePago = esDePago
                )
            }
        }
    }

    val isPaqueteValido = remember(paquete) { "^[a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)+$".toRegex().matches(paquete) && paquete.length <= 150 }
    
    LaunchedEffect(paquete) {
        if (paquete.isNotEmpty() && paquete.length > 5 && isPaqueteValido && paquete != appToEdit?.paquete) {
            delay(500)
            viewModel.verifyPackageRealtime(paquete)
        }
    }

    if (showReputationAviso) {
        ModalBottomSheet(
            onDismissRequest = { showReputationAviso = false },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Stars,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                
                Text(
                    stringResource(R.string.aviso_reputacion_titulo),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    stringResource(R.string.aviso_reputacion_msg),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.como_subir_rep_titulo),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.como_subir_rep_items),
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp
                        )
                    }
                }
                
                Button(
                    onClick = { showReputationAviso = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.entendido_y_listo), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (appToEdit == null) stringResource(R.string.registrar_nueva_app) else stringResource(R.string.editar_aplicaci_n),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                
                if (appToEdit == null && (nombre.isNotBlank() || paquete.isNotBlank())) {
                    IconButton(onClick = { viewModel.clearDraft(); onDismiss() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Borrar Progreso", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            LinearProgressIndicator(
                progress = { step / 7f },
                modifier = Modifier.fillMaxWidth().clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
            
            Text(
                text = stringResource(R.string.paso_n, step),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )

            
            Box(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                when (step) {
                    1 -> StepNombre(
                        nombre = nombre,
                        descripcion = descripcion,
                        iconoUri = iconoUri,
                        iconoUrlActual = appToEdit?.icono,
                        onNombreChange = { nombre = it },
                        onDescripcionChange = { descripcion = it },
                        onIconoSelected = { iconoUri = it }
                    )
                    2 -> StepPaquete(paquete, viewModel) { paquete = it }
                    3 -> StepCategoria(categoria) { categoria = it }
                    4 -> StepPaises(paisesAgregados) { paisesAgregados = it }
                    5 -> StepMetodoTest(metodoTest, urlGoogleGroup, { metodoTest = it }, { urlGoogleGroup = it })
                    6 -> StepOpcional(instrucciones, contactoDirecto, comunidadUrl, notas, cuentaPrueba, 
                        { instrucciones = it }, { contactoDirecto = it }, { comunidadUrl = it }, { notas = it }, { cuentaPrueba = it })
                    7 -> StepPago(esDePago) { esDePago = it }
                }
            }

            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step-- },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.volver))
                    }
                }
                
                Button(
                    onClick = {
                        if (step < 7) {
                            step++
                        } else {
                            if (appToEdit == null) {
                                viewModel.registerNewApp(
                                    nombre, paquete, 
                                    "https://play.google.com/apps/testing/$paquete",
                                    "https://play.google.com/store/apps/details?id=$paquete",
                                    instrucciones, notas, urlGoogleGroup.trim(), categoria,
                                    contactoDirecto, comunidadUrl, cuentaPrueba, esDePago, paisesAgregados, metodoTest,
                                    descripcion,
                                    iconoUri = iconoUri
                                )
                                viewModel.clearDraft()
                            } else {
                                viewModel.updateAppFullInfo(
                                    appToEdit.id, nombre, paquete,
                                    "https://play.google.com/apps/testing/$paquete",
                                    "https://play.google.com/store/apps/details?id=$paquete",
                                    instrucciones, notas, urlGoogleGroup.trim(), categoria,
                                    contactoDirecto, comunidadUrl, cuentaPrueba, esDePago, paisesAgregados, metodoTest,
                                    descripcion,
                                    iconoUri = iconoUri,
                                    iconoActual = appToEdit.icono
                                )
                            }
                            onDismiss()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    ),
                    enabled = when(step) {
                        1 -> nombre.length >= 3
                        2 -> isPaqueteValido && !viewModel.packageExists
                        5 -> if (metodoTest == "google_group") urlGoogleGroup.startsWith("https://groups.google.com/") else true
                        else -> true
                    }
                ) {
                    Text(
                        if (step < 7) stringResource(R.string.siguiente) else stringResource(R.string.publicar_app),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StepNombre(
    nombre: String,
    descripcion: String,
    iconoUri: android.net.Uri?,
    iconoUrlActual: String?,
    onNombreChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onIconoSelected: (android.net.Uri?) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        onIconoSelected(uri)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.icono_app_opcional),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { launcher.launch("image/*") },
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (iconoUri != null) {
                        AsyncImage(
                            model = iconoUri,
                            contentDescription = "Ícono seleccionado",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (!iconoUrlActual.isNullOrBlank()) {
                        SubcomposeAsyncImage(
                            model = iconoUrlActual,
                            contentDescription = "Ícono actual",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.subir_foto),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (iconoUri != null || !iconoUrlActual.isNullOrBlank()) {
                TextButton(onClick = { onIconoSelected(null) }) {
                    Text(stringResource(R.string.quitar_imagen), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.nombre_de_la_app), fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = nombre,
                onValueChange = onNombreChange,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                placeholder = { Text(stringResource(R.string.mi_increible_app_placeholder)) },
                isError = nombre.isNotEmpty() && nombre.length < 3,
                supportingText = {
                    if (nombre.isNotEmpty() && nombre.length < 3) {
                        Text(stringResource(R.string.nombre_app_error), color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.descripcion_app), fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = descripcion,
                onValueChange = onDescripcionChange,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                placeholder = { Text(stringResource(R.string.descripcion_opcional)) },
                minLines = 3
            )
        }
    }
}

@Composable
fun StepPaquete(paquete: String, viewModel: AuthViewModel, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.paquete_com_ejemplo_app), fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = paquete,
            onValueChange = { onValueChange(it.trim().lowercase()) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            placeholder = { Text(stringResource(R.string.paquete_ejemplo)) },
            supportingText = {
                Column {
                    if (viewModel.packageExists) Text(stringResource(R.string.este_id_ya_esta_registrado), color = MaterialTheme.colorScheme.error)
                    Text(stringResource(R.string.paquete_advertencia), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
            },
            trailingIcon = {
                if (viewModel.isCheckingPackage) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else if (viewModel.packageExists) Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StepCategoria(selected: String, onSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.categoria_lbl), fontWeight = FontWeight.Bold)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppConstants.CATEGORIAS.forEach { cat ->
                FilterChip(
                    selected = selected == cat,
                    onClick = { onSelected(cat) },
                    label = { Text(cat, fontSize = 12.sp) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
fun StepPaises(agregados: Boolean, onAgregados: (Boolean) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            stringResource(R.string.pregunta_paises),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { onAgregados(true) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (agregados) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = if (agregados) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(16.dp),
                border = if (!agregados) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) else null,
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.si_ya_agregue), fontWeight = FontWeight.Bold) }
            
            Button(
                onClick = { onAgregados(false) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!agregados) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = if (!agregados) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(16.dp),
                border = if (agregados) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) else null,
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.no_aun_no), fontWeight = FontWeight.Bold) }
        }

        if (!agregados) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    stringResource(R.string.advertencia_paises),
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun StepMetodoTest(metodo: String, url: String, onMetodo: (String) -> Unit, onUrl: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.metodo_test_pregunta), fontWeight = FontWeight.Bold)
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = metodo == "google_group",
                onClick = { onMetodo("google_group") },
                label = { Text(stringResource(R.string.metodo_google_groups)) },
                leadingIcon = { if (metodo == "google_group") Icon(Icons.Default.Check, null) }
            )
            FilterChip(
                selected = metodo == "email",
                onClick = { onMetodo("email") },
                label = { Text(stringResource(R.string.metodo_emails)) },
                leadingIcon = { if (metodo == "email") Icon(Icons.Default.Check, null) }
            )
        }

        if (metodo == "google_group") {
            OutlinedTextField(
                value = url,
                onValueChange = onUrl,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.link_grupo_google)) },
                placeholder = { Text("https://groups.google.com/g/mi-grupo") },
                shape = RoundedCornerShape(12.dp),
                supportingText = { Text(stringResource(R.string.info_google_groups)) }
            )
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(stringResource(R.string.advertencia_emails), modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun StepOpcional(
    inst: String, cont: String, com: String, notas: String, cuenta: String,
    onInst: (String) -> Unit, onCont: (String) -> Unit, onCom: (String) -> Unit, onNotas: (String) -> Unit, onCuenta: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.info_adicional_opcional), fontWeight = FontWeight.Bold)
        OutlinedTextField(value = inst, onValueChange = onInst, label = { Text(stringResource(R.string.instrucciones)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), minLines = 2)
        OutlinedTextField(value = cont, onValueChange = onCont, label = { Text(stringResource(R.string.link_contacto_directo)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        OutlinedTextField(value = com, onValueChange = onCom, label = { Text(stringResource(R.string.link_comunidad_otros)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        OutlinedTextField(value = cuenta, onValueChange = onCuenta, label = { Text(stringResource(R.string.instrucciones_acceso)) }, placeholder = { Text(stringResource(R.string.instrucciones_acceso_hint)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        OutlinedTextField(value = notas, onValueChange = onNotas, label = { Text(stringResource(R.string.notas)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
    }
}

@Composable
fun StepPago(pago: Boolean, onPago: (Boolean) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            stringResource(R.string.gratis_o_pago),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { onPago(false) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!pago) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = if (!pago) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(16.dp),
                border = if (pago) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) else null,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    stringResource(R.string.gratuita),
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { onPago(true) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (pago) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = if (pago) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(16.dp),
                border = if (!pago) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) else null,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    stringResource(R.string.de_pago),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (pago) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    stringResource(R.string.advertencia_pago),
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallationStepsBottomSheet(
    app: AppModel,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = stringResource(R.string.guia_instalacion_titulo),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            InstallStepItem(
                number = "1",
                title = stringResource(R.string.paso_1_titulo),
                description = if (app.metodoTest == "google_group") 
                    stringResource(R.string.paso_1_desc_grupo) 
                else 
                    stringResource(R.string.paso_1_desc_email),
                icon = if (app.metodoTest == "google_group") Icons.Default.Groups else Icons.Default.Email
            )

            InstallStepItem(
                number = "2",
                title = stringResource(R.string.paso_2_titulo),
                description = stringResource(R.string.paso_2_desc),
                icon = Icons.Default.Language
            )

            InstallStepItem(
                number = "3",
                title = stringResource(R.string.paso_3_titulo),
                description = stringResource(R.string.paso_3_desc),
                icon = Icons.Default.Shop
            )
            
            if (app.esDePago) {
                InstallStepItem(
                    number = "4",
                    title = stringResource(R.string.compra_tarjeta_prueba),
                    description = stringResource(R.string.desc_compra_tarjeta_prueba),
                    icon = Icons.Default.CreditCard
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        Text(
                            text = stringResource(R.string.advertencia_pago),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InstallStepItem(number: String, title: String, description: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(number, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailBottomSheet(
    app: AppModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.info_app_titulo), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            
            InfoSection(stringResource(R.string.descripcion_app), app.descripcion)
            InfoSection(stringResource(R.string.instrucciones), app.instrucciones)
            InfoSection("Cuenta de Acceso", app.cuentaPrueba)
            InfoSection("Notas", app.notas)
            
            if (app.contactoDirecto.isNotBlank()) LinkItem("Contacto Directo", app.contactoDirecto)
            if (app.comunidadUrl.isNotBlank()) LinkItem("Comunidad", app.comunidadUrl)
            if (app.urlGoogleGroup.isNotBlank()) LinkItem("Google Group", app.urlGoogleGroup)
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun InfoSection(title: String, content: String) {
    if (content.isNotBlank()) {
        val context = LocalContext.current
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { copyToClipboard(context, content) },
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(content, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun LinkItem(title: String, url: String) {
    val context = LocalContext.current
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
            append(url)
        }
    }
    Column {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            ClickableText(
                text = annotatedString,
                onClick = {   },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { copyToClipboard(context, url) }) {
                Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("app_info", text)
    clipboard.setPrimaryClip(clip)
    showNiceToast(context, context.getString(R.string.link_copiado), NiceToastType.INFO)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAppBottomSheet(
    app: AppModel,
    viewModel: AuthViewModel,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.editar_aplicaci_n)) },
                leadingContent = { Icon(Icons.Default.Edit, null) },
                modifier = Modifier.clickable { onEdit(); onDismiss() }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.resetear_app)) },
                leadingContent = { Icon(Icons.Default.Refresh, null) },
                modifier = Modifier.clickable { viewModel.resetApp(app.id); onDismiss() }
            )
            ListItem(
                headlineContent = { Text(if (app.status == "oculta") stringResource(R.string.mostrar_app) else stringResource(R.string.ocultar_app)) },
                leadingContent = { Icon(if (app.status == "oculta") Icons.Default.Visibility else Icons.Default.VisibilityOff, null) },
                modifier = Modifier.clickable { viewModel.toggleAppStatus(app); onDismiss() }
            )
            if (app.status != "produccion") {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.marcar_produccion)) },
                    leadingContent = { Icon(Icons.Default.RocketLaunch, null) },
                    modifier = Modifier.clickable { viewModel.updateAppStatus(app.id, "produccion"); onDismiss() }
                )
            }
            HorizontalDivider()
            ListItem(
                headlineContent = { Text(stringResource(R.string.eliminar_app), color = MaterialTheme.colorScheme.error) },
                leadingContent = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                modifier = Modifier.clickable { showDeleteConfirm = true }
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.eliminar_app)) },
            text = { Text(stringResource(R.string.confirmar_eliminar_app)) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteApp(app.id); onDismiss() }) {
                    Text(stringResource(R.string.eliminar_app), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }
}
