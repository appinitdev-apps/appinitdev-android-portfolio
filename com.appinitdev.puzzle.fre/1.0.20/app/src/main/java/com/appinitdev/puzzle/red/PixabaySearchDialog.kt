@Composable
fun PixabaySearchDialog(
    onDismiss: () -> Unit,
    onImageSelected: (Bitmap) -> Unit
) {
    var query by remember { mutableStateOf("woman") }
    val photos = remember { mutableStateListOf<PixabayPhoto>() }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val recommendations = listOf(
        "Woman 👧",
        "Cosplay 🎭",
        "Anime 🌸",
        "Naturaleza 🌿",
        "Autos 🏎️",
        "Gatos 🐱",
        "Fantasía ✨",
        "Paisajes 🏔️"
    )
    val performSearch: (String) -> Unit = { searchQuery ->
        scope.launch {
            if (searchQuery.isBlank()) return@launch
            isLoading = true
            errorMessage = null
            try {
                val cleanQuery = searchQuery.replace(Regex("[^a-zA-Z0-9\\s]"), "").trim().ifEmpty { searchQuery.trim() }
                Log.d("PixabaySearch", "Sending query: '$cleanQuery'")
                val response = RetrofitClient.service.searchPhotos(
                    apiKey = PixabayConfig.API_KEY,
                    query = cleanQuery,
                    imageType = "photo",
                    perPage = 30
                )
                photos.clear()
                photos.addAll(response.hits)
                Log.d("PixabaySearch", "Hits received: ${response.hits.size}")
                if (response.hits.isEmpty()) {
                    errorMessage = "No se encontraron imágenes."
                }
            } catch (e: Exception) {
                Log.e("PixabaySearch", "Error searching Pixabay: ${e.javaClass.simpleName} - ${e.message}", e)
                errorMessage = "Error de conexión con Pixabay."
            } finally {
                isLoading = false
            }
        }
    }
    LaunchedEffect(Unit) {
        performSearch(query)
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ImageSearch,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Buscar en Pixabay",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Ej: Cosplay, Woman, Anime...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = {
                        IconButton(onClick = { performSearch(query) }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Recomendaciones de Búsqueda:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(recommendations) { tag ->
                        SuggestionChip(
                            onClick = {
                                query = tag
                                performSearch(tag)
                            },
                            label = { Text(tag, style = MaterialTheme.typography.bodySmall) },
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Cargando Pixabay...", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(photos, key = { it.id }) { photo ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clickable {
                                            scope.launch {
                                                isLoading = true
                                                val imageUrl = photo.largeImageURL.ifEmpty { photo.webformatURL }
                                                val bitmap = downloadUnsplashImage(context, imageUrl)
                                                bitmap?.let { onImageSelected(it) }
                                                isLoading = false
                                                onDismiss()
                                            }
                                        }
                                ) {
                                    AsyncImage(
                                        model = photo.webformatURL.ifEmpty { photo.previewURL },
                                        contentDescription = photo.tags,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    shape = CircleShape,
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text(stringResource(R.string.cerrar), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
