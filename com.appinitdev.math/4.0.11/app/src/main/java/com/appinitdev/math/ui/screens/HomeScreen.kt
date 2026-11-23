
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MathViewModel,
    onNavigateToSearch: () -> Unit
) {
    val categories by viewModel.filteredCategories.collectAsState()
    val selectedDomain by viewModel.selectedDomain.collectAsState()
    val favoriteTopics by viewModel.favoriteTopics.collectAsState()
    val recentTopics by viewModel.recentTopics.collectAsState()
    val pinnedTopics by viewModel.pinnedTopics.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val currentTheme by viewModel.themeMode.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val interactionSource = remember { MutableInteractionSource() }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val allLanguages = remember {
        listOf(
            Triple("es", "🇪🇸", "Español"),
            Triple("en", "🇺🇸", "English"),
            Triple("de", "🇩🇪", "Deutsch"),
            Triple("fr", "🇫🇷", "Français"),
            Triple("pt", "🇵🇹", "Português"),
            Triple("zh", "🇨🇳", "中文"),
            Triple("ja", "🇯🇵", "日本語"),
            Triple("ko", "🇰🇷", "한국어"),
            Triple("hi", "🇮🇳", "हिन्दी"),
            Triple("id", "🇮🇩", "Bahasa Indonesia"),
            Triple("ru", "🇷🇺", "Русский"),
            Triple("nl", "🇳🇱", "Nederlands"),
            Triple("sv", "🇸🇪", "Svenska"),
            Triple("no", "🇳🇴", "Norsk"),
            Triple("da", "🇩🇰", "Dansk"),
            Triple("pl", "🇵🇱", "Polski"),
            Triple("th", "🇹🇭", "ไทย"),
            Triple("tr", "🇹🇷", "Türkçe"),
            Triple("uk", "🇺🇦", "Українська"),
            Triple("vi", "🇻🇳", "Tiếng Việt")
        )
    }

    
    if (showSettingsDialog) {
        var settingsTab by remember { mutableIntStateOf(0) }

        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Column {
                    Text(
                        text = "⚙️ " + stringResource(id = R.string.settings_title),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = R.string.settings_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = settingsTab == 0,
                            onClick = { settingsTab = 0 },
                            label = { Text("🌐 " + stringResource(id = R.string.settings_tab_language)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = settingsTab == 1,
                            onClick = { settingsTab = 1 },
                            label = { Text("🎨 " + stringResource(id = R.string.settings_tab_theme)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (settingsTab == 0) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(allLanguages) { (code, flag, name) ->
                                val isSelected = currentLang == code
                                Card(
                                    onClick = { viewModel.switchLanguage(code) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = flag, fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = name,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text("✓", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        
                        val themes = listOf(
                            Triple("SYSTEM", "📱 Sistema", Color(0xFF6200EE)),
                            Triple("DEFAULT", "🔵 Ocean Blue", Color(0xFF0284C7)),
                            Triple("EMERALD", "🟢 Emerald Forest", Color(0xFF059669)),
                            Triple("PURPLE", "🟣 Purple Velvet", Color(0xFF7C3AED)),
                            Triple("AMBER", "🟠 Golden Amber", Color(0xFFD97706)),
                            Triple("ROSE", "🔴 Rose Berry", Color(0xFFE11D48)),
                            Triple("CYAN", "🩵 Neon Cyan", Color(0xFF0891B2)),
                            Triple("TEAL", "🌲 Deep Teal", Color(0xFF0D9488)),
                            Triple("INDIGO", "🌌 Midnight Indigo", Color(0xFF4F46E5)),
                            Triple("OLED", "🖤 Pitch Black OLED", Color(0xFF000000)),
                            Triple("DARK", "🌙 Dark Slate", Color(0xFF1E293B)),
                            Triple("LIGHT", "☀️ Clean Light", Color(0xFFF8FAFC))
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(themes) { (modeKey, name, colorPreview) ->
                                val isSelected = currentTheme == modeKey
                                Card(
                                    onClick = { viewModel.changeThemeMode(modeKey) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(colorPreview)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = name,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text("✓", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSettingsDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(id = R.string.btn_close), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { viewModel.navigateTo(Screen.HOME) },
                    icon = { Text("🏠") },
                    label = { Text(stringResource(id = R.string.nav_home)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(Screen.SEARCH) },
                    icon = { Text("🔍") },
                    label = { Text(stringResource(id = R.string.nav_search)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(Screen.FAVORITES) },
                    icon = { Text("⭐") },
                    label = { Text(stringResource(id = R.string.nav_favorites)) }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.title_app),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(id = R.string.subtitle_app),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isPremium) {
                            Button(
                                onClick = {
                                    if (activity != null) viewModel.launchBillingFlow(activity) else viewModel.setPremium(true)
                                },
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.btn_get_premium),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Text(
                                    text = stringResource(id = R.string.premium_active_badge),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Text(text = "⚙️", fontSize = 20.sp)
                        }
                    }
                }
            }

            
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSearch() }
                ) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        placeholder = {
                            Text(stringResource(id = R.string.placeholder_search))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        interactionSource = interactionSource
                    )
                }
            }

            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    
                    Card(
                        onClick = { viewModel.navigateTo(Screen.FLASHCARDS) },
                        modifier = Modifier
                            .weight(1f)
                            .height(105.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2563EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_flashcards),
                                    contentDescription = stringResource(id = R.string.nav_flashcards),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(id = R.string.nav_flashcards),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    
                    Card(
                        onClick = { viewModel.navigateTo(Screen.GLOSSARY) },
                        modifier = Modifier
                            .weight(1f)
                            .height(105.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF059669)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_glossary),
                                    contentDescription = stringResource(id = R.string.nav_glossary),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(id = R.string.nav_glossary),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    
                    Card(
                        onClick = { viewModel.navigateTo(Screen.UNIT_CONVERTER) },
                        modifier = Modifier
                            .weight(1f)
                            .height(105.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF7C3AED)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_converter),
                                    contentDescription = stringResource(id = R.string.nav_converter),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(id = R.string.nav_converter),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            
            if (pinnedTopics.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = stringResource(id = R.string.section_pinned),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(pinnedTopics) { topic ->
                                PinnedFormulaCard(
                                    topic = topic,
                                    onClick = { viewModel.selectTopic(topic) },
                                    onUnpinClick = { viewModel.togglePinnedTopic(topic) }
                                )
                            }
                        }
                    }
                }
            }

            
            if (recentTopics.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = stringResource(id = R.string.section_recents),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(recentTopics) { topic ->
                                FilterChip(
                                    selected = false,
                                    onClick = { viewModel.selectTopic(topic) },
                                    label = { Text("📄 " + topic.title) },
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            
            if (favoriteTopics.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = stringResource(id = R.string.section_favorites),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(favoriteTopics) { topic ->
                                FilterChip(
                                    selected = true,
                                    onClick = { viewModel.selectTopic(topic) },
                                    label = { Text(topic.title) },
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            
            item {
                Column {
                    Text(
                        text = stringResource(id = R.string.section_areas),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val domains = listOf(
                        MathDomain.ALL to stringResource(id = R.string.tab_all),
                        MathDomain.ALGEBRA to "Álgebra",
                        MathDomain.TRIGONOMETRY to "Trigonometría",
                        MathDomain.CALCULUS to "Cálculo",
                        MathDomain.GEOMETRY to "Geometría",
                        MathDomain.LOGIC_STATS to "Estadística y Lógica"
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(domains) { (domain, label) ->
                            FilterChip(
                                selected = selectedDomain == domain,
                                onClick = { viewModel.selectDomain(domain) },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
            }

            
            items(categories) { category ->
                CategoryCard(
                    category = category,
                    onClick = {
                        viewModel.selectCategory(category)
                    }
                )
            }
        }
    }
}

@Composable
fun PinnedFormulaCard(
    topic: MathTopic,
    onClick: () -> Unit,
    onUnpinClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(220.dp)
            .height(95.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Column(modifier = Modifier.align(Alignment.TopStart).padding(end = 24.dp)) {
                Text(
                    text = topic.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = topic.categoryId.replace("-", " ").uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = onUnpinClick,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopEnd)
            ) {
                Text(text = "❌", fontSize = 12.sp)
            }
        }
    }
}
