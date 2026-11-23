
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    viewModel: MathViewModel,
    onBack: () -> Unit
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val topics by viewModel.categoryTopics.collectAsState()

    val category = selectedCategory ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = category.title,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text(text = "←", style = MaterialTheme.typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category info header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = category.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "📐 ${topics.size} temas y fórmulas disponibles",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Contenido de la Sección",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Topics list
            items(topics) { topic ->
                TopicCard(
                    topic = topic,
                    onClick = { viewModel.selectTopic(topic) },
                    onFavoriteClick = { viewModel.toggleFavorite(topic) }
                )
            }
        }
    }
}
