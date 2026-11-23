
@Composable
fun UserProfileCard(profile: UserProfile, viewModel: AuthViewModel, onEditClick: () -> Unit, onReputationClick: () -> Unit, navController: NavController) {
    var isExpanded by remember { mutableStateOf(false) }
    var showAchievementsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("tester_prefs", Context.MODE_PRIVATE) }
    var showTutorial by rememberSaveable {
        mutableStateOf(!prefs.getBoolean("tutorial_visto", false))
    }
    
    if (showTutorial) {
        TesterAppTutorialDialog(
            onDismiss = {
                prefs.edit().putBoolean("tutorial_visto", true).apply()
                
                showTutorial = false
            },
            onSkip = {
                prefs.edit().putBoolean("tutorial_visto", true).apply()
                
                showTutorial = false
            }
        )
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = profile.devName.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile.devName.ifEmpty { "Desarrollador" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(8.dp))
                        com.appinitdev.tester.screen.StreakBadge(profile.streaks)
                        
                        if (profile.achievements.isNotEmpty()) {
                            Spacer(Modifier.width(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.clickable { showAchievementsDialog = true }
                            ) {
                                profile.achievements.take(3).forEach { achievementId ->
                                    AchievementBadge(achievementId)
                                }
                            }
                        }
                        
                        Spacer(Modifier.weight(1f))
                        
                        IconButton(onClick = { navController.navigate("points_history") }) {
                            Icon(Icons.Default.History, "Historial de Puntos", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { navController.navigate("verification_screen") }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.TrendingUp, null, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "LVL ${profile.nivel.toInt()}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { onReputationClick() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Star, null, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "REP ${String.format("%.2f", profile.reputacion)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }



            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val notifications by viewModel.notifications.collectAsState()
                    val unreadCount = notifications.count { !it.isRead }
                    
                    BadgedBox(
                        badge = { if (unreadCount > 0) Badge { Text("$unreadCount") } }
                    ) {
                        FilledTonalIconButton(
                            onClick = { navController.navigate("notifications") },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Notifications, stringResource(R.string.notificaciones), modifier = Modifier.size(20.dp))
                        }
                    }

                    FilledTonalIconButton(
                        onClick = { showTutorial = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.AutoStories, "Tutorial", modifier = Modifier.size(20.dp))
                    }

                    FilledTonalIconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Edit, "Editar", modifier = Modifier.size(20.dp))
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        NotificationPermissionButton()
                    }

                    if (com.appinitdev.tester.BuildConfig.DEBUG) {
                        FilledTonalIconButton(
                            onClick = { viewModel.notifyNewAppToRandomTesters(context) },
                            modifier = Modifier.size(40.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Campaign, "Debug", modifier = Modifier.size(20.dp))
                        }
                    } else {
                        LanguageSettingsButton()
                    }
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expandir",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {


                Column {
                    Column{
                        Spacer(modifier = Modifier.height(24.dp))

                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RatingBarDisplay(profile.promedioEstrellas, profile.calificaciones.size)

                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Stars,
                                            null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = profile.puntos.toString(),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = stringResource(R.string.puntos).uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                InfoSection(
                    label = stringResource(R.string.codigo_referido),
                    value = profile.referralCode.ifEmpty { stringResource(R.string.generar_codigo) },
                    icon = Icons.Default.Share
                )
                if (profile.referralCode.isEmpty()) {
                    TextButton(onClick = { viewModel.generateReferralCode() }) {
                        Text(stringResource(R.string.generar_mi_codigo))
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                    
                    Text(
                        stringResource(R.string.contacto_y_enlaces),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))



                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = profile.contacto.ifBlank { stringResource(R.string.no_disponible) },
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = profile.web.ifBlank { stringResource(R.string.no_disponible) },
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        
                        horizontalArrangement = Arrangement.End,
                        
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary 
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = profile.uid.ifBlank { stringResource(R.string.no_disponible) }.take(25),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium 
                        )
                    }
                }
            }

        }
    }

    if (showAchievementsDialog) {
        AchievementsDetailsDialog(
            achievementIds = profile.achievements,
            onDismiss = { showAchievementsDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsDetailsDialog(achievementIds: List<String>, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Text(
                text = stringResource(R.string.tus_logros_titulo),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            achievementIds.forEach { id ->
                val (title, desc, icon, color) = when (id) {
                    "tester_pioneer" -> arrayOf(stringResource(R.string.pionero_titulo), stringResource(R.string.pionero_desc), Icons.Default.MilitaryTech, Color(0xFFFFD700))
                    "quality_guardian" -> arrayOf(stringResource(R.string.guardian_titulo), stringResource(R.string.guardian_desc), Icons.Default.Shield, Color(0xFF4CAF50))
                    "streak_fire" -> arrayOf(stringResource(R.string.fuego_titulo), stringResource(R.string.fuego_desc), Icons.Default.Whatshot, Color(0xFFFF5722))
                    "top_reviewer" -> arrayOf(stringResource(R.string.reviewer_titulo), stringResource(R.string.reviewer_desc), Icons.Default.RateReview, Color(0xFF2196F3))
                    else -> arrayOf(stringResource(R.string.logro_comunidad_titulo), stringResource(R.string.logro_comunidad_desc), Icons.Default.EmojiEvents, Color.Gray)
                }

                ListItem(
                    leadingContent = {
                        Surface(
                            color = (color as Color).copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = icon as ImageVector,
                                contentDescription = null,
                                tint = color as Color,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    },
                    headlineContent = { Text(title as String, fontWeight = FontWeight.Bold) },
                    supportingContent = { Text(desc as String) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementBadge(id: String) {
    val (icon, color, label) = when (id) {
        "tester_pioneer" -> Triple(Icons.Default.MilitaryTech, Color(0xFFFFD700), "Pionero")
        "quality_guardian" -> Triple(Icons.Default.Shield, Color(0xFF4CAF50), "Guardián")
        "streak_fire" -> Triple(Icons.Default.Whatshot, Color(0xFFFF5722), "Fuego")
        "top_reviewer" -> Triple(Icons.Default.RateReview, Color(0xFF2196F3), "Reviewer")
        else -> Triple(Icons.Default.EmojiEvents, Color.Gray, "Logro")
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = CircleShape,
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = Modifier.size(28.dp),
        tonalElevation = 2.dp
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.padding(5.dp)
        )
    }
}

@Composable
fun RatingBarDisplay(rating: Float, votes: Int) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(5) { index ->
                val starColor = if (index < rating.toInt()) Color(0xFFFFB300) else Color.LightGray
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = starColor
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = String.format("%.1f", rating),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Text(
            text = "$votes "+ stringResource(R.string.calificaciones),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun InfoSection(label: String, value: String, icon: ImageVector) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(
                text = value.ifEmpty { stringResource(R.string.sin_informaci_n) },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ContactChip(icon: ImageVector, label: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}
