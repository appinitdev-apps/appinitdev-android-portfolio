

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerFormDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit,
    initialCustomer: Customer? = null
) {
    if (!showDialog) return

    var firstName by remember { mutableStateOf(initialCustomer?.firstName ?: "") }
    var lastName by remember { mutableStateOf(initialCustomer?.lastName ?: "") }
    var email by remember { mutableStateOf(initialCustomer?.email ?: "") }
    var phone by remember { mutableStateOf(initialCustomer?.phone ?: "") }
    var address by remember { mutableStateOf(initialCustomer?.address ?: "") }
    var city by remember { mutableStateOf(initialCustomer?.city ?: "") }
    var state by remember { mutableStateOf(initialCustomer?.state ?: "") }
    var zipCode by remember { mutableStateOf(initialCustomer?.zipCode ?: "") }
    var country by remember { mutableStateOf(initialCustomer?.country ?: "") }
    var notes by remember { mutableStateOf(initialCustomer?.notes ?: "") }
    var creditLimit by remember { mutableStateOf(initialCustomer?.creditLimit?.toPlainString() ?: "1000") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .imePadding(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = 8.dp
            ) {
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .imePadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (initialCustomer == null) stringResource(R.string.nuevo_cliente) else stringResource(R.string.editar_cliente),
                        style = MaterialTheme.typography.headlineSmall
                    )

                    // Campos de texto generales
                    data class FieldConfig(val label: String, val value: String, val isNumeric: Boolean = false, val onChange: (String) -> Unit)
                    listOf(
                        FieldConfig(stringResource(R.string.nombre), firstName) { firstName = it },
                        FieldConfig(stringResource(R.string.apellido), lastName) { lastName = it },
                        FieldConfig(stringResource(R.string.email1), email) { email = it },
                        FieldConfig(stringResource(R.string.tel_fono), phone, isNumeric = true) { phone = it },
                        FieldConfig(stringResource(R.string.direcci_n), address) { address = it },
                        FieldConfig(stringResource(R.string.ciudad), city) { city = it },
                        FieldConfig(stringResource(R.string.estado), state) { state = it },
                        FieldConfig(stringResource(R.string.c_digo_postal), zipCode, isNumeric = true) { zipCode = it },
                        FieldConfig(stringResource(R.string.pa_s), country) { country = it },
                        FieldConfig(stringResource(R.string.notesa), notes) { notes = it }
                    ).forEach { (label, value, isNumeric, onChange) ->
                        OutlinedTextField(
                            value = value,
                            onValueChange = onChange,
                            label = { Text(label) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = if (isNumeric)
                                KeyboardOptions(keyboardType = KeyboardType.Number)
                            else KeyboardOptions.Default
                        )
                    }

                    // Campo para Credit Limit
                    OutlinedTextField(
                        value = creditLimit,
                        onValueChange = {
                            if (it.matches(Regex("^[0-9]*\\.?[0-9]*$"))) creditLimit = it
                        },
                        label = { Text(stringResource(R.string.l_mite_de_cr_dito)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Botones
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(onClick = onDismiss) { Text(text = stringResource(R.string.cancelar))}
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            val customer = createNewCustomerOrUpdate(
                                firstName, lastName, email.ifEmpty { null }, phone.ifEmpty { null },
                                address.ifEmpty { null }, city.ifEmpty { null }, state.ifEmpty { null },
                                zipCode.ifEmpty { null }, country.ifEmpty { null },
                                notes.ifEmpty { null }, creditLimit,
                                initialCustomer
                            )
                            onSave(customer)
                            onDismiss()
                        }) {
                            Text(stringResource(R.string.guardar_cliente))
                        }
                    }
                }
            }
        }
    }
}

fun createNewCustomerOrUpdate(
    firstName: String,
    lastName: String,
    email: String?,
    phone: String?,
    address: String?,
    city: String?,
    state: String?,
    zipCode: String?,
    country: String?,
    notes: String?,
    creditLimit: String,
    initialCustomer: Customer? = null,
): Customer {
    return if (initialCustomer == null) {
        Customer(
            id = UUID.randomUUID().toString(),
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = phone,
            address = address,
            city = city,
            state = state,
            zipCode = zipCode,
            country = country,
            notes = notes,
            creditLimit = creditLimit.toBigDecimalOrNull() ?: BigDecimal(1000), // ✅ si no escribe nada, 1000
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            isActive = true
        )
    } else {
        initialCustomer.copy(
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = phone,
            address = address,
            city = city,
            state = state,
            zipCode = zipCode,
            country = country,
            notes = notes,
            creditLimit = creditLimit.toBigDecimalOrNull() ?: initialCustomer.creditLimit,
            updatedAt = LocalDateTime.now()
        )
    }
}
