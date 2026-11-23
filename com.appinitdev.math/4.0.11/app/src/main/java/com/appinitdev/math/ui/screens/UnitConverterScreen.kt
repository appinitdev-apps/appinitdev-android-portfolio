
data class UnitCategory(
    val id: String,
    val nameEs: String,
    val nameEn: String,
    val icon: String,
    val units: List<UnitDefinition>
)

data class UnitDefinition(
    val code: String,
    val nameEs: String,
    val nameEn: String,
    val toBase: (Double) -> Double,
    val fromBase: (Double) -> Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    viewModel: MathViewModel,
    onBack: () -> Unit
) {
    val currentLang by viewModel.currentLanguage.collectAsState()

    val categories = remember {
        listOf(
            UnitCategory(
                "ANGLE", "Ángulos", "Angles", "📐",
                listOf(
                    UnitDefinition("rad", "Radianes (rad)", "Radians (rad)", { it }, { it }),
                    UnitDefinition("deg", "Grados (°)", "Degrees (°)", { Math.toRadians(it) }, { Math.toDegrees(it) }),
                    UnitDefinition("grad", "Grados Centesimales", "Gradians", { it * Math.PI / 200.0 }, { it * 200.0 / Math.PI }),
                    UnitDefinition("rev", "Revoluciones (rev)", "Revolutions (rev)", { it * 2 * Math.PI }, { it / (2 * Math.PI) })
                )
            ),
            UnitCategory(
                "LENGTH", "Longitud", "Length", "📏",
                listOf(
                    UnitDefinition("m", "Metros (m)", "Meters (m)", { it }, { it }),
                    UnitDefinition("km", "Kilómetros (km)", "Kilometers (km)", { it * 1000.0 }, { it / 1000.0 }),
                    UnitDefinition("cm", "Centímetros (cm)", "Centimeters (cm)", { it / 100.0 }, { it * 100.0 }),
                    UnitDefinition("mm", "Milímetros (mm)", "Millimeters (mm)", { it / 1000.0 }, { it * 1000.0 }),
                    UnitDefinition("mi", "Millas (mi)", "Miles (mi)", { it * 1609.344 }, { it / 1609.344 }),
                    UnitDefinition("ft", "Pies (ft)", "Feet (ft)", { it * 0.3048 }, { it / 0.3048 }),
                    UnitDefinition("in", "Pulgadas (in)", "Inches (in)", { it * 0.0254 }, { it / 0.0254 })
                )
            ),
            UnitCategory(
                "DATA", "Almacenamiento de Datos", "Data Storage", "💾",
                listOf(
                    UnitDefinition("B", "Bytes (B)", "Bytes (B)", { it }, { it }),
                    UnitDefinition("KB", "Kilobytes (KB)", "Kilobytes (KB)", { it * 1024.0 }, { it / 1024.0 }),
                    UnitDefinition("MB", "Megabytes (MB)", "Megabytes (MB)", { it * 1048576.0 }, { it / 1048576.0 }),
                    UnitDefinition("GB", "Gigabytes (GB)", "Gigabytes (GB)", { it * 1073741824.0 }, { it / 1073741824.0 }),
                    UnitDefinition("TB", "Terabytes (TB)", "Terabytes (TB)", { it * 1099511627776.0 }, { it / 1099511627776.0 })
                )
            ),
            UnitCategory(
                "SPEED", "Velocidad", "Speed", "🚀",
                listOf(
                    UnitDefinition("m/s", "Metros/segundo (m/s)", "Meters/sec (m/s)", { it }, { it }),
                    UnitDefinition("km/h", "Kilómetros/hora (km/h)", "Kilometers/hour (km/h)", { it / 3.6 }, { it * 3.6 }),
                    UnitDefinition("mph", "Millas/hora (mph)", "Miles/hour (mph)", { it * 0.44704 }, { it / 0.44704 }),
                    UnitDefinition("kn", "Nudos (kn)", "Knots (kn)", { it * 0.514444 }, { it / 0.514444 })
                )
            ),
            UnitCategory(
                "MASS", "Masa / Peso", "Mass / Weight", "⚖️",
                listOf(
                    UnitDefinition("kg", "Kilogramos (kg)", "Kilograms (kg)", { it }, { it }),
                    UnitDefinition("g", "Gramos (g)", "Grams (g)", { it / 1000.0 }, { it * 1000.0 }),
                    UnitDefinition("mg", "Miligramos (mg)", "Milligrams (mg)", { it / 1000000.0 }, { it * 1000000.0 }),
                    UnitDefinition("lb", "Libras (lb)", "Pounds (lb)", { it * 0.45359237 }, { it / 0.45359237 }),
                    UnitDefinition("oz", "Onzas (oz)", "Ounces (oz)", { it * 0.028349523125 }, { it / 0.028349523125 }),
                    UnitDefinition("t", "Toneladas (t)", "Metric Tons (t)", { it * 1000.0 }, { it / 1000.0 })
                )
            ),
            UnitCategory(
                "POWER", "Potencia", "Power", "🔌",
                listOf(
                    UnitDefinition("W", "Vatios (W)", "Watts (W)", { it }, { it }),
                    UnitDefinition("kW", "Kilovatios (kW)", "Kilowatts (kW)", { it * 1000.0 }, { it / 1000.0 }),
                    UnitDefinition("MW", "Megavatios (MW)", "Megawatts (MW)", { it * 1000000.0 }, { it / 1000000.0 }),
                    UnitDefinition("HP", "Caballos de Fuerza (HP)", "Horsepower (HP)", { it * 745.699872 }, { it / 745.699872 })
                )
            ),
            UnitCategory(
                "FREQUENCY", "Frecuencia", "Frequency", "📻",
                listOf(
                    UnitDefinition("Hz", "Hertz (Hz)", "Hertz (Hz)", { it }, { it }),
                    UnitDefinition("kHz", "Kilohertz (kHz)", "Kilohertz (kHz)", { it * 1000.0 }, { it / 1000.0 }),
                    UnitDefinition("MHz", "Megahertz (MHz)", "Megahertz (MHz)", { it * 1000000.0 }, { it / 1000000.0 }),
                    UnitDefinition("GHz", "Gigahertz (GHz)", "Gigahertz (GHz)", { it * 1000000000.0 }, { it / 1000000000.0 })
                )
            ),
            UnitCategory(
                "TEMP", "Temperatura", "Temperature", "🌡️",
                listOf(
                    UnitDefinition("C", "Celsius (°C)", "Celsius (°C)", { it }, { it }),
                    UnitDefinition("F", "Fahrenheit (°F)", "Fahrenheit (°F)", { (it - 32.0) * 5.0 / 9.0 }, { it * 9.0 / 5.0 + 32.0 }),
                    UnitDefinition("K", "Kelvin (K)", "Kelvin (K)", { it - 273.15 }, { it + 273.15 })
                )
            ),
            UnitCategory(
                "AREA", "Área", "Area", "🗺️",
                listOf(
                    UnitDefinition("m2", "Metros² (m²)", "Square Meters (m²)", { it }, { it }),
                    UnitDefinition("km2", "Kilómetros² (km²)", "Square Kilometers (km²)", { it * 1000000.0 }, { it / 1000000.0 }),
                    UnitDefinition("ha", "Hectáreas (ha)", "Hectares (ha)", { it * 10000.0 }, { it / 10000.0 }),
                    UnitDefinition("ft2", "Pies² (ft²)", "Square Feet (ft²)", { it * 0.09290304 }, { it / 0.09290304 }),
                    UnitDefinition("ac", "Acres (ac)", "Acres (ac)", { it * 4046.8564224 }, { it / 4046.8564224 })
                )
            ),
            UnitCategory(
                "VOLUME", "Volumen", "Volume", "🧪",
                listOf(
                    UnitDefinition("L", "Litros (L)", "Liters (L)", { it }, { it }),
                    UnitDefinition("mL", "Mililitros (mL)", "Milliliters (mL)", { it / 1000.0 }, { it * 1000.0 }),
                    UnitDefinition("m3", "Metros³ (m³)", "Cubic Meters (m³)", { it * 1000.0 }, { it / 1000.0 }),
                    UnitDefinition("gal", "Galones (gal)", "Gallons (gal)", { it * 3.785411784 }, { it / 3.785411784 }),
                    UnitDefinition("floz", "Onzas Líquidas", "Fluid Ounces", { it * 0.0295735 }, { it / 0.0295735 })
                )
            ),
            UnitCategory(
                "PRESSURE", "Presión", "Pressure", "📊",
                listOf(
                    UnitDefinition("Pa", "Pascales (Pa)", "Pascals (Pa)", { it }, { it }),
                    UnitDefinition("bar", "Bar (bar)", "Bar (bar)", { it * 100000.0 }, { it / 100000.0 }),
                    UnitDefinition("atm", "Atmósferas (atm)", "Atmospheres (atm)", { it * 101325.0 }, { it / 101325.0 }),
                    UnitDefinition("psi", "PSI (psi)", "PSI (psi)", { it * 6894.75729 }, { it / 6894.75729 })
                )
            ),
            UnitCategory(
                "ENERGY", "Energía", "Energy", "⚡",
                listOf(
                    UnitDefinition("J", "Julios (J)", "Joules (J)", { it }, { it }),
                    UnitDefinition("kJ", "Kilojulios (kJ)", "Kilojoules (kJ)", { it * 1000.0 }, { it / 1000.0 }),
                    UnitDefinition("cal", "Calorías (cal)", "Calories (cal)", { it * 4.184 }, { it / 4.184 }),
                    UnitDefinition("kcal", "Kilocalorías (kcal)", "Kilocalories (kcal)", { it * 4184.0 }, { it / 4184.0 }),
                    UnitDefinition("kWh", "Kilovatios-hora", "Kilowatt-hours", { it * 3600000.0 }, { it / 3600000.0 })
                )
            )
        )
    }

    var selectedCat by remember { mutableStateOf(categories.first()) }
    var fromUnit by remember(selectedCat) { mutableStateOf(selectedCat.units.first()) }
    var toUnit by remember(selectedCat) { mutableStateOf(if (selectedCat.units.size > 1) selectedCat.units[1] else selectedCat.units.first()) }
    var inputValueStr by remember { mutableStateOf("1") }

    val inputValue = inputValueStr.toDoubleOrNull() ?: 0.0
    val baseValue = fromUnit.toBase(inputValue)
    val convertedValue = toUnit.fromBase(baseValue)

    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.title_converter), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text(text = "←", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(Screen.HOME) },
                    icon = { Text("🏠") },
                    label = { Text(stringResource(id = R.string.nav_home)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(Screen.SEARCH) },
                    icon = { Text("🔍") },
                    label = { Text(stringResource(id = R.string.nav_search)) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { viewModel.navigateTo(Screen.FAVORITES) },
                    icon = { Text("⭐") },
                    label = { Text(stringResource(id = R.string.nav_favorites)) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val catName = if (currentLang == "en") cat.nameEn else cat.nameEs
                    FilterChip(
                        selected = selectedCat.id == cat.id,
                        onClick = { selectedCat = cat },
                        label = { Text("${cat.icon} $catName") }
                    )
                }
            }

            // Input Value & From Unit Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.converter_from),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = inputValueStr,
                        onValueChange = { inputValueStr = it },
                        label = { Text(stringResource(id = R.string.converter_enter_value)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    ExposedDropdownMenuBox(
                        expanded = fromExpanded,
                        onExpandedChange = { fromExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = if (currentLang == "en") fromUnit.nameEn else fromUnit.nameEs,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = fromExpanded,
                            onDismissRequest = { fromExpanded = false }
                        ) {
                            selectedCat.units.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(if (currentLang == "en") unit.nameEn else unit.nameEs) },
                                    onClick = {
                                        fromUnit = unit
                                        fromExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Arrow Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "⬇️ " + stringResource(id = R.string.converter_equals),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            // Output Converted Result & To Unit
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.converter_to),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    val formattedResult = String.format(Locale.US, "%.6g", convertedValue)
                    Text(
                        text = formattedResult,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    ExposedDropdownMenuBox(
                        expanded = toExpanded,
                        onExpandedChange = { toExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = if (currentLang == "en") toUnit.nameEn else toUnit.nameEs,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = toExpanded,
                            onDismissRequest = { toExpanded = false }
                        ) {
                            selectedCat.units.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(if (currentLang == "en") unit.nameEn else unit.nameEs) },
                                    onClick = {
                                        toUnit = unit
                                        toExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
