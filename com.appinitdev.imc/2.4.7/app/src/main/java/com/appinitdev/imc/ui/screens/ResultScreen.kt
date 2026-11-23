
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    result: BmiResult,
    gender: Gender,
    onBack: () -> Unit,
    onSaveToHistory: (BmiResult) -> Unit,
    onNewCalculation: () -> Unit
) {
    val context = LocalContext.current
    var isSaved by remember { mutableStateOf(false) }

    val statusColor = when (result.bmiType) {
        1 -> UnderweightBlue
        2 -> NormalGreen
        3 -> OverweightYellow
        4 -> ObesityOrange
        5 -> ObesityRed
        else -> ObesityPurple
    }

    val headerShare = stringResource(id = R.string.mi_resultado_imc_share)
    val estadoShare = stringResource(id = R.string.estado_share)
    val valorShare = stringResource(id = R.string.valor_imc_share)
    val pesoShare = stringResource(id = R.string.peso_share)
    val estaturaShare = stringResource(id = R.string.estatura_share)
    val pesoIdealShare = stringResource(id = R.string.peso_ideal_share)
    val appShare = stringResource(id = R.string.calculado_con_app_share)
    val chooserTitle = stringResource(id = R.string.compartir_resultado_title)

    fun shareResult() {
        val shareText = """
            $headerShare
            - $estadoShare ${result.statusText}
            - $valorShare ${String.format("%.1f", result.bmiValue)} kg/m²
            - $pesoShare ${result.pesoFormatted}
            - $estaturaShare ${result.estaturaFormatted}
            - $pesoIdealShare ${String.format("%.1f kg", result.expectedWeightKg)}
            
            $appShare
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.resultado_del_calculo),
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
                    IconButton(onClick = { shareResult() }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(id = R.string.compartir),
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.2.dp,
                        color = statusColor.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(22.dp)
                    ),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.indice_de_masa_corporal),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    BmiGaugeView(
                        bmiValue = result.bmiValue,
                        isBabyCategory = (result.categoryId == 1)
                    )
                }
            }

            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        color = statusColor,
                        shape = RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = statusColor.copy(alpha = 0.18f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = statusColor,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (result.bmiType == 2) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = result.statusText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusColor
                        )

                        if (result.heightStatusText.isNotEmpty()) {
                            Text(
                                text = result.heightStatusText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = stringResource(id = R.string.peso1),
                    value = result.pesoFormatted,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = stringResource(id = R.string.estatura1),
                    value = result.estaturaFormatted,
                    modifier = Modifier.weight(1f)
                )
            }

            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.peso_esperado_promedio),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = String.format("%.1f kg", result.expectedWeightKg),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (result.minHealthyWeightKg > 0 && result.maxHealthyWeightKg > 0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${stringResource(id = R.string.rango_saludable_oms)} ${String.format("%.1f", result.minHealthyWeightKg)} kg - ${String.format("%.1f", result.maxHealthyWeightKg)} kg",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            
            val rec = result.healthRecommendation
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.recomendaciones_diarias_salud),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0288D1).copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (result.categoryId == 1) Icons.Default.ChildCare else Icons.Default.LocalDrink,
                                    contentDescription = null,
                                    tint = Color(0xFF0288D1),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = rec.hydrationTitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = rec.hydrationValue,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0288D1)
                            )
                            Text(
                                text = rec.hydrationNote,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFF5722).copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Color(0xFFFF5722),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = rec.energyTitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = rec.energyValue,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFF5722)
                            )
                            Text(
                                text = rec.energyNote,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            
            if (result.referenceTable.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.tabla_de_valores_oms),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        ReferenceTableContent(table = result.referenceTable, categoryId = result.categoryId)
                    }
                }
            }

            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (!isSaved) {
                            onSaveToHistory(result)
                            isSaved = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = !isSaved,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSaved) stringResource(id = R.string.guardado_en_historial) else stringResource(id = R.string.guardar_en_historial),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onNewCalculation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.nuevo_calculo),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ReferenceTableContent(table: DoubleArray, categoryId: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when (categoryId) {
            1, 2 -> { 
                if (table.size >= 4) {
                    TableRowItem(stringResource(id = R.string.desnutricion), "< ${table[0]}")
                    TableRowItem(stringResource(id = R.string.normal), "${table[1]} - ${table[2]}")
                    TableRowItem(stringResource(id = R.string.sobrepeso), "> ${table[2]}")
                    TableRowItem(stringResource(id = R.string.obesidad), "> ${table[3]}")
                }
            }
            3, 4 -> { 
                if (table.size >= 4) {
                    TableRowItem("${stringResource(id = R.string.desnutricion)} (IMC)", "<= ${table[0]}")
                    TableRowItem("${stringResource(id = R.string.normal)} (IMC)", "${table[0]} - ${table[2]}")
                    TableRowItem("${stringResource(id = R.string.sobrepeso)} (IMC)", "${table[2]} - ${table[3]}")
                    TableRowItem("${stringResource(id = R.string.obesidad)} (IMC)", ">= ${table[3]}")
                }
            }
            6 -> { 
                TableRowItem("${stringResource(id = R.string.bajo_peso_desnutricion)} (Geriatría)", "< 22.0")
                TableRowItem(stringResource(id = R.string.peso_normal), "22.0 - 26.9")
                TableRowItem(stringResource(id = R.string.sobrepeso_table), "27.0 - 29.9")
                TableRowItem(stringResource(id = R.string.obesidad_grado_i), "30.0 - 34.9")
                TableRowItem(stringResource(id = R.string.obesidad_grado_ii), "35.0 - 39.9")
                TableRowItem(stringResource(id = R.string.obesidad_grado_iii), ">= 40.0")
            }
            else -> { 
                TableRowItem(stringResource(id = R.string.bajo_peso_desnutricion), "< 18.5")
                TableRowItem(stringResource(id = R.string.peso_normal), "18.5 - 24.9")
                TableRowItem(stringResource(id = R.string.sobrepeso_table), "25.0 - 29.9")
                TableRowItem(stringResource(id = R.string.obesidad_grado_i), "30.0 - 34.9")
                TableRowItem(stringResource(id = R.string.obesidad_grado_ii), "35.0 - 39.9")
                TableRowItem(stringResource(id = R.string.obesidad_grado_iii), ">= 40.0")
            }
        }
    }
}

@Composable
fun TableRowItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
