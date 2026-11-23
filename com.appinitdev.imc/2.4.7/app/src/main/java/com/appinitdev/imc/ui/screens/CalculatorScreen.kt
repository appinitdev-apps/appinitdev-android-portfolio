
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    category: AgeCategory,
    preselectedSubIndex: Int = 0,
    preselectedGender: Gender = Gender.MALE,
    onBack: () -> Unit,
    onCalculateResult: (BmiResult, Gender) -> Unit
) {
    val context = LocalContext.current
    var unitSystem by remember { mutableStateOf(UnitSystem.METRIC) }

    val subOptions = remember(category.id) {
        BmiCalculatorEngine.getOptionsForCategory(context, category.id)
    }

    var selectedSubOptionIndex by remember(category.id, preselectedSubIndex) {
        mutableIntStateOf(preselectedSubIndex.coerceIn(0, (subOptions?.size ?: 1) - 1))
    }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val familyManager = remember { FamilyProfileManager(context) }
    val activeMember = remember { familyManager.getActiveMember() }

    val lastGrowthRecord = remember(activeMember?.id) {
        if (activeMember != null) {
            familyManager.getGrowthRecordsForMember(activeMember.id).lastOrNull()
        } else null
    }

    val lastHistoryRecord = remember {
        if (lastGrowthRecord == null) {
            BmiHistoryManager(context).getRecords().lastOrNull()
        } else null
    }

    
    val defaultHeight = when (category.id) {
        1 -> 65.0
        2 -> 95.0
        3 -> 125.0
        4 -> 155.0
        else -> 170.0
    }

    val defaultWeight = when (category.id) {
        1 -> 7.5
        2 -> 14.0
        3 -> 25.0
        4 -> 48.0
        else -> 70.0
    }

    val initialHeight = lastGrowthRecord?.heightCm
        ?: lastHistoryRecord?.heightCm
        ?: defaultHeight

    val initialWeight = lastGrowthRecord?.weightKg
        ?: lastHistoryRecord?.weightKg
        ?: defaultWeight

    val initialGender = activeMember?.gender ?: preselectedGender

    var selectedGender by remember(category.id, activeMember?.id, preselectedGender) { mutableStateOf(initialGender) }
    var heightCm by remember(category.id, activeMember?.id) { mutableDoubleStateOf(initialHeight) }
    var weightKg by remember(category.id, activeMember?.id) { mutableDoubleStateOf(initialWeight) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = category.titleResId),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.volver),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                        modifier = Modifier
                            .clickable {
                                unitSystem = if (unitSystem == UnitSystem.METRIC) UnitSystem.IMPERIAL else UnitSystem.METRIC
                            }
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = if (unitSystem == UnitSystem.METRIC) "kg / cm" else "lbs / in",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            AdBanner()
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
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
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(id = category.drawableResId),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(46.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(id = category.titleResId),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(id = category.descriptionResId),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        
                        if (!subOptions.isNullOrEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isDropdownExpanded = true }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Edad: ${subOptions[selectedSubOptionIndex]}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = isDropdownExpanded,
                                    onDismissRequest = { isDropdownExpanded = false }
                                ) {
                                    subOptions.forEachIndexed { index, option ->
                                        DropdownMenuItem(
                                            text = { Text(text = option, fontWeight = FontWeight.Medium) },
                                            onClick = {
                                                selectedSubOptionIndex = index
                                                isDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val isFemale = (selectedGender == Gender.FEMALE)
                            val isMale = (selectedGender == Gender.MALE)

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isFemale) Color(0xFFE91E63) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isFemale) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedGender = Gender.FEMALE }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = "${stringResource(id = R.string.femenino)} 🌸",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isMale) Color(0xFF2196F3) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isMale) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedGender = Gender.MALE }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = "${stringResource(id = R.string.masculino)} 🔷",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                
                InteractiveRulerPicker(
                    title = stringResource(id = R.string.estatura),
                    value = if (unitSystem == UnitSystem.METRIC) heightCm else BmiCalculatorEngine.cmToInches(heightCm),
                    unitLabel = if (unitSystem == UnitSystem.METRIC) "cm" else "in",
                    range = if (unitSystem == UnitSystem.METRIC) 30.0..230.0 else 12.0..90.0,
                    step = 0.5,
                    onValueChange = { newVal ->
                        heightCm = if (unitSystem == UnitSystem.METRIC) newVal else BmiCalculatorEngine.inchesToCm(newVal)
                    },
                    accentColor = MaterialTheme.colorScheme.primary
                )

                
                InteractiveRulerPicker(
                    title = stringResource(id = R.string.peso),
                    value = if (unitSystem == UnitSystem.METRIC) weightKg else BmiCalculatorEngine.kgToLbs(weightKg),
                    unitLabel = if (unitSystem == UnitSystem.METRIC) "kg" else "lbs",
                    range = if (unitSystem == UnitSystem.METRIC) 2.0..250.0 else 4.0..550.0,
                    step = 0.5,
                    onValueChange = { newVal ->
                        weightKg = if (unitSystem == UnitSystem.METRIC) newVal else BmiCalculatorEngine.lbsToKg(newVal)
                    },
                    accentColor = Color(0xFF00CEC9)
                )

                
                Button(
                    onClick = {
                        val result = BmiCalculatorEngine.calculate(
                            context = context,
                            categoryId = category.id,
                            subOptionIndex = selectedSubOptionIndex,
                            gender = selectedGender,
                            weightKg = weightKg,
                            heightCm = heightCm,
                            unitSystem = unitSystem
                        )
                        if (activeMember != null) {
                            familyManager.saveGrowthRecord(
                                GrowthRecord(
                                    memberId = activeMember.id,
                                    weightKg = weightKg,
                                    heightCm = heightCm,
                                    bmiValue = result.bmiValue,
                                    statusText = result.statusText
                                )
                            )
                        }
                        onCalculateResult(result, selectedGender)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(id = R.string.calcular),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
