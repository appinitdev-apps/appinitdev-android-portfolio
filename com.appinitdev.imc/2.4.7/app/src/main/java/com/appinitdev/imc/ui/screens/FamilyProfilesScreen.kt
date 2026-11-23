
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyProfilesScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val manager = remember { FamilyProfileManager(context) }
    val dbHelper = remember { BmiDatabaseHelper(context) }

    var members by remember { mutableStateOf(manager.getMembers()) }
    var selectedMember by remember { mutableStateOf<FamilyMember?>(null) }

    var showAddMemberDialog by remember { mutableStateOf(false) }
    var memberToEdit by remember { mutableStateOf<FamilyMember?>(null) }
    var memberToDelete by remember { mutableStateOf<FamilyMember?>(null) }

    var showAddMeasurementDialog by remember { mutableStateOf(false) }
    var recordToDelete by remember { mutableStateOf<GrowthRecord?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedMember != null) selectedMember!!.name else "Perfiles Familiares",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectedMember != null) {
                                selectedMember = null
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.volver),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    if (selectedMember != null) {
                        IconButton(onClick = { memberToEdit = selectedMember }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar Familiar",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        IconButton(onClick = { memberToDelete = selectedMember }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar Familiar",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        IconButton(onClick = { showAddMemberDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Agregar Familiar",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            if (selectedMember == null) {
                FloatingActionButton(
                    onClick = { showAddMemberDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar Familiar")
                }
            }
        },
        bottomBar = { AdBanner() }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (selectedMember == null) {
                
                if (members.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FamilyRestroom,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(id = R.string.aun_no_has_registrado_familiares),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(id = R.string.agrega_a_tus_familiares_desc),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { showAddMemberDialog = true },
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = stringResource(id = R.string.agregar_familiar))
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(members, key = { it.id }) { member ->
                            val records = manager.getGrowthRecordsForMember(member.id)
                            val latestRecord = records.lastOrNull()

                            FamilyMemberCard(
                                member = member,
                                latestRecord = latestRecord,
                                totalRecords = records.size,
                                onClick = { selectedMember = member },
                                onEdit = { memberToEdit = member },
                                onDelete = { memberToDelete = member }
                            )
                        }
                    }
                }
            } else {
                
                val activeMember = selectedMember!!
                var memberRecords by remember(activeMember.id) {
                    mutableStateOf(manager.getGrowthRecordsForMember(activeMember.id))
                }

                var selectedMetricTab by remember { mutableIntStateOf(0) }
                val metricKeys = listOf("WEIGHT", "HEIGHT", "BMI", "HEAD_CIRCUMFERENCE", "BODY_FAT", "ICE")
                val metricTitles = listOf("⚖️ Peso", "📏 Estatura", "📊 IMC", "🧠 Perímetro", "💪 Grasa %", "🫀 ICE")

                val activeMetricKey = metricKeys[selectedMetricTab]
                val currentMetricMeasurements = remember(activeMember.id, activeMetricKey, memberRecords) {
                    dbHelper.getMeasurementsForMemberAndMetric(activeMember.id, activeMetricKey)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (activeMember.gender == Gender.FEMALE) Color(0xFFE91E63).copy(alpha = 0.15f) else Color(0xFF2196F3).copy(alpha = 0.15f),
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (activeMember.gender == Gender.FEMALE) Icons.Default.Female else Icons.Default.Male,
                                        contentDescription = null,
                                        tint = if (activeMember.gender == Gender.FEMALE) Color(0xFFE91E63) else Color(0xFF2196F3),
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeMember.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${activeMember.relationship} • ${getAgeFormatted(activeMember.birthTimestamp)}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(onClick = { memberToEdit = activeMember }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val latestRecord = memberRecords.lastOrNull()
                                val latestBmiResult = if (latestRecord != null) {
                                    val catId = getCategoryIdFromBirth(activeMember.birthTimestamp)
                                    BmiCalculatorEngine.calculate(
                                        context = context,
                                        categoryId = catId,
                                        subOptionIndex = 0,
                                        gender = activeMember.gender,
                                        weightKg = latestRecord.weightKg,
                                        heightCm = latestRecord.heightCm
                                    )
                                } else null

                                DirectPrintManager.printClinicalReport(
                                    context = context,
                                    memberName = activeMember.name,
                                    memberAge = getAgeFormatted(activeMember.birthTimestamp),
                                    memberGender = if (activeMember.gender == Gender.FEMALE) "Femenino" else "Masculino",
                                    latestBmiResult = latestBmiResult,
                                    growthRecords = memberRecords
                                )
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = stringResource(id = R.string.imprimir), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val latestRecord = memberRecords.lastOrNull()
                                val latestBmiResult = if (latestRecord != null) {
                                    val catId = getCategoryIdFromBirth(activeMember.birthTimestamp)
                                    BmiCalculatorEngine.calculate(
                                        context = context,
                                        categoryId = catId,
                                        subOptionIndex = 0,
                                        gender = activeMember.gender,
                                        weightKg = latestRecord.weightKg,
                                        heightCm = latestRecord.heightCm
                                    )
                                } else null

                                PdfReportGenerator.generateAndSharePdf(
                                    context = context,
                                    memberName = activeMember.name,
                                    memberAge = getAgeFormatted(activeMember.birthTimestamp),
                                    memberGender = if (activeMember.gender == Gender.FEMALE) "Femenino" else "Masculino",
                                    latestBmiResult = latestBmiResult,
                                    growthRecords = memberRecords
                                )
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B894))
                        ) {
                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = stringResource(id = R.string.pdf_label), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                CsvExportManager.exportAndShareCsv(
                                    context = context,
                                    memberName = activeMember.name,
                                    records = memberRecords
                                )
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = stringResource(id = R.string.excel_label), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    
                    Button(
                        onClick = { showAddMeasurementDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.registrar_nuevo_peso_talla),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    
                    WhoPercentileChart(records = memberRecords)

                    
                    Text(
                        text = stringResource(id = R.string.filtro_graficas_evolucion),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    ScrollableTabRow(
                        selectedTabIndex = selectedMetricTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.primary,
                        edgePadding = 8.dp
                    ) {
                        metricTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedMetricTab == index,
                                onClick = { selectedMetricTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedMetricTab == index) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            )
                        }
                    }

                    
                    GenericMetricChart(
                        title = "Evolución de ${metricTitles[selectedMetricTab]}",
                        measurements = currentMetricMeasurements
                    )

                    
                    if (memberRecords.isNotEmpty()) {
                        Text(
                            text = stringResource(id = R.string.historial_de_mediciones),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        memberRecords.reversed().forEach { rec ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = DateFormat.format("dd MMM yyyy", Date(rec.timestamp)).toString(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "${rec.weightKg} kg  •  ${rec.heightCm.toInt()} cm",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = rec.statusText,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(
                                        onClick = { recordToDelete = rec }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar Medicion",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    
    if (showAddMemberDialog) {
        var nameInput by remember { mutableStateOf("") }
        var relationshipInput by remember { mutableStateOf("Hijo/a") }
        var genderInput by remember { mutableStateOf(Gender.MALE) }
        var birthYearInput by remember { mutableStateOf("2020") }

        val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
        val parsedYear = birthYearInput.trim().toIntOrNull()
        val isYearValid = parsedYear != null && parsedYear in 1900..currentYear

        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            title = {
                Text(text = stringResource(id = R.string.agregar_familiar), fontWeight = FontWeight.Bold, fontSize = 19.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text(stringResource(id = R.string.label_nombre_ejemplo)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = relationshipInput,
                        onValueChange = { relationshipInput = it },
                        label = { Text(stringResource(id = R.string.label_relacion_rol)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (genderInput == Gender.FEMALE) Color(0xFFE91E63) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { genderInput = Gender.FEMALE }
                                .padding(vertical = 10.dp),
                            contentColor = if (genderInput == Gender.FEMALE) Color.White else MaterialTheme.colorScheme.onSurface
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "${stringResource(id = R.string.femenino)} 🌸", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (genderInput == Gender.MALE) Color(0xFF2196F3) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { genderInput = Gender.MALE }
                                .padding(vertical = 10.dp),
                            contentColor = if (genderInput == Gender.MALE) Color.White else MaterialTheme.colorScheme.onSurface
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "${stringResource(id = R.string.masculino)} 🔷", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = birthYearInput,
                        onValueChange = { birthYearInput = it },
                        label = { Text(stringResource(id = R.string.ano_nacimiento_label_rango, currentYear)) },
                        isError = !isYearValid && birthYearInput.isNotBlank(),
                        supportingText = {
                            if (!isYearValid && birthYearInput.isNotBlank()) {
                                Text(
                                    text = stringResource(id = R.string.ano_nacimiento_invalido, currentYear),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = nameInput.isNotBlank() && isYearValid,
                    onClick = {
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.YEAR, parsedYear!!)
                        val newMember = FamilyMember(
                            name = nameInput.trim(),
                            relationship = relationshipInput.ifBlank { "Familiar" },
                            gender = genderInput,
                            birthTimestamp = cal.timeInMillis
                        )
                        manager.saveMember(newMember)
                        members = manager.getMembers()
                        showAddMemberDialog = false
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.guardar), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showAddMemberDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.cancelar), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }

    
    if (memberToEdit != null) {
        val target = memberToEdit!!
        val birthCal = Calendar.getInstance().apply { timeInMillis = target.birthTimestamp }

        var nameInput by remember(target.id) { mutableStateOf(target.name) }
        var relationshipInput by remember(target.id) { mutableStateOf(target.relationship) }
        var genderInput by remember(target.id) { mutableStateOf(target.gender) }
        var birthYearInput by remember(target.id) { mutableStateOf(birthCal.get(Calendar.YEAR).toString()) }

        val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
        val parsedYear = birthYearInput.trim().toIntOrNull()
        val isYearValid = parsedYear != null && parsedYear in 1900..currentYear

        AlertDialog(
            onDismissRequest = { memberToEdit = null },
            title = {
                Text(text = stringResource(id = R.string.editar_familiar), fontWeight = FontWeight.Bold, fontSize = 19.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text(stringResource(id = R.string.label_nombre)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = relationshipInput,
                        onValueChange = { relationshipInput = it },
                        label = { Text(stringResource(id = R.string.label_relacion_rol_simple)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (genderInput == Gender.FEMALE) Color(0xFFE91E63) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { genderInput = Gender.FEMALE }
                                .padding(vertical = 10.dp),
                            contentColor = if (genderInput == Gender.FEMALE) Color.White else MaterialTheme.colorScheme.onSurface
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "${stringResource(id = R.string.femenino)} 🌸", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (genderInput == Gender.MALE) Color(0xFF2196F3) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { genderInput = Gender.MALE }
                                .padding(vertical = 10.dp),
                            contentColor = if (genderInput == Gender.MALE) Color.White else MaterialTheme.colorScheme.onSurface
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "${stringResource(id = R.string.masculino)} 🔷", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = birthYearInput,
                        onValueChange = { birthYearInput = it },
                        label = { Text(stringResource(id = R.string.ano_nacimiento_label_rango, currentYear)) },
                        isError = !isYearValid && birthYearInput.isNotBlank(),
                        supportingText = {
                            if (!isYearValid && birthYearInput.isNotBlank()) {
                                Text(
                                    text = stringResource(id = R.string.ano_nacimiento_invalido, currentYear),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = nameInput.isNotBlank() && isYearValid,
                    onClick = {
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.YEAR, parsedYear!!)
                        val updated = target.copy(
                            name = nameInput.trim(),
                            relationship = relationshipInput.ifBlank { "Familiar" },
                            gender = genderInput,
                            birthTimestamp = cal.timeInMillis
                        )
                        manager.saveMember(updated)
                        members = manager.getMembers()
                        if (selectedMember?.id == target.id) {
                            selectedMember = updated
                        }
                        memberToEdit = null
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.guardar_cambios), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { memberToEdit = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.cancelar), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }

    
    if (memberToDelete != null) {
        val target = memberToDelete!!
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            },
            title = {
                Text(text = stringResource(id = R.string.eliminar_familiar), fontWeight = FontWeight.Bold, fontSize = 19.sp)
            },
            text = {
                Text(
                    text = stringResource(id = R.string.confirmar_eliminar_perfil, target.name),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        manager.deleteMember(target.id)
                        members = manager.getMembers()
                        if (selectedMember?.id == target.id) {
                            selectedMember = null
                        }
                        memberToDelete = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(text = stringResource(id = R.string.eliminar), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { memberToDelete = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.cancelar), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }

    
    if (recordToDelete != null && selectedMember != null) {
        val activeMember = selectedMember!!
        val rec = recordToDelete!!
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = {
                Text(text = stringResource(id = R.string.eliminar_medicion), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Text(
                    text = "¿Eliminar el registro de ${rec.weightKg} kg / ${rec.heightCm.toInt()} cm del ${DateFormat.format("dd MMM yyyy", Date(rec.timestamp))}?",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        manager.deleteGrowthRecord(rec.id)
                        recordToDelete = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(text = stringResource(id = R.string.eliminar), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { recordToDelete = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.cancelar), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }

    
    if (showAddMeasurementDialog && selectedMember != null) {
        val activeMember = selectedMember!!
        var weightInput by remember { mutableStateOf("12.5") }
        var heightInput by remember { mutableStateOf("85.0") }

        AlertDialog(
            onDismissRequest = { showAddMeasurementDialog = false },
            title = {
                Text(text = stringResource(id = R.string.registrar_medicion), fontWeight = FontWeight.Bold, fontSize = 19.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text(stringResource(id = R.string.label_peso_kg_ejemplo)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { heightInput = it },
                        label = { Text(stringResource(id = R.string.label_estatura_cm_ejemplo)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = weightInput.toDoubleOrNull()
                        val h = heightInput.toDoubleOrNull()
                        if (w != null && h != null && h > 0) {
                            val categoryId = getCategoryIdFromBirth(activeMember.birthTimestamp)
                            val result = BmiCalculatorEngine.calculate(
                                context = context,
                                categoryId = categoryId,
                                subOptionIndex = 0,
                                gender = activeMember.gender,
                                weightKg = w,
                                heightCm = h
                            )

                            val record = GrowthRecord(
                                memberId = activeMember.id,
                                weightKg = w,
                                heightCm = h,
                                bmiValue = result.bmiValue,
                                statusText = result.statusText
                            )
                            manager.saveGrowthRecord(record)
                            showAddMeasurementDialog = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.guardar_medicion), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showAddMeasurementDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.cancelar), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }
}

@Composable
fun FamilyMemberCard(
    member: FamilyMember,
    latestRecord: GrowthRecord?,
    totalRecords: Int,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (member.gender == Gender.FEMALE) Color(0xFFE91E63).copy(alpha = 0.15f) else Color(0xFF2196F3).copy(alpha = 0.15f),
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (member.gender == Gender.FEMALE) Icons.Default.Female else Icons.Default.Male,
                        contentDescription = null,
                        tint = if (member.gender == Gender.FEMALE) Color(0xFFE91E63) else Color(0xFF2196F3),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${member.relationship} • ${getAgeFormatted(member.birthTimestamp)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (latestRecord != null) {
                    Text(
                        text = stringResource(id = R.string.ultimo_registro_formato, latestRecord.weightKg, latestRecord.heightCm.toInt(), latestRecord.statusText),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.sin_mediciones_guardadas),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(id = R.string.editar_perfil),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.eliminar_perfil),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun getAgeFormatted(birthTimestamp: Long): String {
    val birth = Calendar.getInstance().apply { timeInMillis = birthTimestamp }
    val now = Calendar.getInstance()

    var years = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
    if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
        years--
    }

    return if (years <= 0) {
        val months = (now.get(Calendar.MONTH) - birth.get(Calendar.MONTH) + 12) % 12
        "$months meses"
    } else {
        "$years años"
    }
}

private fun getCategoryIdFromBirth(birthTimestamp: Long): Int {
    val birth = Calendar.getInstance().apply { timeInMillis = birthTimestamp }
    val now = Calendar.getInstance()
    var years = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
    if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
        years--
    }

    return when {
        years < 1 -> 1
        years in 1..4 -> 2
        years in 5..9 -> 3
        years in 10..19 -> 4
        years in 20..64 -> 5
        else -> 6
    }
}
