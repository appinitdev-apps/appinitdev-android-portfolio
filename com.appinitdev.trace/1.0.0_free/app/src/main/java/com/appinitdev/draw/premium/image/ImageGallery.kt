
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Reemplazado por FilterChip nativo de Material 3
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = category.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelLarge
            )
        }
    )
}

@Composable
fun ImageGallery(images: List<Image>, onImageClick: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), // Espaciado más moderno
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(images) { image ->
            AsyncImage(
                model = image.webformatURL,
                contentDescription = image.tags,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Cambiado a relación de aspecto cuadrada (más profesional que altura fija)
                    .clip(RoundedCornerShape(12.dp)) // Esquinas redondeadas en las imágenes estilo M3
                    .clickable { onImageClick(image.webformatURL) },
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun ShowGalleryDialog(
    images: List<Image>,
    categories: List<String>,
    selectedCategory: String,
    isLoading: Boolean,
    onCategorySelected: (String) -> Unit,
    onSearchQuery: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onNextPage: () -> Unit,
    onPrevPage: () -> Unit,
    onImageClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Permite mejor control del tamaño en pantallas chicas/grandes
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge, // Estilo M3 para diálogos de pantalla casi completa
            color = MaterialTheme.colorScheme.surface, // Usa el esquema de colores de la app, no blanco harcodeado
            tonalElevation = 6.dp, // Elevación tonal característica de Material 3
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)) { // Más padding para respirar mejor

                // --- BUSCADOR MANUAL ---
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.buscar_im_genes)) },
                    trailingIcon = {
                        IconButton(onClick = {
                            if (searchQuery.isNotBlank()) {
                                onSearchQuery(searchQuery)
                            }
                        }) {
                            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.buscar))
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    textStyle = MaterialTheme.typography.bodyLarge
                )

                // Selector de Categorías (M3 Chips)
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp), // Espaciado simétrico
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        CategoryChip(
                            category = category,
                            isSelected = category == selectedCategory
                        ) {
                            searchQuery = ""
                            onCategorySelected(category)
                        }
                    }
                }

                // Galería con Loading State
                Box(modifier = Modifier.weight(1f)) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary // Asegura el color del tema
                        )
                    } else if (images.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_se_encontraron_im_genes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        ImageGallery(images, onImageClick)
                    }
                }

                // Fila de botones para navegar (Paginación)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp) // Espaciado nativo entre botones
                ) {
                    OutlinedButton( // El botón de "atrás" suele ser secundario (Outlined)
                        onClick = onPrevPage,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.anterior)
                        )
                    }

                    ElevatedButton( // El botón de "siguiente" suele ser la acción principal (Elevated / Filled)
                        onClick = onNextPage,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = stringResource(R.string.siguiente)
                        )
                    }
                }
            }
        }
    }
}