data class IdenticonResult(
    val matrix: Array<BooleanArray>,
    val color: Color
)
@Composable
fun AsyncIdenticonAvatar(
    appName: String,
    modifier: Modifier = Modifier,
    gridSize: Int = 7
) {
    var identiconData by remember(appName) { mutableStateOf<IdenticonResult?>(null) }
    LaunchedEffect(appName) {
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
        if (identiconData == null) {
        }
        AnimatedVisibility(
            visible = identiconData != null,
            enter = fadeIn(animationSpec = tween(durationMillis = 400))
        ) {
            identiconData?.let { data ->
                Canvas(
                    modifier = Modifier
                        .fillMaxSize() 
                        .padding(0.dp) 
                ) {
                    val canvasSize = size.minDimension
                    val cellSize = canvasSize / gridSize
                    for (x in 0 until gridSize) {
                        for (y in 0 until gridSize) {
                            if (data.matrix[x][y]) {
                                drawRect(
                                    color = data.color,
                                    topLeft = Offset(x * cellSize, y * cellSize),
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
private fun generateIdenticonData(name: String, gridSize: Int): IdenticonResult {
    val hash = MessageDigest.getInstance("MD5")
        .digest(name.trim().lowercase().toByteArray())
    val hue = (hash.fold(0) { acc, byte -> acc + abs(byte.toInt()) } % 360).toFloat()
    val saturation = 0.60f + ((hash[0].toInt() and 0xFF) % 20) / 100f
    val value = 0.45f + ((hash[1].toInt() and 0xFF) % 20) / 100f
    val color = Color.hsv(hue = hue, saturation = saturation, value = value)
    val matrix = Array(gridSize) { BooleanArray(gridSize) }
    val columnsToGenerate = (gridSize / 2) + 1
    for (x in 0 until columnsToGenerate) {
        for (y in 0 until gridSize) {
            val bytePos = (x * gridSize + y + 2) % hash.size
            val isFilled = (hash[bytePos].toInt() and 1) == 0
            matrix[x][y] = isFilled
            matrix[gridSize - 1 - x][y] = isFilled
        }
    }
    return IdenticonResult(matrix, color)
}