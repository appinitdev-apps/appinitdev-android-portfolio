
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    viewModel: CustomerViewModel = hiltViewModel(),
) {
    CustomersScreenCompose(viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreenCompose(
    viewModel: CustomerViewModel,
) {
    val filteredCustomers by viewModel.filteredCustomers.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.observeAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var searchType by remember { mutableStateOf("name") }

    var showCustomersDialogCreate by remember { mutableStateOf(false) }
    var showCustomersDialogUpdate by remember { mutableStateOf(false) }
    var showInfoCustomersDialog by remember { mutableStateOf(true) }
    var customersToEdit by remember { mutableStateOf<Customer?>(null) }
    var customerEdit by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        // verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.customers),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.manage_your_customers),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    onClick = {
                        customerEdit = !customerEdit
                    },
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (!customerEdit) Icons.Default.Edit else Icons.Default.Check,
                            contentDescription = "Edit customer",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(5.dp))


                Surface(
                    onClick = {
                        showCustomersDialogCreate = true
                        customersToEdit = null
                    },
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Customer",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.searchCustomers(it)
            },
            label = { Text(stringResource(R.string.search_customers)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(modifier = Modifier.height(8.dp))

        // Filter Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = { viewModel.getCustomersWithLoyaltyPoints() },
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text(stringResource(R.string.loyalty))
                }
            }

            Button(
                onClick = { viewModel.getCustomersWithBalance() },
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text( stringResource(R.string.balancee))
                }
            }

            Button(
                onClick = { viewModel.getHighRiskCustomers() },
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text(stringResource(R.string.high_risk))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { viewModel.clearFilters() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.clear_filters))
        }

        Spacer(modifier = Modifier.height(16.dp))


        // Customers List
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (filteredCustomers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_customers_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                items(filteredCustomers) { customer ->
                    CustomerCardCompose(
                        customer = customer,
                        onEdit = {
                            showCustomersDialogUpdate = true
                            customersToEdit = customer
                        },
                        edit = customerEdit,
                        onDelete = { viewModel.deleteCustomer(customer) },
                        onSelect = {
                            showInfoCustomersDialog = true
                            customersToEdit = customer
                            //viewModel.selectCustomer(customer) }
                        }
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
    }

    if (showCustomersDialogCreate) {
        CustomerFormDialog(
            showDialog = showCustomersDialogCreate,
            onDismiss = { showCustomersDialogCreate = false },
            onSave = { customer ->
                viewModel.addCustomer(customer)
                // Aquí guardas el producto, por ejemplo en un ViewModel o lista
                Log.d("customer", "customer creado: $customer")
                showCustomersDialogCreate = false
            },
            initialCustomer = customersToEdit, // null porque es creación

        )
    }

    if (showInfoCustomersDialog && customersToEdit != null) {
        CustomerDialog(
            customer = customersToEdit!!,
            onDismiss = { showInfoCustomersDialog = false },
            onSave = { customer ->
                viewModel.updateCustomer(customer)
            }
        )
    }

    if (showCustomersDialogUpdate && customersToEdit != null) {
        CustomerFormDialog(
            showDialog = showCustomersDialogUpdate,
            onDismiss = { showCustomersDialogUpdate = false },
            onSave = { customer ->
                viewModel.updateCustomer(customer)
                // Aquí guardas el producto, por ejemplo en un ViewModel o lista
                Log.d("Products", "Producto actualizado: $customer")
                showCustomersDialogUpdate = false
            },
            initialCustomer = customersToEdit, // null porque es creación

        )
    }
}

@Composable
fun CustomerCardCompose(
    customer: com.mopanesystems.ziposmobile.data.model.Customer,
    onEdit: () -> Unit,
    edit: Boolean = false,
    onDelete: () -> Unit,
    onSelect: () -> Unit,
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp, horizontal = 4.dp),
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
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {


                    if (!customer.email.isNullOrEmpty()) {
                        Text(
                            text = "✉️ ${customer.email}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(onClick = {
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Email", customer.email)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context,
                                context.getString(R.string.email_copiado), Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar email")
                        }
                    }

                    if (!customer.phone.isNullOrEmpty()) {
                        Text(
                            text = "📞 ${customer.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(onClick = {
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Phone", customer.phone ?: "")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context,
                                context.getString(R.string.tel_fono_copiado), Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar teléfono")
                        }

                    }

                }


                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    customer.loyaltyPoints.let {
                        Text(
                            text = stringResource(R.string.pointss) +" ${it}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold // 👈 fuerza negrita
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    customer.balance.let {

                        val colorContainer = when {
                            it.signum() < 0 -> MaterialTheme.colorScheme.error
                            it.signum() == 0 -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.secondary
                        }
                        Text(
                            text = stringResource(R.string.balanceee) +" ${it}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold // 👈 fuerza negrita
                            ),
                            color = colorContainer
                        )
                    }

                    customer.riskScore.let {

                        val colorContainer = when {
                            it == 0 -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.error
                        }
                        val emoji = when {
                            it == 0 -> "😊"
                            it in 1..3 ->  "😐"
                            else -> "😡"
                        }

                        Text(
                            text = stringResource(R.string.riskscore) +" ${emoji} ${it}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold // 👈 fuerza negrita
                            ),
                            color = colorContainer
                        )
                    }


                }



            }

            // Botones de acción

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onSelect) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Select",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                if (edit)
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                if (edit)
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
            }
        }
    }
}
