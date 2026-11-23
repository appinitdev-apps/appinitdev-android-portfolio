
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSelectionDialog(
    showDialog: Boolean,
    viewModel: CustomerViewModel,
    onDismiss: () -> Unit,
    onSelect: (Customer?) -> Unit,
) {
    if (!showDialog) return

    
    val customers by viewModel.filteredCustomers.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    
    val filteredCustomers = remember(searchQuery, customers) {
        customers.filter {
            it.fullName.contains(searchQuery, ignoreCase = true) ||
                    (it.email?.contains(searchQuery, ignoreCase = true) ?: false) ||
                    (it.phone?.contains(searchQuery, ignoreCase = true) ?: false)
        }
    }

    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(
            

            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()        
                .wrapContentHeight()   
                .imePadding()       
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.select_customer),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(R.string.search)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(filteredCustomers) { customer ->

                        CustomerCardCompose(
                            customer = customer,
                            onSelect = {
                                onSelect(customer)
                                onDismiss()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        }
    }
}


@Composable
fun CustomerCardCompose(
    customer: Customer,
    onSelect: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                
                Text(
                    text = customer.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!customer.email.isNullOrEmpty()) {
                    Text(
                        text = "✉️ " + customer.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "📞 ${customer.phone ?: stringResource(R.string.no_phone)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "💰 $${customer.balance}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (customer.balance > BigDecimal.ZERO) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                
                Text(
                    text = "⭐ " + stringResource(R.string.pointss) + " ${customer.loyaltyPoints}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onSelect) {
                    Icon(
                        Icons.Default.PersonAddAlt,
                        contentDescription = "Select",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

            }
        }
    }
}
