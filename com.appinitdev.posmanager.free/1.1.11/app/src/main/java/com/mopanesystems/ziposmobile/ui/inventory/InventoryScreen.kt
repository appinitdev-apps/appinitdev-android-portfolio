
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = hiltViewModel()
) {
    InventoryScreenCompose(viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreenCompose(
    viewModel: InventoryViewModel
) {
    val stockAlerts by viewModel.stockAlerts.observeAsState(emptyList())
    val recentAdjustments by viewModel.recentAdjustments.observeAsState(emptyList())
    val lowStockCount by viewModel.lowStockCount.observeAsState(0)
    val productCount by viewModel.productCount.observeAsState(0)
    val totalInventoryValue by viewModel.totalInventoryValue.collectAsState()
    val outOfStockCount by viewModel.outOfStockCount.observeAsState(0)
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()
    val filteredProducts by viewModel.lowStockProducts.observeAsState(emptyList())
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showProductDialogUpdate by remember { mutableStateOf(false) }
    var productEdit by remember { mutableStateOf(false) }
    val categoriesAll by viewModel.allCategories.collectAsState(initial = emptyList())
    val categories = categoriesAll.map { it.name }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Header

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
                        text = stringResource(R.string.inventory_management),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.manage_your_inventory),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.inventory),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricCard(
                        title = stringResource(R.string.products),
                        value = productCount.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = stringResource(R.string.inventory_value),
                        value = formatCurrency(totalInventoryValue),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricCard(
                        title = stringResource(R.string.low_stock),
                        value = lowStockCount.toString(),
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = stringResource(R.string.out_of_stock),
                        value = outOfStockCount.toString(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        //

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.low_stock_alerts),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

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

                )
            }
        }

        // Action Buttons
        /* Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { /* Stock Adjustment */ },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stock Adjustment")
            }
            
            Button(
                onClick = { /* Purchase Order */ },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Purchase Order")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))*/

        // Stock Alerts
        /*   Text(
            text = "Stock Alerts",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(stockAlerts) { alert ->
                StockAlertCardCompose(
                    alert = alert,
                    onAcknowledge = { viewModel.acknowledgeAlert(alert) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Recent Adjustments
        Text(
            text = "Recent Adjustments",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recentAdjustments) { adjustment ->
                AdjustmentCardCompose(adjustment = adjustment)
            }
        }
        */
        // Error message
        errorMessage?.let { message ->
            LaunchedEffect(message) {
                // Show error message
            }

    }
        
        /*if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }*/

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
    }
}

@Composable
fun StatCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun StockAlertCard(
    alert: StockAlert,
    onAcknowledge: () -> Unit
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
                    text = alert.productId, // TODO: Fetch product name from Product entity
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Current: ${alert.currentStock} | Threshold: ${alert.threshold}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = alert.alertType.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Button(
                onClick = onAcknowledge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Acknowledge")
            }
        }
    }
}

@Composable
fun AdjustmentCard(adjustment: InventoryAdjustment) {
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
                    text = adjustment.productId, // TODO: Fetch product name from Product entity
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Quantity: ${adjustment.quantity} | Reason: ${adjustment.reason}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "By: ${adjustment.userId} | ${adjustment.createdAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// StockAlert data class is defined in data.model package

@Composable
fun StockAlertCardCompose(
    alert: com.mopanesystems.ziposmobile.data.model.StockAlert,
    onAcknowledge: () -> Unit
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
                    text = alert.productId, // TODO: Fetch product name from Product entity
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Current: ${alert.currentStock} | Threshold: ${alert.threshold}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = alert.alertType.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Button(
                onClick = onAcknowledge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Acknowledge")
            }
        }
    }
}



@Composable
fun ModernProductCard(
    product: com.mopanesystems.ziposmobile.data.model.Product,
    onEdit: () -> Unit,
) {


    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    text = product.categoryId ?: "Uncategorized",
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

                }
            }
        }
    }

}

// InventoryAdjustment data class is defined in data.model package
