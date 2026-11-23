data class HealthToolCategory(
    val idKey: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val forFemaleOnly: Boolean = false
)
private fun calculateAgeYearsAndMonths(birthTimestamp: Long): Pair<Int, Int> {
    val birth = java.util.Calendar.getInstance().apply { timeInMillis = birthTimestamp }
    val now = java.util.Calendar.getInstance()
    var years = now.get(java.util.Calendar.YEAR) - birth.get(java.util.Calendar.YEAR)
    var months = now.get(java.util.Calendar.MONTH) - birth.get(java.util.Calendar.MONTH)
    if (now.get(java.util.Calendar.DAY_OF_MONTH) < birth.get(java.util.Calendar.DAY_OF_MONTH)) {
        months--
    }
    if (months < 0) {
        years--
        months += 12
    }
    val totalMonths = (years * 12) + months
    return Pair(years.coerceAtLeast(0), totalMonths.coerceAtLeast(0))
}
private fun getInitialValueForMember(
    context: android.content.Context,
    activeMember: FamilyMember?,
    metricType: String,
    fallbackValue: String,
    growthFieldSelector: ((GrowthRecord) -> String)? = null
): String {
    val dbHelper = BmiDatabaseHelper(context)
    if (activeMember != null) {
        val lastMeasurement = dbHelper.getLatestMeasurementForMemberAndMetric(activeMember.id, metricType)
        if (lastMeasurement != null) {
            val num = lastMeasurement.valueNumeric
            return if (num == num.toLong().toDouble()) num.toLong().toString() else String.format(java.util.Locale.US, "%.1f", num)
        }
        if (growthFieldSelector != null) {
            val lastGrowth = dbHelper.getGrowthRecordsForMember(activeMember.id).lastOrNull()
            if (lastGrowth != null) {
                return growthFieldSelector(lastGrowth)
            }
        }
    } else {
        val lastHistory = BmiHistoryManager(context).getRecords().lastOrNull()
        if (lastHistory != null && growthFieldSelector != null) {
            val growthDummy = GrowthRecord(
                memberId = 0L,
                weightKg = lastHistory.weightKg,
                heightCm = lastHistory.heightCm,
                bmiValue = lastHistory.bmiValue,
                statusText = lastHistory.statusText
            )
            return growthFieldSelector(growthDummy)
        }
    }
    return fallbackValue
}
private fun isToolSuitableForProfile(
    toolKey: String,
    gender: Gender,
    ageYears: Int,
    totalMonths: Int
): Boolean {
    return when (toolKey) {
        "PREGNANCY" -> gender == Gender.FEMALE && ageYears >= 12
        "HEAD" -> totalMonths <= 24
        "ROHRER" -> ageYears <= 5
        "INFANT_REQ" -> totalMonths <= 12
        "TANNER" -> ageYears in 2..19
        "NOTIF" -> ageYears <= 18
        "IBW", "WHR", "BAI", "LBM", "TBW", "PAM", "VO2MAX", "BMR", "MACROS", "HR_ZONES" -> ageYears >= 10
        "ICE", "ACTIVITY", "WATER", "BACKUP" -> true
        else -> true
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthToolsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val familyManager = remember { FamilyProfileManager(context) }
    var allMembers by remember { mutableStateOf(familyManager.getMembers()) }
    var activeMember by remember(allMembers) { mutableStateOf(familyManager.getActiveMember()) }
    var showProfileSwitchDialog by remember { mutableStateOf(false) }
    var selectedToolKey by remember { mutableStateOf<String?>(null) } 
    val allTools = listOf(
        HealthToolCategory("WATER", "💧 Agua e Hidratación", "Meta Diaria OMS", Icons.Default.LocalDrink, Color(0xFF0288D1)),
        HealthToolCategory("BMR", "🔥 Metabolismo y TMB", "Mifflin-St Jeor Pro", Icons.Default.LocalFireDepartment, Color(0xFFFF5722)),
        HealthToolCategory("ACTIVITY", "🏃 Gasto Calórico", "Cálculo por METs", Icons.Default.DirectionsRun, Color(0xFF10B981)),
        HealthToolCategory("ICE", "🫀 Riesgo Cardiovascular", "Índice Cintura-Estatura", Icons.Default.MonitorWeight, Color(0xFF8B5CF6)),
        HealthToolCategory("MACROS", "🍎 Macronutrientes", "Proteínas, Carbs y Grasas", Icons.Default.Restaurant, Color(0xFFFF9800)),
        HealthToolCategory("IBW", "⚖️ Peso Corporal Ideal", "Devine, Hamwi, Broca", Icons.Default.MonitorWeight, Color(0xFF3F51B5)),
        HealthToolCategory("WHR", "⏳ Cintura-Cadera (ÍCC)", "Distribución y Riesgo OMS", Icons.Default.MonitorWeight, Color(0xFFE91E63)),
        HealthToolCategory("HR_ZONES", "❤️ Frecuencia Cardíaca", "FCM y Zonas Tanaka", Icons.Default.DirectionsRun, Color(0xFFEF4444)),
        HealthToolCategory("BAI", "📐 Adiposidad (IAC/BAI)", "% Grasa por Cadera", Icons.Default.Height, Color(0xFF8B5CF6)),
        HealthToolCategory("LBM", "💪 Masa Magra (LBM)", "Boer & James en Kilos", Icons.Default.MonitorWeight, Color(0xFF10B981)),
        HealthToolCategory("TBW", "💧 Agua Corporal (TBW)", "Watson & Hume en Litros", Icons.Default.LocalDrink, Color(0xFF0288D1)),
        HealthToolCategory("ROHRER", "👶 Ín. Ponderal Rohrer", "Proporción Neonatal", Icons.Default.ChildCare, Color(0xFFE91E63)),
        HealthToolCategory("INFANT_REQ", "🍼 Calorías & Leche Bebé", "0 a 12 Meses OMS/AAP", Icons.Default.ChildCare, Color(0xFFFF9800)),
        HealthToolCategory("PAM", "🩸 Presión Arterial (PAM)", "Presión Media & Pulso", Icons.Default.MonitorWeight, Color(0xFFEF4444)),
        HealthToolCategory("VO2MAX", "🫁 VO2 Máx Estimado", "Capacidad Aeróbica", Icons.Default.DirectionsRun, Color(0xFF10B981)),
        HealthToolCategory("HEAD", "👶 Perímetro Cefálico", "0 a 24 Meses OMS", Icons.Default.ChildCare, Color(0xFFE91E63)),
        HealthToolCategory("TANNER", "📐 Talla Adulta Tanner", "Predicción Genética", Icons.Default.Height, Color(0xFF00A88F)),
        HealthToolCategory("PREGNANCY", "🤰 Ganancia Gestacional", "Peso en Embarazo IOM", Icons.Default.Female, Color(0xFFEC407A), forFemaleOnly = true),
        HealthToolCategory("NOTIF", "🔔 Alertas Pediátricas", "Recordatorios de Peso", Icons.Default.NotificationsActive, Color(0xFF3F51B5)),
        HealthToolCategory("BACKUP", "💾 Copia de Seguridad", "Respaldo JSON", Icons.Default.Backup, Color(0xFF607D8B))
    )
    val birthTimestamp = activeMember?.birthTimestamp ?: System.currentTimeMillis()
    val (ageYears, totalMonths) = remember(activeMember?.birthTimestamp) {
        calculateAgeYearsAndMonths(birthTimestamp)
    }
    val activeGender = activeMember?.gender ?: Gender.MALE
    var showAllToolsManually by remember { mutableStateOf(false) }
    val suitableTools = remember(activeGender, ageYears, totalMonths) {
        allTools.filter { tool ->
            isToolSuitableForProfile(tool.idKey, activeGender, ageYears, totalMonths)
        }
    }
    val availableTools = if (showAllToolsManually) allTools else suitableTools
    val selectedTool = allTools.find { it.idKey == selectedToolKey }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = selectedTool?.title ?: "Herramientas de Salud",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedToolKey != null) {
                            selectedToolKey = null
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.volver),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = { AdBanner() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (activeMember?.gender == Gender.FEMALE) Color(0xFFE91E63) else Color(0xFF2196F3),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (activeMember?.gender == Gender.FEMALE) Icons.Default.Female else Icons.Default.Male,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = stringResource(id = R.string.perfil_vinculado), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${activeMember?.name ?: stringResource(id = R.string.mi_perfil)} (${if (activeMember?.gender == Gender.FEMALE) "Femenino 🌸" else "Masculino 🔷"})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.clickable { showProfileSwitchDialog = true }
                    ) {
                        Text(
                            text = stringResource(id = R.string.cambiar_perfil),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
            if (selectedToolKey == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (showAllToolsManually) "Todas las Calculadoras (${allTools.size})" else "Recomendadas para ${activeMember?.name ?: "ti"}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (showAllToolsManually) "Mostrando catálogo completo" else "${if (activeGender == Gender.FEMALE) "Femenino 🌸" else "Masculino 🔷"} • $ageYears años",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (showAllToolsManually) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.clickable { showAllToolsManually = !showAllToolsManually }
                    ) {
                        Text(
                            text = if (showAllToolsManually) "🎯 Filtrar (${suitableTools.size})" else "🌐 Ver Todas (${allTools.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (showAllToolsManually) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(availableTools) { _, tool ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(135.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedToolKey = tool.idKey },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(shape = CircleShape, color = tool.color.copy(alpha = 0.15f), modifier = Modifier.size(42.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(imageVector = tool.icon, contentDescription = null, tint = tool.color, modifier = Modifier.size(24.dp))
                                    }
                                }
                                Column {
                                    Text(text = tool.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                                    Text(text = tool.subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    when (selectedToolKey) {
                        "WATER" -> ProfileWaterTrackerTool(activeMember)
                        "BMR" -> AdvancedBmrTool(activeMember)
                        "ACTIVITY" -> PhysicalActivityTool(activeMember)
                        "ICE" -> ProfileWaistToHeightTool(activeMember)
                        "MACROS" -> MacronutrientsTool(activeMember)
                        "IBW" -> IdealBodyWeightTool(activeMember)
                        "WHR" -> WaistToHipTool(activeMember)
                        "HR_ZONES" -> HeartRateZonesTool(activeMember)
                        "BAI" -> BodyAdiposityTool(activeMember)
                        "LBM" -> LeanBodyMassTool(activeMember)
                        "TBW" -> TotalBodyWaterTool(activeMember)
                        "ROHRER" -> RohrerIndexTool(activeMember)
                        "INFANT_REQ" -> InfantRequirementTool(activeMember)
                        "PAM" -> BloodPressureTool(activeMember)
                        "VO2MAX" -> Vo2MaxTool(activeMember)
                        "HEAD" -> ProfileHeadCircumferenceTool(activeMember)
                        "TANNER" -> TannerHeightTool(activeMember)
                        "PREGNANCY" -> PregnancyGainTool(activeMember)
                        "NOTIF" -> PediatricRemindersTool(activeMember)
                        "BACKUP" -> BackupRestoreTool()
                    }
                }
            }
        }
    }
    if (showProfileSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showProfileSwitchDialog = false },
            title = {
                Text(text = "Seleccionar Perfil Activo", fontWeight = FontWeight.Bold, fontSize = 19.sp)
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    allMembers.forEach { member ->
                        val isSelected = (activeMember?.id == member.id)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    familyManager.setActiveMemberId(member.id)
                                    activeMember = member
                                    selectedToolKey = null 
                                    showProfileSwitchDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (member.gender == Gender.FEMALE) Icons.Default.Female else Icons.Default.Male,
                                        contentDescription = null,
                                        tint = if (member.gender == Gender.FEMALE) Color(0xFFE91E63) else Color(0xFF2196F3)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = member.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(
                                            text = "${member.relationship} • ${if (member.gender == Gender.FEMALE) "Femenino" else "Masculino"}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(
                    onClick = { showProfileSwitchDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.entendido), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}
@Composable
fun ToolExplanationCard(
    whatItMeasures: String,
    whatIsItFor: String,
    interpretation: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.informacion_y_guia_salud),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = stringResource(id = R.string.que_mide_title), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = whatItMeasures, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = stringResource(id = R.string.para_que_sirve_title), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = whatIsItFor, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = stringResource(id = R.string.interpretacion_resultados_title), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = interpretation, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
@Composable
fun FormulaCard(
    formulaTitle: String,
    formulaExpression: String,
    variablesNotes: String,
    medicalStandard: String = "Guía OMS / AHA / Referencia"
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Functions,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.formula_de_referencia_title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = medicalStandard,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Text(
                text = formulaTitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formulaExpression,
                    fontSize = 12.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp)
                )
            }
            if (variablesNotes.isNotBlank()) {
                Text(
                    text = variablesNotes,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
@Composable
fun ProfileWaterTrackerTool(activeMember: FamilyMember?) {
    val context = LocalContext.current
    val dbHelper = remember { BmiDatabaseHelper(context) }
    val todayDateStr = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) }
    var waterData by remember { mutableStateOf(dbHelper.getWaterIntakeForDate(todayDateStr)) }
    val consumed = waterData.first
    val target = waterData.second
    val progress = (consumed.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color(0xFF0288D1).copy(alpha = 0.15f), modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.LocalDrink, contentDescription = null, tint = Color(0xFF0288D1), modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = "Consumo Diario de Agua", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Para ${activeMember?.name ?: "Mi Perfil"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0288D1).copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "REGISTRO DE HOY ($todayDateStr)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0288D1))
                Text(text = "$consumed ml / $target ml", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0288D1))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                    color = Color(0xFF0288D1),
                    strokeCap = StrokeCap.Round
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            val newTotal = dbHelper.addWaterIntake(todayDateStr, 250, target)
                            waterData = Pair(newTotal, target)
                            val memberId = activeMember?.id ?: 0L
                            dbHelper.insertProfileMeasurement(
                                ProfileMeasurement(
                                    memberId = memberId,
                                    metricType = "WATER",
                                    valueNumeric = newTotal.toDouble(),
                                    unitLabel = "ml",
                                    statusLabel = "$newTotal ml hidratación"
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text(text = "+250 ml", fontWeight = FontWeight.Bold) }
                    Button(
                        onClick = {
                            val newTotal = dbHelper.addWaterIntake(todayDateStr, 500, target)
                            waterData = Pair(newTotal, target)
                            val memberId = activeMember?.id ?: 0L
                            dbHelper.insertProfileMeasurement(
                                ProfileMeasurement(
                                    memberId = memberId,
                                    metricType = "WATER",
                                    valueNumeric = newTotal.toDouble(),
                                    unitLabel = "ml",
                                    statusLabel = "$newTotal ml hidratación"
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text(text = "+500 ml", fontWeight = FontWeight.Bold) }
                }
                OutlinedButton(
                    onClick = {
                        dbHelper.resetWaterIntake(todayDateStr)
                        waterData = Pair(0, target)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(text = "Reiniciar Consumo de Hoy") }
            }
        }
        ToolExplanationCard(
            whatItMeasures = "Mide el volumen diario de líquidos ingeridos en mililitros (mL).",
            whatIsItFor = "Prevenir la deshidratación, apoyar la digestión, optimizar la función renal y el equilibrio térmico.",
            interpretation = "La OMS sugiere un consumo promedio de 35 mL de agua por cada kilo de peso corporal al día."
        )
        FormulaCard(
            formulaTitle = "Cálculo de Meta Diaria de Hidratación OMS",
            formulaExpression = "Meta Diaria (mL) = Peso Corporal (kg) × 35 mL/kg",
            variablesNotes = "Ejemplo: Para 70 kg → 70 × 35 = 2,450 mL (~2.5 Litros al día).",
            medicalStandard = "Estándar OMS / EFSA"
        )
    }
}
@Composable
fun AdvancedBmrTool(activeMember: FamilyMember?) {
    val context = LocalContext.current
    val gender = activeMember?.gender ?: Gender.MALE
    val initialWeight = remember(activeMember?.id) {
        getInitialValueForMember(context, activeMember, "BMR", "75") { it.weightKg.toString() }
    }
    val initialHeight = remember(activeMember?.id) {
        getInitialValueForMember(context, activeMember, "BMR", "175") { it.heightCm.toInt().toString() }
    }
    val initialAge = remember(activeMember?.id) {
        if (activeMember != null) calculateAgeYearsAndMonths(activeMember.birthTimestamp).first.toString() else "30"
    }
    var weightInput by remember(activeMember?.id, initialWeight) { mutableStateOf(initialWeight) }
    var heightInput by remember(activeMember?.id, initialHeight) { mutableStateOf(initialHeight) }
    var ageInput by remember(activeMember?.id, initialAge) { mutableStateOf(initialAge) }
    val w = weightInput.toDoubleOrNull() ?: 75.0
    val h = heightInput.toDoubleOrNull() ?: 175.0
    val age = ageInput.toIntOrNull() ?: 30
    val mifflinBmr = if (gender == Gender.MALE) (10 * w) + (6.25 * h) - (5 * age) + 5 else (10 * w) + (6.25 * h) - (5 * age) - 161
    val harrisBmr = if (gender == Gender.MALE) 88.362 + (13.397 * w) + (4.799 * h) - (5.677 * age) else 447.593 + (9.247 * w) + (3.098 * h) - (4.330 * age)
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color(0xFFFF5722).copy(alpha = 0.15f), modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFF5722), modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = "Tasa Metabólica Basal Pro", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Para ${activeMember?.name ?: "Mi Perfil"} (${if (gender == Gender.FEMALE) "Femenino 🌸" else "Masculino 🔷"})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(value = weightInput, onValueChange = { weightInput = it }, label = { Text(stringResource(id = R.string.label_peso_kg)) }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = heightInput, onValueChange = { heightInput = it }, label = { Text(stringResource(id = R.string.label_estatura_cm)) }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = ageInput, onValueChange = { ageInput = it }, label = { Text(stringResource(id = R.string.label_edad_anos)) }, modifier = Modifier.weight(1f), singleLine = true)
        }
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = stringResource(id = R.string.mifflin_estandar_moderno), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "${mifflinBmr.toInt()} kcal / día", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
        }
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = stringResource(id = R.string.harris_revisada), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "${harrisBmr.toInt()} kcal / día", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF5722))
            }
        }
        ToolExplanationCard(
            whatItMeasures = "Mide la Tasa Metabólica Basal (TMB): la energía mínima consumida en reposo absoluto para mantener las funciones vitales (corazón, pulmones, cerebro).",
            whatIsItFor = "Establecer la base calórica diaria y diseñar planes de alimentación para perder grasa, mantener peso o ganar masa muscular.",
            interpretation = "Si consumes menos calorías que tu TMB + actividad física, perderás peso. Si consumes más, aumentarás de peso."
        )
        FormulaCard(
            formulaTitle = "Fórmulas de Mifflin-St Jeor y Harris-Benedict Revisada",
            formulaExpression = "Mifflin (H): 10W + 6.25H - 5A + 5\nMifflin (M): 10W + 6.25H - 5A - 161\n\nHarris (H): 88.362 + 13.397W + 4.799H - 5.677A\nHarris (M): 447.593 + 9.247W + 3.098H - 4.330A",
            variablesNotes = "Donde: W = Peso en kg, H = Estatura en cm, A = Edad en años.",
            medicalStandard = "Estándar Nutrición Clínica ESPEN"
        )
    }
}
@Composable
fun PhysicalActivityTool(activeMember: FamilyMember?) {
    val context = LocalContext.current
    val initialWeight = remember(activeMember?.id) {
        getInitialValueForMember(context, activeMember, "ACTIVITY", "70") { it.weightKg.toString() }
    }
    var weightInput by remember(activeMember?.id, initialWeight) { mutableStateOf(initialWeight) }
    var durationMinutes by remember { mutableIntStateOf(45) }
    var activityMetIndex by remember { mutableIntStateOf(1) }
    val w = weightInput.toDoubleOrNull() ?: 70.0
    val mets = listOf(3.5, 8.0, 7.5, 6.0, 5.0)
    val activityNames = listOf("🚶 Caminar", "🏃 Correr", "🚴 Ciclismo", "🏊 Natación", "🏋️ Gimnasio")
    val selectedMet = mets[activityMetIndex]
    val caloriesBurned = (selectedMet * w * (durationMinutes / 60.0)).toInt()
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color(0xFF10B981).copy(alpha = 0.15f), modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.DirectionsRun, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = "Gasto Calórico por Actividad", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Cálculo por METs", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        OutlinedTextField(value = weightInput, onValueChange = { weightInput = it }, label = { Text("Tu Peso Corporal (kg)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Text(text = "Selecciona la Actividad", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            activityNames.forEachIndexed { idx, name ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (activityMetIndex == idx) Color(0xFF10B981) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f).clickable { activityMetIndex = idx }.padding(vertical = 8.dp),
                    contentColor = if (activityMetIndex == idx) Color.White else MaterialTheme.colorScheme.onSurface
                ) { Box(contentAlignment = Alignment.Center) { Text(text = name, fontSize = 10.sp, fontWeight = FontWeight.Bold) } }
            }
        }
        Text(text = "Duración: $durationMinutes minutos", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Slider(value = durationMinutes.toFloat(), onValueChange = { durationMinutes = it.toInt() }, valueRange = 10f..180f)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "CALORÍAS QUEMADAS ESTIMADAS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                Text(text = "~$caloriesBurned kcal", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
            }
        }
        ToolExplanationCard(
            whatItMeasures = "Mide las calorías quemadas durante un ejercicio o deporte según su intensidad y duración.",
            whatIsItFor = "Calcular el gasto energético adicional al realizar actividades como caminar, correr, nadar o ir al gimnasio.",
            interpretation = "Se calcula con los Equivalentes Metabólicos de Tarea (METs), donde 1 MET equivale al gasto calórico en reposo."
        )
        FormulaCard(
            formulaTitle = "Cálculo por Equivalentes Metabólicos (METs)",
            formulaExpression = "Calorías Quemadas = MET × Peso (kg) × (Duración Minutos / 60)",
            variablesNotes = "METs: Caminar = 3.5, Correr = 8.0, Ciclismo = 7.5, Natación = 6.0, Gimnasio = 5.0.",
            medicalStandard = "Compendio ACSM de Actividades Físicas"
        )
    }
}
@Composable
fun ProfileWaistToHeightTool(activeMember: FamilyMember?) {
    val context = LocalContext.current
    val dbHelper = remember { BmiDatabaseHelper(context) }
    var waistInput by remember { mutableStateOf("82") }
    var heightInput by remember { mutableStateOf("170") }
    val waist = waistInput.toDoubleOrNull() ?: 82.0
    val height = heightInput.toDoubleOrNull() ?: 170.0
    val ice = remember(waist, height) {
        MedicalCalculators.calculateWaistToHeightRatio(waist, height)
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color(0xFF8B5CF6).copy(alpha = 0.15f), modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.MonitorWeight, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = "Índice Cintura-Estatura (ICE)", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Para ${activeMember?.name ?: "Mi Perfil"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = waistInput,
                onValueChange = { waistInput = it },
                label = { Text("Cintura (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = heightInput,
                onValueChange = { heightInput = it },
                label = { Text("Estatura (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(ice.statusColorHex).copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "EVALUACIÓN RIESGO CARDIOVASCULAR (ICE)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = String.format("%.2f", ice.ratio), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(ice.statusColorHex))
                Text(text = ice.categoryText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(ice.statusColorHex))
                Button(
                    onClick = {
                        val memberId = activeMember?.id ?: 0L
                        dbHelper.insertProfileMeasurement(
                            ProfileMeasurement(
                                memberId = memberId,
                                metricType = "ICE",
                                valueNumeric = ice.ratio,
                                unitLabel = "ICE",
                                statusLabel = ice.categoryText
                            )
                        )
                        Toast.makeText(context, context.getString(R.string.toast_medicion_guardada, activeMember?.name ?: context.getString(R.string.mi_perfil)), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(id = R.string.guardar_registro_en_perfil, activeMember?.name ?: stringResource(id = R.string.mi_perfil)), fontWeight = FontWeight.Bold)
                }
            }
        }
        ToolExplanationCard(
            whatItMeasures = "Mide la relación entre la circunferencia de la cintura y la estatura corporal.",
            whatIsItFor = "Detectar la acumulación de grasa visceral abdominal y evaluar el riesgo cardiometabólico.",
            interpretation = "Límite saludable universal: ICE < 0.50 (Tu cintura debe medir menos de la mitad de tu estatura)."
        )
        FormulaCard(
            formulaTitle = "Fórmula del Índice Cintura-Estatura (ICE / WHtR)",
            formulaExpression = "ICE = Perímetro Cintura (cm) / Estatura (cm)",
            variablesNotes = "Rango de Salud: ICE < 0.50 (Sin riesgo) | ICE 0.50 - 0.59 (Riesgo Aumentado) | ICE >= 0.60 (Alto Riesgo).",
            medicalStandard = "Estándar OMS / UK NICE"
        )
    }
}
@Composable
fun MacronutrientsTool(activeMember: FamilyMember? = null) {
    val context = LocalContext.current
    val initialBmr = remember(activeMember?.id) {
        getInitialValueForMember(context, activeMember, "MACROS", "1650")
    }
    var bmrInput by remember(activeMember?.id, initialBmr) { mutableStateOf(initialBmr) }
    var activityIndex by remember { mutableIntStateOf(1) }
    var goalIndex by remember { mutableIntStateOf(0) }
    val bmr = bmrInput.toIntOrNull() ?: 1650
    val macros = remember(bmr, activityIndex, goalIndex) {
        MedicalCalculators.calculateMacronutrients(bmr, activityIndex, goalIndex)
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color(0xFFFF9800).copy(alpha = 0.15f), modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Restaurant, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = "Calculadora de Macronutrientes", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Proteínas, Carbohidratos y Grasas", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        OutlinedTextField(
            value = bmrInput,
            onValueChange = { bmrInput = it },
            label = { Text("Tasa Metabólica Basal / Calorías Reposo (kcal)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "META CALÓRICA DIARIA (${macros.goalText})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(text = "${macros.targetCalories} kcal / día", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MacroBox(title = "Proteínas", value = "${macros.proteinGrams} g", color = Color(0xFF0288D1), modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    MacroBox(title = "Carbs", value = "${macros.carbsGrams} g", color = Color(0xFF10B981), modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    MacroBox(title = "Grasas", value = "${macros.fatGrams} g", color = Color(0xFFFF9800), modifier = Modifier.weight(1f))
                }
            }
        }
        ToolExplanationCard(
            whatItMeasures = "Mide la distribución diaria necesaria en gramos de Proteínas, Carbohidratos y Grasas.",
            whatIsItFor = "Estructurar la dieta para objetivos de déficit calórico (pérdida de grasa), mantenimiento o hipertrofia.",
            interpretation = "Proteínas (4 kcal/g) construyen tejido magro, Carbohidratos (4 kcal/g) brindan energía y Grasas (9 kcal/g) regulan hormonas."
        )
        FormulaCard(
            formulaTitle = "Distribución de Macronutrientes (30% Proteína, 45% Carbs, 25% Grasas)",
            formulaExpression = "Proteínas (g) = (kcal Target × 0.30) / 4\nCarbohidratos (g) = (kcal Target × 0.45) / 4\nGrasas (g) = (kcal Target × 0.25) / 9",
            variablesNotes = "Target kcal = TMB × Factor Actividad (1.20 - 1.75) × Factor Meta (-20% / Mantenimiento / +15%).",
            medicalStandard = "Guías Nutricionales ISSN"
        )
    }
}
@Composable
fun MacroBox(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.15f), modifier = modifier) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}
@Composable
fun ProfileHeadCircumferenceTool(activeMember: FamilyMember?) {
    val context = LocalContext.current
    val dbHelper = remember { BmiDatabaseHelper(context) }
    val babyGender = activeMember?.gender ?: Gender.FEMALE
    var ageMonths by remember { mutableIntStateOf(6) }
    var headInput by remember { mutableStateOf("43.0") }
    val headCm = headInput.toDoubleOrNull() ?: 43.0
    val headResult = remember(babyGender, ageMonths, headCm) {
        MedicalCalculators.calculateHeadCircumference(babyGender, ageMonths, headCm)
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color(0xFFE91E63).copy(alpha = 0.15f), modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.ChildCare, contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = "Perímetro Cefálico Pediátrico", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Para ${activeMember?.name ?: "Bebé"} (${if (babyGender == Gender.FEMALE) "Niña 🌸" else "Niño 🔷"})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        OutlinedTextField(
            value = headInput,
            onValueChange = { headInput = it },
            label = { Text("Perímetro Cefálico (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text(text = "Edad del Bebé: $ageMonths meses", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Slider(value = ageMonths.toFloat(), onValueChange = { ageMonths = it.toInt() }, valueRange = 0f..24f)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = if (headResult.isNormal) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "EVALUACIÓN PERÍMETRO CEFÁLICO OMS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = "${headResult.headCm} cm", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Text(text = headResult.statusText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Button(
                    onClick = {
                        val memberId = activeMember?.id ?: 0L
                        dbHelper.insertProfileMeasurement(
                            ProfileMeasurement(
                                memberId = memberId,
                                metricType = "HEAD_CIRCUMFERENCE",
                                valueNumeric = headResult.headCm,
                                unitLabel = "cm",
                                statusLabel = headResult.statusText
                            )
                        )
                        Toast.makeText(context, context.getString(R.string.toast_medicion_guardada, activeMember?.name ?: context.getString(R.string.bebe)), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(id = R.string.guardar_registro_en_perfil, activeMember?.name ?: stringResource(id = R.string.bebe)), fontWeight = FontWeight.Bold)
                }
            }
        }
        ToolExplanationCard(
            whatItMeasures = "Mide el crecimiento del perímetro de la cabeza en niños de 0 a 24 meses.",
            whatIsItFor = "Monitorear el desarrollo neurológico y cerebral en los primeros dos años de vida.",
            interpretation = "Compara la medida con los patrones infantiles estándar de la OMS (Percentiles P3 a P97)."
        )
        FormulaCard(
            formulaTitle = "Z-Score y Tablas Percentilares de Perímetro Cefálico OMS",
            formulaExpression = "Z-Score OMS = (Perímetro Medido - Mediana OMS) / Desviación Estándar OMS",
            variablesNotes = "Rango saludable: Entre -2 DE y +2 DE (Percentil 3 a 97).",
            medicalStandard = "Patrones de Crecimiento Infantil OMS"
        )
    }
}
@Composable
fun TannerHeightTool(activeMember: FamilyMember?) {
    val childGender = activeMember?.gender ?: Gender.MALE
    var fatherHeightInput by remember { mutableStateOf("175") }
    var motherHeightInput by remember { mutableStateOf("162") }
    val fatherH = fatherHeightInput.toDoubleOrNull() ?: 175.0
    val motherH = motherHeightInput.toDoubleOrNull() ?: 162.0
    val prediction = remember(childGender, fatherH, motherH) {
        MedicalCalculators.calculateTannerTargetHeight(childGender, fatherH, motherH)
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color(0xFF00A88F).copy(alpha = 0.15f), modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Height, contentDescription = null, tint = Color(0xFF00A88F), modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = "Predicción de Talla Adulta", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Para ${activeMember?.name ?: "Mi Perfil"} (${if (childGender == Gender.FEMALE) "Niña 🌸" else "Niño 🔷"})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(value = fatherHeightInput, onValueChange = { fatherHeightInput = it }, label = { Text("Padre (cm)") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = motherHeightInput, onValueChange = { motherHeightInput = it }, label = { Text("Madre (cm)") }, modifier = Modifier.weight(1f), singleLine = true)
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = stringResource(id = R.string.talla_adulta_objetivo_estimada), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = prediction.formattedTarget, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        ToolExplanationCard(
            whatItMeasures = "Mide la talla adulta objetivo genéticamente esperada en base a la estatura de ambos padres.",
            whatIsItFor = "Proyectar la estatura máxima esperada al alcanzar la madurez esquelética adulta.",
            interpretation = "Establece el rango genético esperado de estatura infantil con un margen de variabilidad de ± 5.0 cm."
        )
        FormulaCard(
            formulaTitle = "Fórmula de Talla Media Parental de Tanner",
            formulaExpression = "Niño (cm) = (Estatura Padre + Estatura Madre + 13 cm) / 2\nNiña (cm) = (Estatura Padre + Estatura Madre - 13 cm) / 2",
            variablesNotes = "Intervalo de confianza genético: Talla Objetivo ± 5.0 cm.",
            medicalStandard = "Fórmula de Tanner & Whitehouse (Pediatría)"
        )
    }
}
@Composable
fun PregnancyGainTool(activeMember: FamilyMember?) {
    var preWeightInput by remember { mutableStateOf("58") }
    var heightInput by remember { mutableStateOf("163") }
    var currentWeightInput by remember { mutableStateOf("64") }
    var currentWeek by remember { mutableIntStateOf(24) }
    val preW = preWeightInput.toDoubleOrNull() ?: 58.0
    val h = heightInput.toDoubleOrNull() ?: 163.0
    val curW = currentWeightInput.toDoubleOrNull() ?: 64.0
    val preg = remember(preW, h, currentWeek, curW) {
        MedicalCalculators.calculatePregnancyWeightGain(preW, h, currentWeek, curW)
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color(0xFFEC407A).copy(alpha = 0.15f), modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Female, contentDescription = null, tint = Color(0xFFEC407A), modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = "Peso en el Embarazo (IOM / OMS)", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Para ${activeMember?.name ?: "Mamá"} (Perfil Femenino 🌸)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        OutlinedTextField(value = currentWeightInput, onValueChange = { currentWeightInput = it }, label = { Text("Peso Actual (kg)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEC407A).copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = stringResource(id = R.string.peso_ganado_en_embarazo), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = "+${String.format("%.1f kg", preg.totalGainKg)}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Text(text = preg.statusText, fontSize = 12.sp)
            }
        }
        ToolExplanationCard(
            whatItMeasures = "Mide la ganancia de peso materna acumulada durante las semanas de gestación.",
            whatIsItFor = "Evaluar si el aumento de peso se mantiene dentro del rango seguro recomendado para prevenir complicaciones.",
            interpretation = "El rango recomendado depende directamente del IMC previo al embarazo (IOM / OMS)."
        )
        FormulaCard(
            formulaTitle = "Pautas del Instituto de Medicina (IOM 2009 / OMS)",
            formulaExpression = "Ganancia Esperada (kg) = Ganancia Trimestre 1 + [ Tasa Semanal IOM × (Semana - 13) ]",
            variablesNotes = "Tasas semanales: Bajo peso (0.51 kg), Normal (0.42 kg), Sobrepeso (0.28 kg), Obesidad (0.22 kg).",
            medicalStandard = "Pautas del IOM 2009 / OMS Embarazo"
        )
    }
}
@Composable
fun PediatricRemindersTool(activeMember: FamilyMember?) {
    val context = LocalContext.current
    val notifManager = remember { PediatricNotificationManager(context) }
    var childNameInput by remember(activeMember?.name) { mutableStateOf(activeMember?.name ?: "Lucas") }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color(0xFF3F51B5).copy(alpha = 0.15f), modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFF3F51B5), modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = "Alertas Pediátricas", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Recordatorios para $childNameInput", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        OutlinedTextField(
            value = childNameInput,
            onValueChange = { childNameInput = it },
            label = { Text("Nombre del Bebé / Hijo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                notifManager.schedulePediatricReminder(childNameInput, 30)
                Toast.makeText(context, "Recordatorio programado en 30 días para $childNameInput", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Activar Alerta Mensual (30 días)", fontWeight = FontWeight.Bold)
        }
    }
}
@Composable
fun BackupRestoreTool() {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color(0xFF607D8B).copy(alpha = 0.15f), modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Backup, contentDescription = null, tint = Color(0xFF607D8B), modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = "Copia de Seguridad y Restauración", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Respaldo en JSON", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Button(
            onClick = {
                val jsonBackup = BackupRestoreManager.exportBackupJson(context)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, jsonBackup)
                }
                context.startActivity(Intent.createChooser(intent, "Compartir Respaldo (JSON)"))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Exportar y Compartir Respaldo (JSON)", fontWeight = FontWeight.Bold)
        }
    }
}
@Composable
fun IdealBodyWeightTool(member: FamilyMember?) {
    val context = LocalContext.current
    val gender = member?.gender ?: Gender.MALE
    val initialHeight = remember(member?.id) {
        getInitialValueForMember(context, member, "IBW", "170.0") { it.heightCm.toInt().toString() }
    }
    val initialWeight = remember(member?.id) {
        getInitialValueForMember(context, member, "IBW", "70.0") { it.weightKg.toString() }
    }
    var heightInput by remember(member?.id, initialHeight) { mutableStateOf(initialHeight) }
    var weightInput by remember(member?.id, initialWeight) { mutableStateOf(initialWeight) }
    val h = heightInput.toDoubleOrNull() ?: 170.0
    val w = weightInput.toDoubleOrNull() ?: 0.0
    val result = remember(gender, h, w) {
        MedicalCalculators.calculateIdealBodyWeight(gender, h, w)
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "⚖️ Calculadora de Peso Corporal Ideal (IBW)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = "Calcula el peso ideal estimado según estatura utilizando las 5 fórmulas médicas clínicas más reconocidas a nivel mundial.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = heightInput,
                    onValueChange = { heightInput = it },
                    label = { Text("Estatura / Talla en cm (ej. 170)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Peso Actual en kg (opcional, ej. 75)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "PESO IDEAL PROMEDIO ESTIMADO", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Text(text = "${String.format("%.1f", result.averageIbwKg)} kg", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                if (w > 0) {
                    val diffStr = if (result.weightDiffKg > 0) "+${String.format("%.1f", result.weightDiffKg)} kg sobre el ideal" else "${String.format("%.1f", result.weightDiffKg)} kg bajo el ideal"
                    Text(text = "Diferencia con peso actual: $diffStr", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Desglose por Fórmulas Médicas:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                IbwFormulaRow("Devine (Uso Clínico)", result.devineKg)
                IbwFormulaRow("Robinson (Nutrición)", result.robinsonKg)
                IbwFormulaRow("Miller", result.millerKg)
                IbwFormulaRow("Hamwi (ADA)", result.hamwiKg)
                IbwFormulaRow("Broca (Clásica)", result.brocaKg)
            }
        }
        ToolExplanationCard(
            whatItMeasures = "Mide el peso corporal ideal estimado en kilos en base a la estatura y la estructura corporal.",
            whatIsItFor = "Establecer metas de peso saludables y dosificación en farmacología clínica.",
            interpretation = "Compara las 5 fórmulas históricas y clínicas más utilizadas en el mundo."
        )
        FormulaCard(
            formulaTitle = "Fórmulas Médicas Clásicas de Peso Ideal (IBW)",
            formulaExpression = "Devine (H): 50 + 2.3 × (Estatura pulg - 60)\nDevine (M): 45.5 + 2.3 × (Estatura pulg - 60)\nBroca: (Estatura cm - 100) × Factor Sexo (0.90 / 0.85)",
            variablesNotes = "Fórmulas: Devine, Robinson, Miller, Hamwi (ADA) y Broca.",
            medicalStandard = "Asociación Americana de Diabetes (ADA)"
        )
    }
}
@Composable
private fun IbwFormulaRow(formulaName: String, valueKg: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = formulaName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "${String.format("%.1f", valueKg)} kg", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
@Composable
fun WaistToHipTool(member: FamilyMember?) {
    val context = LocalContext.current
    val gender = member?.gender ?: Gender.MALE
    val initialWaist = remember(member?.id) {
        getInitialValueForMember(context, member, "WHR", "80.0")
    }
    val initialHip = remember(member?.id) {
        getInitialValueForMember(context, member, "WHR", "95.0")
    }
    var waistInput by remember(member?.id, initialWaist) { mutableStateOf(initialWaist) }
    var hipInput by remember(member?.id, initialHip) { mutableStateOf(initialHip) }
    val waist = waistInput.toDoubleOrNull() ?: 80.0
    val hip = hipInput.toDoubleOrNull() ?: 95.0
    val result = remember(gender, waist, hip) {
        MedicalCalculators.calculateWaistToHipRatio(gender, waist, hip)
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "⏳ Índice Cintura-Cadera (ÍCC / WHR)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = "Evalúa la distribución de grasa corporal y el riesgo cardiovascular de acuerdo con los estándares de la Organización Mundial de la Salud (OMS).", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = waistInput,
                    onValueChange = { waistInput = it },
                    label = { Text("Perímetro de Cintura en cm (ej. 80)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = hipInput,
                    onValueChange = { hipInput = it },
                    label = { Text("Perímetro de Cadera en cm (ej. 95)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(result.statusColorHex).copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "ÍNDICE CINTURA-CADERA (ÍCC)", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(result.statusColorHex))
                Text(text = String.format("%.2f", result.ratio), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(result.statusColorHex))
                Text(text = result.riskLevel, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = "Distribución corporal: ${result.bodyShape}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        ToolExplanationCard(
            whatItMeasures = "Mide la relación circunferencia cintura/cadera para definir el patrón de distribución de grasa.",
            whatIsItFor = "Diferenciar el patrón androide (manzana, mayor riesgo cardiometabólico) del patrón ginoide (pera).",
            interpretation = "Criterios OMS: Riesgo Alto en Hombres >= 1.00, Mujeres >= 0.85."
        )
        FormulaCard(
            formulaTitle = "Fórmula del Índice Cintura-Cadera (ÍCC / WHR)",
            formulaExpression = "ÍCC = Perímetro Cintura (cm) / Perímetro Cadera (cm)",
            variablesNotes = "Riesgo Alto OMS: Hombres > 0.99, Mujeres > 0.84.",
            medicalStandard = "Organización Mundial de la Salud (OMS)"
        )
    }
}
@Composable
fun HeartRateZonesTool(member: FamilyMember?) {
    var ageInput by remember { mutableStateOf("30") }
    val age = ageInput.toIntOrNull() ?: 30
    val result = remember(age) {
        MedicalCalculators.calculateHeartRateZones(age)
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "❤️ Frecuencia Cardíaca Máxima & Zonas de Entrenamiento", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = "Calcula la Frecuencia Cardíaca Máxima (FCM) mediante la fórmula clínica de Tanaka y desglosa las 5 zonas de intensidad de entrenamiento.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = ageInput,
                    onValueChange = { ageInput = it },
                    label = { Text("Edad en años (ej. 30)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "FCM TANAKA", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    Text(text = "${result.tanakaMaxHr} bpm", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "FCM HASKELL", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    Text(text = "${result.haskellMaxHr} bpm", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        Text(text = "Zonas de Intensidad de Entrenamiento:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        result.zones.forEach { zone ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = zone.zoneName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = zone.percentRange, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(text = zone.rangeBpmText, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = zone.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        ToolExplanationCard(
            whatItMeasures = "Mide la Frecuencia Cardíaca Máxima (FCM) y las 5 zonas de intensidad de esfuerzo en latidos por minuto.",
            whatIsItFor = "Guiar el ejercicio seguro y eficiente según el objetivo (Quema de grasa, Acondicionamiento aeróbico o Esfuerzo máximo).",
            interpretation = "Zona 2 (60-70%) optimiza la oxidación de grasas. Zonas 3 y 4 (70-90%) aumentan la resistencia aeróbica."
        )
        FormulaCard(
            formulaTitle = "Fórmulas de Tanaka y Haskell-Fox para FCM",
            formulaExpression = "FCM Tanaka = 208 - (0.7 × Edad en años)\nFCM Haskell = 220 - Edad en años",
            variablesNotes = "Zonas: Z1 (50-60%), Z2 (60-70%), Z3 (70-80%), Z4 (80-90%), Z5 (90-100%).",
            medicalStandard = "Colegio Americano de Medicina del Deporte (ACSM)"
        )
    }
}
@Composable
fun BodyAdiposityTool(member: FamilyMember?) {
    val gender = member?.gender ?: Gender.MALE
    var heightInput by remember { mutableStateOf("170.0") }
    var hipInput by remember { mutableStateOf(if (gender == Gender.FEMALE) "95.0" else "92.0") }
    val h = heightInput.toDoubleOrNull() ?: 170.0
    val hip = hipInput.toDoubleOrNull() ?: 92.0
    val result = remember(gender, h, hip) {
        MedicalCalculators.calculateBodyAdiposityIndex(gender, h, hip)
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "📐 Índice de Adiposidad Corporal (IAC / BAI)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = "Estima el porcentaje de grasa corporal utilizando la relación entre el perímetro de cadera y la estatura, sin necesidad de usar báscula.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = heightInput,
                    onValueChange = { heightInput = it },
                    label = { Text("Estatura en cm (ej. 170)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = hipInput,
                    onValueChange = { hipInput = it },
                    label = { Text("Perímetro de Cadera en cm (ej. 92)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(result.statusColorHex).copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "PORCENTAJE DE GRASA (IAC / BAI)", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(result.statusColorHex))
                Text(text = "${String.format("%.1f", result.baiValue)}%", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(result.statusColorHex))
                Text(text = result.categoryText, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        ToolExplanationCard(
            whatItMeasures = "Mide el porcentaje estimado de grasa corporal en base al perímetro de la cadera y la estatura.",
            whatIsItFor = "Estimar la adiposidad corporal sin necesidad de báscula.",
            interpretation = "Útil en personas musculosas donde el IMC tradicional puede sobreestimar sobrepeso u obesidad."
        )
        FormulaCard(
            formulaTitle = "Fórmula del Índice de Adiposidad Corporal (IAC / BAI)",
            formulaExpression = "IAC / BAI (%) = [ Perímetro Cadera (cm) / (Estatura en m)^1.5 ] - 18",
            variablesNotes = "Desarrollado por Bergman et al. en el estudio Obesity Journal.",
            medicalStandard = "Estudio Clínico Bergman"
        )
    }
}
@Composable
fun LeanBodyMassTool(member: FamilyMember?) {
    val gender = member?.gender ?: Gender.MALE
    var heightInput by remember { mutableStateOf("170.0") }
    var weightInput by remember { mutableStateOf("70.0") }
    val h = heightInput.toDoubleOrNull() ?: 170.0
    val w = weightInput.toDoubleOrNull() ?: 70.0
    val result = remember(gender, h, w) {
        MedicalCalculators.calculateLeanBodyMass(gender, h, w)
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "💪 Masa Magra y Masa Grasa en Kilos (LBM)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = "Calcula la masa magra (músculo, agua, órganos) y masa grasa en kilos comparando las fórmulas clínicas de Boer y James.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = heightInput,
                    onValueChange = { heightInput = it },
                    label = { Text("Estatura en cm (ej. 170)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Peso Corporal en kg (ej. 70)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "MASA MAGRA PROMEDIO (LBM)", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Text(text = "${String.format("%.1f", result.averageLbmKg)} kg", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Masa Grasa", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        Text(text = "${String.format("%.1f", result.fatMassKg)} kg", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "% Grasa Estimado", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        Text(text = "${String.format("%.1f", result.fatPercentage)}%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Comparación de Fórmulas Médicas:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Fórmula de Boer:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${String.format("%.1f", result.boerLbmKg)} kg magros", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Fórmula de James:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${String.format("%.1f", result.jamesLbmKg)} kg magros", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        ToolExplanationCard(
            whatItMeasures = "Mide los kilos de masa magra (músculos, agua, órganos) y masa grasa corporal.",
            whatIsItFor = "Evaluar el tono muscular y la composición corporal en atletas y adultos.",
            interpretation = "Compara las fórmulas clínicas de Boer y James para ofrecer una estimación precisa."
        )
        FormulaCard(
            formulaTitle = "Fórmulas de Boer y James para Masa Magra",
            formulaExpression = "Boer (H): 0.407W + 0.267H - 19.2 | Boer (M): 0.252W + 0.473H - 48.3\nJames (H): 1.1W - 128(W/H)² | James (M): 1.07W - 148(W/H)²",
            variablesNotes = "Donde: W = Peso Corporal (kg), H = Estatura (cm).",
            medicalStandard = "Fórmulas Clínicas Boer & James"
        )
    }
}
@Composable
fun TotalBodyWaterTool(member: FamilyMember?) {
    val gender = member?.gender ?: Gender.MALE
    var ageInput by remember { mutableStateOf("30") }
    var heightInput by remember { mutableStateOf("170.0") }
    var weightInput by remember { mutableStateOf("70.0") }
    val age = ageInput.toIntOrNull() ?: 30
    val h = heightInput.toDoubleOrNull() ?: 170.0
    val w = weightInput.toDoubleOrNull() ?: 70.0
    val result = remember(gender, age, h, w) {
        MedicalCalculators.calculateTotalBodyWater(gender, age, h, w)
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "💧 Agua Corporal Total (TBW - Watson & Hume)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = "Calcula los litros exactos de agua corporal y el porcentaje de hidratación del cuerpo utilizando las fórmulas clínicas de Watson y Hume.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { heightInput = it },
                        label = { Text("Estatura (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Peso (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = ageInput,
                    onValueChange = { ageInput = it },
                    label = { Text("Edad (años)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0288D1).copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "AGUA CORPORAL TOTAL ESTIMADA", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0288D1))
                Text(text = "${String.format("%.1f", result.averageLiters)} Litros", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0288D1))
                Text(text = "Porcentaje corporal: ${String.format("%.1f", result.waterPercentage)}%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = result.statusText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Desglose por Fórmulas Médicas:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Fórmula de Watson (Clínica):", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${String.format("%.1f", result.watsonLiters)} Litros", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Fórmula de Hume:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${String.format("%.1f", result.humeLiters)} Litros", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        ToolExplanationCard(
            whatItMeasures = "Mide los litros de agua corporal total y el porcentaje de agua sobre el peso corporal.",
            whatIsItFor = "Evaluar el estado de hidratación celular y el volumen hídrico corporal.",
            interpretation = "El porcentaje normal de agua corporal suele estar entre 45% y 65% del peso total."
        )
        FormulaCard(
            formulaTitle = "Fórmulas Clínicas de Watson y Hume de Agua Corporal Total",
            formulaExpression = "Watson (H): 2.447 - 0.09156A + 0.1074H + 0.3362W\nWatson (M): -2.097 + 0.1069H + 0.2466W",
            variablesNotes = "Donde: A = Edad (años), H = Estatura (cm), W = Peso (kg).",
            medicalStandard = "Fórmula Watson / Fisiología Renal"
        )
    }
}
@Composable
fun RohrerIndexTool(member: FamilyMember?) {
    var weightInput by remember { mutableStateOf("3.4") }
    var heightInput by remember { mutableStateOf(if ((weightInput.toDoubleOrNull() ?: 3.4) < 10) "50.0" else "85.0") }
    val w = weightInput.toDoubleOrNull() ?: 3.4
    val h = heightInput.toDoubleOrNull() ?: 50.0
    val result = remember(w, h) {
        MedicalCalculators.calculateRohrerPonderalIndex(w, h)
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "👶 Índice Ponderal de Rohrer (Neonatal y Pediátrico)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = "Mide la proporcionalidad corporal en recién nacidos y bebés para detectar desnutrición intrauterina o desarrollo asimétrico.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Peso del Bebé en kg (ej. 3.4)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = heightInput,
                    onValueChange = { heightInput = it },
                    label = { Text("Longitud / Estatura en cm (ej. 50)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(result.statusColorHex).copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "ÍNDICE PONDERAL DE ROHRER", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(result.statusColorHex))
                Text(text = String.format("%.2f", result.indexValue), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(result.statusColorHex))
                Text(text = result.statusText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
@Composable
fun InfantRequirementTool(member: FamilyMember?) {
    var ageMonthsInput by remember { mutableStateOf("4") }
    var weightInput by remember { mutableStateOf("6.5") }
    val m = ageMonthsInput.toIntOrNull() ?: 4
    val w = weightInput.toDoubleOrNull() ?: 6.5
    val result = remember(m, w) {
        MedicalCalculators.calculateInfantRequirement(m, w)
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "🍼 Requerimientos Calóricos y Volumétricos del Lactante", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = "Estima las calorías diarias y el volumen de leche/fórmula en mililitros para lactantes de 0 a 12 meses según recomendaciones de la OMS y la AAP.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = ageMonthsInput,
                    onValueChange = { ageMonthsInput = it },
                    label = { Text("Edad en Meses (0 - 12)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Peso Corporal en kg (ej. 6.5)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "REQUERIMIENTO DIARIO RECOMENDADO", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Text(text = "${result.caloriesPerDay} kcal / día", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(text = "~${result.milkVolumeMlPerDay} ml de leche al día", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(text = "Promedio por toma: ~${result.volumePerFeedingMl} ml", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}
@Composable
fun BloodPressureTool(activeMember: FamilyMember? = null) {
    val context = LocalContext.current
    val initialSys = remember(activeMember?.id) {
        getInitialValueForMember(context, activeMember, "PAM", "120")
    }
    val initialDia = remember(activeMember?.id) {
        getInitialValueForMember(context, activeMember, "PAM", "80")
    }
    var sysInput by remember(activeMember?.id, initialSys) { mutableStateOf(initialSys) }
    var diaInput by remember(activeMember?.id, initialDia) { mutableStateOf(initialDia) }
    val sys = sysInput.toIntOrNull() ?: 120
    val dia = diaInput.toIntOrNull() ?: 80
    val result = remember(sys, dia) {
        MedicalCalculators.calculateMeanArterialPressure(sys, dia)
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "🩸 Presión Arterial Media (PAM) & Presión de Pulso", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = "Calcula la Presión Arterial Media (PAM) e identifica la clasificación de riesgo vascular según los criterios de la AHA y la OMS.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = sysInput,
                    onValueChange = { sysInput = it },
                    label = { Text("Sistólica (mmHg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = diaInput,
                    onValueChange = { diaInput = it },
                    label = { Text("Diastólica (mmHg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(result.statusColorHex).copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "PRESIÓN ARTERIAL MEDIA (PAM)", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(result.statusColorHex))
                Text(text = "${String.format("%.1f", result.meanArterialPressure)} mmHg", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(result.statusColorHex))
                Text(text = result.categoryText, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = "Presión de Pulso: ${result.pulsePressure} mmHg", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
@Composable
fun Vo2MaxTool(member: FamilyMember?) {
    val gender = member?.gender ?: Gender.MALE
    var ageInput by remember { mutableStateOf("30") }
    var heightInput by remember { mutableStateOf("170.0") }
    var weightInput by remember { mutableStateOf("70.0") }
    var activityIndex by remember { mutableIntStateOf(3) }
    val age = ageInput.toIntOrNull() ?: 30
    val h = heightInput.toDoubleOrNull() ?: 170.0
    val w = weightInput.toDoubleOrNull() ?: 70.0
    val activityLevels = listOf("0: Nula", "1-2: Ligera", "3-5: Moderada", "6-7: Intensa")
    val result = remember(gender, age, h, w, activityIndex) {
        MedicalCalculators.calculateVo2MaxNonExercise(gender, age, h, w, activityIndex * 2)
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "🫁 VO2 Máx Estimado (Consumo Máximo de Oxígeno)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = "Estima la capacidad aeróbica máxima y salud cardiorrespiratoria sin necesidad de pruebas de esfuerzo mediante el modelo de Jackson.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { heightInput = it },
                        label = { Text("Estatura (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Peso (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = ageInput,
                    onValueChange = { ageInput = it },
                    label = { Text("Edad (años)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(text = "Nivel Habitual de Actividad Física:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    activityLevels.forEachIndexed { idx, title ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (activityIndex == idx) Color(0xFF10B981) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f).clickable { activityIndex = idx }.padding(vertical = 8.dp),
                            contentColor = if (activityIndex == idx) Color.White else MaterialTheme.colorScheme.onSurface
                        ) { Box(contentAlignment = Alignment.Center) { Text(text = title, fontSize = 9.sp, fontWeight = FontWeight.Bold) } }
                    }
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(result.statusColorHex).copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "VO2 MÁX ESTIMADO", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(result.statusColorHex))
                Text(text = "${String.format("%.1f", result.vo2MaxMlKgMin)} mL/kg/min", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(result.statusColorHex))
                Text(text = result.categoryText, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        ToolExplanationCard(
            whatItMeasures = "Mide el consumo máximo de oxígeno en mL/kg/min (capacidad aeróbica máxima).",
            whatIsItFor = "Evaluar el estado de salud cardiorrespiratorio sin necesidad de pruebas de esfuerzo agitadas.",
            interpretation = "Valores > 42 mL/kg/min indican una capacidad aeróbica buena o excelente."
        )
        FormulaCard(
            formulaTitle = "Modelo de Regresión de Jackson (Non-Exercise VO2Max)",
            formulaExpression = "VO2Max = 56.363 + 1.921(Act) - 0.381(Edad) - 0.754(IMC) + 10.987(Sexo)",
            variablesNotes = "Donde: Act = Nivel de Actividad Física (0 a 7), Sexo = Hombre (1) / Mujer (0).",
            medicalStandard = "Modelo Jackson / Medicine & Science in Sports"
        )
    }
}
