
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedPOSScreenCompose(
    viewModel: POSViewModel,
    onScanBarcode: () -> Unit,
    onProcessPayment: (PaymentMethod) -> Unit,
    onClearCart: () -> Unit,
    currencyFormatter: NumberFormat
) {
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val subtotal by viewModel.subtotal.collectAsStateWithLifecycle()
    val taxAmount by viewModel.taxAmount.collectAsStateWithLifecycle()
    val totalAmount by viewModel.totalAmount.collectAsStateWithLifecycle()
    val itemCount by viewModel.itemCount.collectAsStateWithLifecycle()
    val selectedCustomer by viewModel.selectedCustomer.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    
    
    var showCart by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (showCart) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    LaunchedEffect(cartItems.isNotEmpty()) {
        showCart = cartItems.isNotEmpty()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sale",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                
                val scanButtonScale by animateFloatAsState(
                    targetValue = if (isProcessing) 0.9f else 1f,
                    animationSpec = tween(200)
                )
                
                IconButton(
                    onClick = onScanBarcode,
                    modifier = Modifier.scale(scanButtonScale)
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner, 
                        contentDescription = "Scan Barcode",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        
        AnimatedVisibility(
            visible = selectedCustomer != null,
            enter = slideInHorizontally() + fadeIn(),
            exit = slideOutHorizontally() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Customer:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedCustomer?.fullName ?: "No Customer Selected",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(onClick = {   }) {
                        Text("Add Customer")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .scale(scale)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cart Items (${itemCount})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    
                    AnimatedVisibility(
                        visible = itemCount > 0,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = itemCount.toString(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (cartItems.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No items in cart",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn {
                        items(
                            items = cartItems,
                            key = { it.id }
                        ) { item ->
                            AnimatedVisibility(
                                visible = true,
                                enter = slideInVertically() + fadeIn(),
                                exit = slideOutVertically() + fadeOut()
                            ) {
                                CartItemRowCompose(
                                    item = item,
                                    onQuantityChange = { newQuantity ->
                                        
                                    },
                                    onRemove = {
                                        viewModel.removeProductFromCart(item.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        
        AnimatedVisibility(
            visible = cartItems.isNotEmpty(),
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    
                    AnimatedContent(
                        targetState = totalAmount,
                        transitionSpec = {
                            slideInVertically() + fadeIn() togetherWith
                            slideOutVertically() + fadeOut()
                        }
                    ) { total ->
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Subtotal:",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = currencyFormatter.format(subtotal),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tax:",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = currencyFormatter.format(taxAmount),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total:",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = currencyFormatter.format(total),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val paymentMethods = listOf(
                            PaymentMethod.CASH to "Cash",
                            PaymentMethod.CARD to "Card",
                            PaymentMethod.MOBILE_PAYMENT to "Mobile"
                        )
                        
                        paymentMethods.forEach { (method, label) ->
                            val buttonScale by animateFloatAsState(
                                targetValue = if (isProcessing) 0.95f else 1f,
                                animationSpec = tween(100)
                            )
                            
                            Button(
                                onClick = { onProcessPayment(method) },
                                modifier = Modifier
                                    .weight(1f)
                                    .scale(buttonScale),
                                enabled = cartItems.isNotEmpty() && !isProcessing
                            ) {
                                Text(label)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = onClearCart,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Cart")
                    }
                    
                    
                    AnimatedVisibility(
                        visible = isProcessing,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
