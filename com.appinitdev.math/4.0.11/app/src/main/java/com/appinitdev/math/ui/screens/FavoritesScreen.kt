@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: MathViewModel,
    onBack: () -> Unit
) {
    val favoriteTopics by viewModel.favoriteTopics.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.title_favorites), fontWeight = FontWeight.Bold) },
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
                    selected = false,
                    onClick = { viewModel.navigateTo(Screen.SEARCH) },
                    icon = { Text("🔍") },
                    label = { Text(stringResource(id = R.string.nav_search)) }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { viewModel.navigateTo(Screen.FAVORITES) },
                    icon = { Text("⭐") },
                    label = { Text(stringResource(id = R.string.nav_favorites)) }
                )
            }
        }
    ) { innerPadding ->
        if (favoriteTopics.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "⭐", style = MaterialTheme.typography.displayLarge)
                    Text(
                        text = stringResource(id = R.string.favorites_empty_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(id = R.string.favorites_empty_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(favoriteTopics) { topic ->
                    TopicCard(
                        topic = topic,
                        onClick = { viewModel.selectTopic(topic) },
                        onFavoriteClick = { viewModel.toggleFavorite(topic) }
                    )
                }
            }
        }
    }
}
