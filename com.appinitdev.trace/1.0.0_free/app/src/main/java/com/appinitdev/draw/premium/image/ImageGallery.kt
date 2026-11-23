@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
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
        verticalArrangement = Arrangement.spacedBy(12.dp), 
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(images) { image ->
            AsyncImage(
                model = image.webformatURL,
                contentDescription = image.tags,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) 
                    .clip(RoundedCornerShape(12.dp)) 
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
        properties = DialogProperties(usePlatformDefaultWidth = false) 
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge, 
            color = MaterialTheme.colorScheme.surface, 
            tonalElevation = 6.dp, 
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)) { 
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
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp), 
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
                Box(modifier = Modifier.weight(1f)) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary 
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp) 
                ) {
                    OutlinedButton( 
                        onClick = onPrevPage,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.anterior)
                        )
                    }
                    ElevatedButton( 
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