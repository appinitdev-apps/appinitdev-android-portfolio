
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    activeMember: FamilyMember?,
    onSwitchProfileClick: () -> Unit,
    onEditActiveProfileClick: () -> Unit,
    onCalculateForActiveProfile: () -> Unit,
    onSelectCategoryManually: (AgeCategory) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenFamily: () -> Unit,
    onOpenTools: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenInfo: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = stringResource(id = R.string.estandar_nutricional_oms),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenTheme) {
                        Icon(
                            imageVector = Icons.Default.Contrast,
                            contentDescription = "Cambiar Tema",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = onOpenTools) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = "Herramientas de Salud",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = onOpenFamily) {
                        Icon(
                            imageVector = Icons.Default.FamilyRestroom,
                            contentDescription = "Grupo Familiar",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = onOpenHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = stringResource(id = R.string.historial),
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = onOpenInfo) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(id = R.string.informacion),
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF00B894),
                            Color(0xFF00CEC9)
                        )
                    )
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
            // Active Profile Selector Banner Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF00B894),
                                    Color(0xFF0984E3)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.25f),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (activeMember?.gender == Gender.FEMALE) Icons.Default.Female else Icons.Default.Male,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "EVALUANDO AHORA PARA:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White.copy(alpha = 0.85f),
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = activeMember?.name ?: "Mi Perfil",
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Action buttons: Edit active profile and Switch Profile Pill
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.25f),
                                    modifier = Modifier.clickable { onEditActiveProfileClick() }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = stringResource(id = R.string.editar_perfil),
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.White.copy(alpha = 0.25f),
                                    modifier = Modifier.clickable { onSwitchProfileClick() }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.cambiar_perfil),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // Direct CTA to calculate for active profile
                        Button(
                            onClick = onCalculateForActiveProfile,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF0984E3)
                            )
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(
                                    id = R.string.calcular_imc_para,
                                    activeMember?.name ?: stringResource(id = R.string.mi_perfil)
                                ),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // Quick Access Modules Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onOpenFamily() },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FamilyRestroom,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(id = R.string.mi_familia),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = stringResource(id = R.string.ver_perfiles),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onOpenTools() },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(id = R.string.herramientas_medicas),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = stringResource(id = R.string.modulos_salud),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Text(
                text = stringResource(id = R.string.explora_manualmente_rango_edad),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 4.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(Datos.categories) { category ->
                    CategoryCard(
                        category = category,
                        onClick = { onSelectCategoryManually(category) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: AgeCategory,
    onClick: () -> Unit
) {
    val categoryBadgeColor = when (category.id) {
        1 -> Color(0xFFFF2D55)
        2 -> Color(0xFF00B0FF)
        3 -> Color(0xFF00E676)
        4 -> Color(0xFFD500F9)
        5 -> Color(0xFF00A88F)
        else -> Color(0xFFFFC400)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(205.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = category.drawableResId),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(96.dp)
                    .padding(2.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(id = category.titleResId),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = categoryBadgeColor.copy(alpha = 0.15f),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = stringResource(id = category.descriptionResId),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = categoryBadgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        maxLines = 1
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = categoryBadgeColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.calcular),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
