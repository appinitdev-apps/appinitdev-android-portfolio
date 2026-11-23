
// --- MODELO DE DATOS ---
data class IdenticonResult(
    val matrix: Array<BooleanArray>,
    val color: Color
)

// --- COMPONENTE VISUAL PRINCIPAL ---
@Composable
fun AsyncIdenticonAvatar(
    appName: String,
    modifier: Modifier = Modifier,
    gridSize: Int = 7
) {
    var identiconData by remember(appName) { mutableStateOf<IdenticonResult?>(null) }

    LaunchedEffect(appName) {
        // Cambiamos a Default para procesamiento pesado
        identiconData = withContext(Dispatchers.Default) {
            generateIdenticonData(appName, gridSize)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF3F4F6)),
        contentAlignment = Alignment.Center
    ) {
        // Placeholder: Si es nulo, mostramos el fondo gris
        if (identiconData == null) {
            // Puedes poner un CircularProgressIndicator pequeño aquí si deseas
        }

        AnimatedVisibility(
            visible = identiconData != null,
            enter = fadeIn(animationSpec = tween(durationMillis = 400))
        ) {
            identiconData?.let { data ->
                Canvas(
                    modifier = Modifier
                        .fillMaxSize() // Cambiado de matchParentSize para forzar llenado
                        .padding(0.dp) // Reducido un poco el padding
                ) {
                    // Calculamos el tamaño de celda basado en el tamaño real del Canvas
                    val canvasSize = size.minDimension
                    val cellSize = canvasSize / gridSize

                    for (x in 0 until gridSize) {
                        for (y in 0 until gridSize) {
                            if (data.matrix[x][y]) {
                                drawRect(
                                    color = data.color,
                                    topLeft = Offset(x * cellSize, y * cellSize),
                                    // Usamos size = cellSize para que encajen perfecto
                                    size = Size(cellSize, cellSize),
                                    style = Fill
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- LÓGICA DE GENERACIÓN (SEGUNDO PLANO) ---
private fun generateIdenticonData(name: String, gridSize: Int): IdenticonResult {
    // 1. Crear el Hash MD5 único del nombre
    val hash = MessageDigest.getInstance("MD5")
        .digest(name.trim().lowercase().toByteArray())

    // 2. Generar Color con Contraste Profesional (Sistema HSV)
    // Usamos el hash para el Tono (0-360)
    val hue = (hash.fold(0) { acc, byte -> acc + abs(byte.toInt()) } % 360).toFloat()

    // Saturación: 60% a 80% | Valor (Brillo): 45% a 65%
    // Esto asegura que resalte sobre fondos claros y se vea "caro"
    val saturation = 0.60f + ((hash[0].toInt() and 0xFF) % 20) / 100f
    val value = 0.45f + ((hash[1].toInt() and 0xFF) % 20) / 100f
    val color = Color.hsv(hue = hue, saturation = saturation, value = value)

    // 3. Crear matriz simétrica
    val matrix = Array(gridSize) { BooleanArray(gridSize) }
    val columnsToGenerate = (gridSize / 2) + 1

    for (x in 0 until columnsToGenerate) {
        for (y in 0 until gridSize) {
            // Usamos una posición de byte saltada para variar el patrón respecto al color
            val bytePos = (x * gridSize + y + 2) % hash.size
            val isFilled = (hash[bytePos].toInt() and 1) == 0

            matrix[x][y] = isFilled
            // Reflejo horizontal (Simetría)
            matrix[gridSize - 1 - x][y] = isFilled
        }
    }

    return IdenticonResult(matrix, color)
}