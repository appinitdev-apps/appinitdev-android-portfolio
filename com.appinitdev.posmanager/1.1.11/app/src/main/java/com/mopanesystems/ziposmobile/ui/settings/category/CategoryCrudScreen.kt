
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCrudScreen(onBack: (() -> Unit)? = null,viewModel: CategoryViewModel = hiltViewModel()) {
    val categories by viewModel.allCategories.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.category_settings)) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBackIosNew, contentDescription = stringResource(R.string.atr_s))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)) {

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.categories),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.manage_your_product_categories),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        onClick = {
                            editingCategory = null
                            showDialog = true
                        },
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(R.string.nueva_categor_a),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn {
                items(categories) { category ->
                    CategoryItem(
                        category = category,
                        onEdit = {
                            editingCategory = category
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteCategory(category) }
                    )
                }
            }
        }

        if (showDialog) {
            CategoryDialog(
                category = editingCategory,
                onDismiss = { showDialog = false },
                onSave = { name, description ->
                    if (editingCategory == null) {
                        
                        val newCategory = Category(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            description = description
                        )
                        viewModel.insertCategory(newCategory)
                    } else {
                        
                        val updated = editingCategory!!.copy(
                            name = name,
                            description = description,
                            updatedAt = java.time.LocalDateTime.now()
                        )
                        viewModel.updateCategory(updated)
                    }
                    showDialog = false
                }
            )
        }
    }
}


@Composable
fun CategoryItem(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(52.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = category.name.take(2).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(category.name, style = MaterialTheme.typography.titleMedium)
                category.description?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
            if (category.name != "All")
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.editar_categor_a))
                    }

                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.eliminarf))
                    }
                }
        }
    }
}


@Composable
fun CategoryDialog(
    category: Category?,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String?) -> Unit,
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var description by remember { mutableStateOf(category?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) stringResource(R.string.nueva_categor_a) else stringResource(
            R.string.editar_categor_a
        )) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.nombrec)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.descripci_nc)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, description.ifBlank { null })
                    }
                }
            ) { Text(stringResource(R.string.guardar_categoria)) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.cancelar)) }
        }
    )
}
