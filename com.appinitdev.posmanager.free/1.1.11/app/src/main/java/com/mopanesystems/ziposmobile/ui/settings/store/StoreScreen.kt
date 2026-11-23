
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    viewModel: StoreViewModel = hiltViewModel(),
    onBack: (() -> Unit)? = null
) {
    val storeState by viewModel.store.collectAsState()

    storeState?.let { s ->
        var name by remember { mutableStateOf(s.name) }
        var address by remember { mutableStateOf(s.address) }
        var city by remember { mutableStateOf(s.city) }
        var state by remember { mutableStateOf(s.state) }
        var zipCode by remember { mutableStateOf(s.zipCode) }
        var country by remember { mutableStateOf(s.country) }
        var phone by remember { mutableStateOf(s.phone.orEmpty()) }
        var email by remember { mutableStateOf(s.email.orEmpty()) }
        var website by remember { mutableStateOf(s.website.orEmpty()) }
        var taxId by remember { mutableStateOf(s.taxId.orEmpty()) }
        var currency by remember { mutableStateOf(s.currency) }
        var timezone by remember { mutableStateOf(s.timezone) }
        val context = LocalContext.current
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.configuraciones_de_la_tienda)) },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.Default.ArrowBackIosNew,
                                    contentDescription = stringResource(R.string.atr_s)
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Todos los campos ocupan todo el ancho
                    val fields = listOf(
                        stringResource(R.string.name) to name,
                        stringResource(R.string.address) to address,
                        stringResource(R.string.city) to city,
                        stringResource(R.string.state) to state,
                        stringResource(R.string.zip_code) to zipCode,
                        stringResource(R.string.country) to country,
                        stringResource(R.string.phone) to phone,
                        stringResource(R.string.email) to email,
                        stringResource(R.string.website) to website,
                        stringResource(R.string.tax_id) to taxId,
                        stringResource(R.string.currency) to currency,
                        stringResource(R.string.time_zone) to timezone
                    )
                    
                    val nameLabel = stringResource(R.string.name)
                    val addressLabel = stringResource(R.string.address)
                    val cityLabel = stringResource(R.string.city)
                    val stateLabel = stringResource(R.string.state)
                    val zipLabel = stringResource(R.string.zip_code)
                    val countryLabel = stringResource(R.string.country)
                    val phoneLabel = stringResource(R.string.phone)
                    val emailLabel = stringResource(R.string.email)
                    val websiteLabel = stringResource(R.string.website)
                    val taxIdLabel = stringResource(R.string.tax_id)
                    val currencyLabel = stringResource(R.string.currency)
                    val timezoneLabel = stringResource(R.string.time_zone)

                    fields.forEach { (label, value) ->
                        OutlinedTextField(
                            value = value,
                            onValueChange = {
                                when (label) {
                                    nameLabel -> name = it
                                    addressLabel -> address = it
                                    cityLabel -> city = it
                                    stateLabel -> state = it
                                    zipLabel -> zipCode = it
                                    countryLabel -> country = it
                                    phoneLabel -> phone = it
                                    emailLabel -> email = it
                                    websiteLabel -> website = it
                                    taxIdLabel -> taxId = it
                                    currencyLabel -> currency = it
                                    timezoneLabel -> timezone = it
                                }
                            },
                            label = { Text(label) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.updateStore(
                                s.copy(
                                    name = name,
                                    address = address,
                                    city = city,
                                    state = state,
                                    zipCode = zipCode,
                                    country = country,
                                    phone = phone.ifBlank { null },
                                    email = email.ifBlank { null },
                                    website = website.ifBlank { null },
                                    taxId = taxId.ifBlank { null },
                                    currency = currency,
                                    timezone = timezone,
                                    updatedAt = LocalDateTime.now()
                                )
                            )
                            Toast.makeText(context,
                                context.getString(R.string.datos_de_tienda_guardado), Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(stringResource(R.string.guardar_cambios))
                    }
                }
            }
        }
    } ?: Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
