@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: MathViewModel,
    onBack: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.advancedSearchResults.collectAsState()
    val filterExamplesOnly by viewModel.searchFilterOnlyExamples.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.title_search), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text(text = "←", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(Screen.HOME) },
                    icon = { Text("🏠") },
                    label = { Text(stringResource(id = R.string.nav_home)) }
                )
                NavigationBarItem(
                    selected = true,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text(stringResource(id = R.string.placeholder_search_input)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Text("✕")
                        }
                    }
                }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = filterExamplesOnly,
                    onClick = { viewModel.toggleSearchExamplesFilter() },
                    label = { Text(stringResource(id = R.string.filter_only_examples)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            }
            if (searchQuery.isBlank()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.placeholder_search_input),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.search_no_results),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "${searchResults.size} " + stringResource(id = R.string.search_results_found),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(searchResults) { match ->
                        Column {
                            if (match.matchedSubtopic != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                ) {
                                    Text(
                                        text = "📍 Coincidencia en: ${match.matchedSubtopic}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            TopicCard(
                                topic = match.topic,
                                onClick = { viewModel.selectTopic(match.topic) },
                                onFavoriteClick = { viewModel.toggleFavorite(match.topic) }
                            )
                        }
                    }
                }
            }
        }
    }
}
