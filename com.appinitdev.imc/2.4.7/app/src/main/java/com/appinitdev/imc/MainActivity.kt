sealed class Screen {
    object Home : Screen()
    data class Calculator(
        val category: AgeCategory,
        val preselectedSubIndex: Int = 0,
        val preselectedGender: Gender = Gender.MALE
    ) : Screen()
    data class Result(val result: BmiResult, val gender: Gender) : Screen()
    object History : Screen()
    object FamilyProfiles : Screen()
    object HealthTools : Screen()
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdManager.initialize(this)
        setContent {
            val context = LocalContext.current
            val themeManager = remember { ThemeManager(context) }
            var activeThemeMode by remember { mutableStateOf(themeManager.getThemeMode()) }
            var activeThemePreset by remember { mutableStateOf(themeManager.getThemePreset()) }
            val useDarkTheme = when (activeThemeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            IMCTheme(
                darkTheme = useDarkTheme,
                preset = activeThemePreset
            ) {
                MainAppNav(
                    activeThemeMode = activeThemeMode,
                    activeThemePreset = activeThemePreset,
                    onThemeModeChange = { newMode ->
                        themeManager.setThemeMode(newMode)
                        activeThemeMode = newMode
                    },
                    onThemePresetChange = { newPreset ->
                        themeManager.setThemePreset(newPreset)
                        activeThemePreset = newPreset
                    }
                )
            }
        }
    }
}
@Composable
fun MainAppNav(
    activeThemeMode: ThemeMode,
    activeThemePreset: ThemePreset,
    onThemeModeChange: (ThemeMode) -> Unit,
    onThemePresetChange: (ThemePreset) -> Unit
) {
    val context = LocalContext.current
    val familyManager = remember { FamilyProfileManager(context) }
    var allMembers by remember { mutableStateOf(familyManager.getMembers()) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showProfileSwitchDialog by remember { mutableStateOf(false) }
    var showAddProfileDialog by remember { mutableStateOf(false) }
    var memberToEditInMain by remember { mutableStateOf<FamilyMember?>(null) }
    var memberToDeleteInMain by remember { mutableStateOf<FamilyMember?>(null) }
    var activeMember by remember(allMembers) {
        mutableStateOf(familyManager.getActiveMember())
    }
    var showOnboardingDialog by remember {
        mutableStateOf(allMembers.isEmpty())
    }
    val historyManager = rememberHistoryManager()
    BackHandler(enabled = currentScreen !is Screen.Home) {
        currentScreen = Screen.Home
    }
    when (val screen = currentScreen) {
        is Screen.Home -> {
            HomeScreen(
                activeMember = activeMember,
                onSwitchProfileClick = {
                    showProfileSwitchDialog = true
                },
                onEditActiveProfileClick = {
                    val target = activeMember
                    if (target != null) {
                        memberToEditInMain = target
                    } else {
                        showOnboardingDialog = true
                    }
                },
                onCalculateForActiveProfile = {
                    val target = activeMember
                    if (target != null) {
                        val (catId, subIdx, _) = BmiCalculatorEngine.getCategoryAndSubIndexFromBirth(target.birthTimestamp)
                        val catObj = Datos.categories.find { it.id == catId } ?: Datos.categories[4]
                        currentScreen = Screen.Calculator(
                            category = catObj,
                            preselectedSubIndex = subIdx,
                            preselectedGender = target.gender
                        )
                    } else {
                        showOnboardingDialog = true
                    }
                },
                onSelectCategoryManually = { category ->
                    currentScreen = Screen.Calculator(category)
                },
                onOpenHistory = {
                    currentScreen = Screen.History
                },
                onOpenFamily = {
                    currentScreen = Screen.FamilyProfiles
                },
                onOpenTools = {
                    currentScreen = Screen.HealthTools
                },
                onOpenTheme = {
                    showThemeDialog = true
                },
                onOpenInfo = {
                    showInfoDialog = true
                }
            )
        }
        is Screen.Calculator -> {
            CalculatorScreen(
                category = screen.category,
                preselectedSubIndex = screen.preselectedSubIndex,
                preselectedGender = screen.preselectedGender,
                onBack = { currentScreen = Screen.Home },
                onCalculateResult = { result, gender ->
                    currentScreen = Screen.Result(result, gender)
                }
            )
        }
        is Screen.Result -> {
            ResultScreen(
                result = screen.result,
                gender = screen.gender,
                onBack = { currentScreen = Screen.Home },
                onSaveToHistory = { res ->
                    val categoryObj = Datos.categories.find { it.id == res.categoryId }
                    val categoryName = if (categoryObj != null) context.getString(categoryObj.descriptionResId) else ""
                    historyManager.saveRecord(
                        BmiRecord(
                            categoryId = res.categoryId,
                            categoryName = categoryName,
                            weightKg = res.pesoKg,
                            heightCm = res.estaturaCm,
                            bmiValue = res.bmiValue,
                            statusText = res.statusText,
                            gender = screen.gender
                        )
                    )
                    val member = activeMember
                    if (member != null) {
                        familyManager.saveGrowthRecord(
                            GrowthRecord(
                                memberId = member.id,
                                weightKg = res.pesoKg,
                                heightCm = res.estaturaCm,
                                bmiValue = res.bmiValue,
                                statusText = res.statusText
                            )
                        )
                    }
                },
                onNewCalculation = {
                    currentScreen = Screen.Home
                }
            )
        }
        is Screen.History -> {
            HistoryScreen(
                onBack = { currentScreen = Screen.Home }
            )
        }
        is Screen.FamilyProfiles -> {
            FamilyProfilesScreen(
                onBack = { currentScreen = Screen.Home }
            )
        }
        is Screen.HealthTools -> {
            HealthToolsScreen(
                onBack = { currentScreen = Screen.Home }
            )
        }
    }
    if (showOnboardingDialog) {
        var nameInput by remember { mutableStateOf("Mi Perfil") }
        var genderInput by remember { mutableStateOf(Gender.MALE) }
        var birthYearInput by remember { mutableStateOf("1995") }
        val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
        val parsedYear = birthYearInput.trim().toIntOrNull()
        val isYearValid = parsedYear != null && parsedYear in 1900..currentYear
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = stringResource(id = R.string.bienvenido_a_imc_saludable),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(id = R.string.crea_tu_perfil_principal_desc),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text(stringResource(id = R.string.label_tu_nombre)) },
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
                        val mainProfile = FamilyMember(
                            name = nameInput.trim(),
                            relationship = "Perfil Principal",
                            gender = genderInput,
                            birthTimestamp = cal.timeInMillis
                        )
                        val newId = familyManager.saveMember(mainProfile)
                        familyManager.setActiveMemberId(newId)
                        allMembers = familyManager.getMembers()
                        activeMember = familyManager.getActiveMember()
                        showOnboardingDialog = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.crear_mi_perfil_y_comenzar), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
    if (showProfileSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showProfileSwitchDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.seleccionar_perfil_activo),
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            familyManager.setActiveMemberId(member.id)
                                            activeMember = member
                                            showProfileSwitchDialog = false
                                        }
                                ) {
                                    Icon(
                                        imageVector = if (member.gender == Gender.FEMALE) Icons.Default.Female else Icons.Default.Male,
                                        contentDescription = null,
                                        tint = if (member.gender == Gender.FEMALE) Color(0xFFE91E63) else Color(0xFF2196F3)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = member.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(text = member.relationship, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(end = 4.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            showProfileSwitchDialog = false
                                            memberToEditInMain = member
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = stringResource(id = R.string.editar_perfil),
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            showProfileSwitchDialog = false
                                            memberToDeleteInMain = member
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(id = R.string.eliminar_perfil),
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            showProfileSwitchDialog = false
                            showAddProfileDialog = true
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = stringResource(id = R.string.agregar_nuevo_familiar), fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProfileSwitchDialog = false }) {
                    Text(text = stringResource(id = R.string.entendido), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
    if (showAddProfileDialog) {
        var nameInput by remember { mutableStateOf("") }
        var relationshipInput by remember { mutableStateOf("Hijo/a") }
        var genderInput by remember { mutableStateOf(Gender.MALE) }
        var birthYearInput by remember { mutableStateOf("2020") }
        val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
        val parsedYear = birthYearInput.trim().toIntOrNull()
        val isYearValid = parsedYear != null && parsedYear in 1900..currentYear
        AlertDialog(
            onDismissRequest = { showAddProfileDialog = false },
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
                        val id = familyManager.saveMember(newMember)
                        familyManager.setActiveMemberId(id)
                        allMembers = familyManager.getMembers()
                        activeMember = familyManager.getActiveMember()
                        showAddProfileDialog = false
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.guardar), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showAddProfileDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.cancelar), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
    if (memberToEditInMain != null) {
        val target = memberToEditInMain!!
        val birthCal = Calendar.getInstance().apply { timeInMillis = target.birthTimestamp }
        var nameInput by remember(target.id) { mutableStateOf(target.name) }
        var relationshipInput by remember(target.id) { mutableStateOf(target.relationship) }
        var genderInput by remember(target.id) { mutableStateOf(target.gender) }
        var birthYearInput by remember(target.id) { mutableStateOf(birthCal.get(Calendar.YEAR).toString()) }
        val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
        val parsedYear = birthYearInput.trim().toIntOrNull()
        val isYearValid = parsedYear != null && parsedYear in 1900..currentYear
        AlertDialog(
            onDismissRequest = { memberToEditInMain = null },
            title = {
                Text(
                    text = stringResource(id = R.string.editar_perfil),
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                )
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
                        familyManager.saveMember(updated)
                        allMembers = familyManager.getMembers()
                        activeMember = familyManager.getActiveMember()
                        memberToEditInMain = null
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.guardar_cambios), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { memberToEditInMain = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.cancelar), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
    if (memberToDeleteInMain != null) {
        val target = memberToDeleteInMain!!
        AlertDialog(
            onDismissRequest = { memberToDeleteInMain = null },
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
                Text(
                    text = stringResource(id = R.string.eliminar_perfil),
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                )
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
                        familyManager.deleteMember(target.id)
                        allMembers = familyManager.getMembers()
                        activeMember = familyManager.getActiveMember()
                        memberToDeleteInMain = null
                        if (allMembers.isEmpty()) {
                            showOnboardingDialog = true
                        }
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
                    onClick = { memberToDeleteInMain = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.cancelar), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = stringResource(id = R.string.personalizacion_visual),
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.modo_iluminacion), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ThemeModeChip(title = "${stringResource(id = R.string.claro)} ☀️", isSelected = activeThemeMode == ThemeMode.LIGHT, onClick = { onThemeModeChange(ThemeMode.LIGHT) }, modifier = Modifier.weight(1f))
                        ThemeModeChip(title = "${stringResource(id = R.string.oscuro)} 🌙", isSelected = activeThemeMode == ThemeMode.DARK, onClick = { onThemeModeChange(ThemeMode.DARK) }, modifier = Modifier.weight(1f))
                        ThemeModeChip(title = "${stringResource(id = R.string.sistema)} 📱", isSelected = activeThemeMode == ThemeMode.SYSTEM, onClick = { onThemeModeChange(ThemeMode.SYSTEM) }, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = stringResource(id = R.string.paleta_colores), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    ThemePreset.values().forEach { preset ->
                        ThemePresetItem(
                            preset = preset,
                            isSelected = (activeThemePreset == preset),
                            onClick = {
                                onThemePresetChange(preset)
                                showThemeDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(text = stringResource(id = R.string.entendido), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = stringResource(id = R.string.acerca_del_imc),
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.acerca_del_imc_desc),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showInfoDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.entendido),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }
}
@Composable
fun ThemeModeChip(title: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
        modifier = modifier.clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
@Composable
fun ThemePresetItem(
    preset: ThemePreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val themeColor = Color(preset.primaryColorHex)
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) themeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = themeColor, modifier = Modifier.size(24.dp)) {}
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = preset.title,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (isSelected) {
                Text(
                    text = stringResource(id = R.string.activo_check),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = themeColor
                )
            }
        }
    }
}
@Composable
fun rememberHistoryManager(): BmiHistoryManager {
    val context = LocalContext.current
    return remember { BmiHistoryManager(context) }
}
