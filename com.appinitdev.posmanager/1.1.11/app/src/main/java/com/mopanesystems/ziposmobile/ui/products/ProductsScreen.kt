
/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: ProductViewModel = hiltViewModel(),
) {
    ProductsScreenCompose(viewModel = viewModel)
}
*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreenCompose(
    viewModel: ProductViewModel,
    viewModelPOS: POSViewModel,

    ) {


    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.observeAsState()
    var showProductDialogCreate by remember { mutableStateOf(false) }
    var showProductDialogUpdate by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categoriesAll by viewModel.allCategories.collectAsState(initial = emptyList())
    var productEdit by remember { mutableStateOf(false) }
    var selectedProductId by remember { mutableStateOf(0) }
    val categories = categoriesAll.map { it.name }
    val mode by viewModel.selectedMode.collectAsState()
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Modern Header Section
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
                Column (modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.product_catalog),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.manage_your_inventory_and_products),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }


                Surface(
                    onClick = {
                        productEdit = !productEdit
                    },
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (!productEdit) Icons.Default.Edit else Icons.Default.Check,
                            contentDescription = "Edit Product",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(5.dp))
                if(!productEdit)
                    Surface(
                        onClick = {
                            showProductDialogCreate = true
                            productToEdit = null
                        },
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Product",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                }

            }
        }

        // Modern Search and Filter Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Enhanced Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.searchProducts(it)
                    },
                    label = { Text(stringResource(R.string.search_products)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                viewModel.clearFilters()
                            }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                // Modern Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            onClick = {
                                if (category.equals("All", ignoreCase = true)) {
                                    viewModel.clearFilters() // Mostrar todos los productos
                                } else {
                                    viewModel.filterByCategory(category) // Filtrar por categoría seleccionada
                                }
                                selectedCategory = category
                            },
                            label = {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            selected = selectedCategory == category,
                            leadingIcon = if (selectedCategory == category) {
                                {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        if (showProductDialogCreate) {
            ProductFormDialog(
                showDialog = showProductDialogCreate,
                onDismiss = { showProductDialogCreate = false },
                onSave = { product ->
                    viewModel.addProduct(product)
                    // Aquí guardas el producto, por ejemplo en un ViewModel o lista
                    Log.d("Products", "Producto creado: $product")
                    showProductDialogCreate = false
                },
                initialProduct = productToEdit, // null porque es creación
                categories = categories
            )
        }

        if (showProductDialogUpdate && productToEdit != null) {
            ProductFormDialog(
                showDialog = showProductDialogUpdate,
                onDismiss = { showProductDialogUpdate = false },
                onSave = { product ->
                    viewModel.updateProduct(product)
                    // Aquí guardas el producto, por ejemplo en un ViewModel o lista
                    Log.d("Products", "Producto actualizado: $product")
                    showProductDialogUpdate = false
                },
                initialProduct = productToEdit, // null porque es creación
                categories = categories
            )
        }


        // Modern Products List
        if (isLoading) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = stringResource(R.string.loading_products),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else if (filteredProducts.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Inventory,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.no_products_found),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty()) stringResource(R.string.try_adjusting_search) else stringResource(R.string.add_first_product),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredProducts) { product ->
                    ModernProductCard(
                        product = product,
                        onEdit = {
                            showProductDialogUpdate = true
                            productToEdit = product
                        },
                        edit = productEdit,
                        onDelete = {
                            productToDelete = product
                            //viewModel.deleteProduct(product)
                                   },
                        onAddToCart = { viewModelPOS.addProductToCart(product) }
                    )
                }
            }
        }

        // Error message
        errorMessage?.let { message ->
            LaunchedEffect(message) {
                // Show error message
            }
        }

        if (productToDelete != null) {
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                title = {
                    Text(
                        text = stringResource(R.string.confirmar_eliminaci_n),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Text(
                        text = "${productToDelete?.name}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            productToDelete?.let { viewModel.deleteProduct(it) }
                            productToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            stringResource(R.string.eliminar),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { productToDelete = null },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            stringResource(R.string.cancelar),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                },
                shape = MaterialTheme.shapes.medium,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 6.dp
            )

        }

    }
}

// Modern Product Card Component
@Composable
fun ModernProductCard(
    product: com.mopanesystems.ziposmobile.data.model.Product,
    onEdit: () -> Unit,
    edit: Boolean,
    onDelete: () -> Unit,
    onAddToCart: () -> Unit,
) {


    var isAdded by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isAdded) 1.1f else 1f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )

    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product avatar/icon
            Surface(
                modifier = Modifier.size(64.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = product.name.take(2).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Product information
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = product.categoryId ?: stringResource(R.string.uncategorized),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${product.price}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    // Stock indicator
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (product.stockQuantity < product.minStockLevel) {
                            MaterialTheme.colorScheme.errorContainer
                        } else if (product.stockQuantity < (product.maxStockLevel
                                ?: (product.minStockLevel + 50))
                        ) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    ) {
                        Text(
                            text = "Stock: ${product.stockQuantity}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (product.stockQuantity < product.minStockLevel) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else if (product.stockQuantity < (product.maxStockLevel
                                    ?: (product.minStockLevel + 50))
                            ) {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Modern action buttons with proper scaling

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Primary action - Add to cart
                if (!edit)
                    Button(
                        onClick = {
                            isAdded = true           // <-- activa el rebote
                            onAddToCart()
                            coroutineScope.launch {
                                delay(300)
                                isAdded = false      // <-- vuelve a la normalidad
                            }
                                  },
                        enabled = product.stockQuantity > 0,
                        modifier = Modifier
                            .height(44.dp)
                            .widthIn(min = 120.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.AddShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.add),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                // Secondary actions
                if (edit)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Edit button
                        FilledTonalButton(

                            onClick = onEdit,
                            modifier = Modifier.size(42.dp),
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit product",
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }

                        // Delete button
                        FilledTonalButton(
                            onClick = onDelete,
                            modifier = Modifier.size(42.dp),
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Delete product",
                                modifier = Modifier.size(22.dp),

                               tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

            }
        }
    }
}


/*
@Composable
fun ProductCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$${String.format("%.2f", product.price)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Stock: ${product.stock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (product.stock < 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
fun ProductCardCompose(
    product: com.mopanesystems.ziposmobile.data.model.Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = product.categoryId ?: "No Category",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$${String.format("%.2f", product.price)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Stock: ${product.stockQuantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (product.stockQuantity < 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}
*/
/*
data class Product(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val stock: Int,
)*/