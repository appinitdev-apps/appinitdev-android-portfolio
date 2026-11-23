

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductFormDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit,
    initialProduct: Product? = null,
    categories: List<String> = listOf("All", "Electronics", "Clothing", "Books", "Food"),
) {
    if (!showDialog) return

    val context = LocalContext.current

    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var sku by remember { mutableStateOf(initialProduct?.sku ?: "") }
    var price by remember { mutableStateOf(initialProduct?.price?.toPlainString() ?: "") }
    var categoryId by remember { mutableStateOf(initialProduct?.categoryId ?: categories.first()) }
    var barcode by remember { mutableStateOf(initialProduct?.barcode ?: "") }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }
    var cost by remember { mutableStateOf(initialProduct?.cost?.toPlainString() ?: "") }
    var stockQuantity by remember {
        mutableStateOf(
            initialProduct?.stockQuantity?.toString() ?: "10"
        )
    }
    var minStockLevel by remember {
        mutableStateOf(
            initialProduct?.minStockLevel?.toString() ?: "5"
        )
    }
    var isTaxable by remember { mutableStateOf(initialProduct?.isTaxable ?: true) }
    var taxRate by remember { mutableStateOf(initialProduct?.taxRate?.toPlainString() ?: "0") }
    var showCategoryDialog by remember { mutableStateOf(false) }

    val barcodeScannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getStringExtra(BarcodeScannerActivity.EXTRA_BARCODE_RESULT)?.let {
                barcode = it
            }
        }
    }



    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            //usePlatformDefaultWidth = false,

            decorFitsSystemWindows = false
        ),

        ){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 6.dp, bottom = 6.dp), // <- margen externo real
        contentAlignment = Alignment.Center // o TopCenter si prefieres que quede arriba
    ) {

            Surface(
                modifier = Modifier
                    .fillMaxWidth()        // ancho completo (dentro del Box con padding)
                    .wrapContentHeight()   // que no se estire a toda la altura
                    .imePadding(),         // evita que el teclado tape contenido
                shape = MaterialTheme.shapes.large,
                tonalElevation = 8.dp
            ) {
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .fillMaxSize() // El Column debe tomar todo el espacio disponible
                        .imePadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (initialProduct == null) stringResource(R.string.nuevo_producto) else stringResource(
                            R.string.editar_producto
                        ),
                        style = MaterialTheme.typography.headlineSmall
                    )

                    // Campos básicos
                    listOf(
                        Triple(stringResource(R.string.nombrep), name) { value: String -> name = value },
                        Triple(stringResource(R.string.sku), sku) { value: String -> sku = value },
                        Triple(stringResource(R.string.precio), price) { value: String ->
                            if (value.matches(Regex("^[0-9]*\\.?[0-9]*$"))) price = value
                        }
                    ).forEach { (label, value, onChange) ->
                        OutlinedTextField(
                            value = value,
                            onValueChange = onChange,
                            label = { Text(label) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = if (label == stringResource(R.string.precio))
                                KeyboardOptions(keyboardType = KeyboardType.Number)
                            else KeyboardOptions.Default
                        )
                    }


                    CategoryDropdown(
                        categories = categories,
                        selectedCategory = categoryId,
                        onCategorySelected = { categoryId = it }
                    )

                    // Código de barras
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text(stringResource(R.string.c_digo_de_barras)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            barcodeScannerLauncher.launch(
                                Intent(
                                    context,
                                    BarcodeScannerActivity::class.java
                                )
                            )
                        }) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear")
                        }
                    }

                    // Otros campos
                    listOf(
                        Triple(stringResource(R.string.descripci_np), description) { value: String -> description = value },
                        Triple(stringResource(R.string.costop), cost) { value: String ->
                            if (value.matches(Regex("^[0-9]*\\.?[0-9]*$"))) cost = value
                        },
                        Triple(stringResource(R.string.stockp), stockQuantity) { value: String ->
                            if (value.matches(Regex("^[0-9]*$"))) stockQuantity = value
                        },
                        Triple(stringResource(R.string.stock_m_nimo), minStockLevel) { value: String ->
                            if (value.matches(Regex("^[0-9]*$"))) minStockLevel = value
                        }
                    ).forEach { (label, value, onChange) ->
                        OutlinedTextField(
                            value = value,
                            onValueChange = onChange,
                            label = { Text(label) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = if (label in listOf(stringResource(R.string.costop), stringResource(R.string.stockp), stringResource(R.string.stock_m_nimo)))
                                KeyboardOptions(keyboardType = KeyboardType.Number)
                            else KeyboardOptions.Default
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isTaxable, onCheckedChange = { isTaxable = it })
                        Text(stringResource(R.string.aplica_impuestos))
                    }

                    if (isTaxable) {
                        OutlinedTextField(
                            value = taxRate,
                            onValueChange = {
                                if (it.matches(Regex("^[0-9]*\\.?[0-9]*$"))) taxRate = it
                            },
                            label = { Text(stringResource(R.string.tasa_de_impuesto)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Botones
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.cancelar)) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            val product = createNewProductOrUpdate(
                                name, sku, price.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                                if (categoryId == "All") null else categoryId,
                                barcode.ifEmpty { null }, description.ifEmpty { null },
                                cost.toBigDecimalOrNull(),
                                stockQuantity.toIntOrNull() ?: 0,
                                minStockLevel.toIntOrNull() ?: 0,
                                isTaxable,
                                taxRate.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                                initialProduct
                            )
                            onSave(product)
                            onDismiss()
                        }) { Text(stringResource(R.string.guardar_producto)) }
                    }
                }

            }



    }
    }


}

fun createNewProductOrUpdate(
    name: String,
    sku: String,
    price: BigDecimal,
    categoryId: String? = null,
    barcode: String? = null,
    description: String? = null,
    cost: BigDecimal? = null,
    stockQuantity: Int = 0,
    minStockLevel: Int = 0,
    isTaxable: Boolean = true,
    taxRate: BigDecimal = BigDecimal.ZERO,
    initialProduct: Product? = null,
): Product {
    return if (initialProduct == null) {
        Product(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            sku = sku,
            barcode = barcode,
            price = price,
            cost = cost,
            categoryId = categoryId,
            stockQuantity = stockQuantity,
            minStockLevel = minStockLevel,
            isActive = true,
            isTaxable = isTaxable,
            taxRate = taxRate,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    } else {
        initialProduct.copy(
            name = name,
            description = description,
            sku = sku,
            barcode = barcode,
            price = price,
            cost = cost,
            categoryId = categoryId,
            stockQuantity = stockQuantity,
            minStockLevel = minStockLevel,
            isTaxable = isTaxable,
            taxRate = taxRate,
            updatedAt = LocalDateTime.now()
        )
    }


}
