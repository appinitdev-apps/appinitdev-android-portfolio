data class PlotFunction(
    val name: String,
    val formulaLatex: String,
    val evaluator: (Double) -> Double
)
@Composable
fun FunctionPlotter(
    modifier: Modifier = Modifier
) {
    val availableFunctions = listOf(
        PlotFunction("Parábola", "y = x² - 4") { x -> x * x - 4.0 },
        PlotFunction("Seno", "y = sin(x)") { x -> sin(x) },
        PlotFunction("Coseno", "y = cos(x)") { x -> cos(x) },
        PlotFunction("Exponencial", "y = e^x") { x -> exp(x) },
        PlotFunction("Logaritmo", "y = ln(x)") { x -> if (x > 0) ln(x) else Double.NaN },
        PlotFunction("Polinomio", "y = x³ - 3x") { x -> x * x * x - 3 * x },
        PlotFunction("Línea Recta", "y = 2x + 1") { x -> 2 * x + 1.0 }
    )
    var selectedFunc by remember { mutableStateOf(availableFunctions.first()) }
    var scale by remember { mutableFloatStateOf(40f) } 
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📈 Graficador de Funciones 2D",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(availableFunctions) { func ->
                    FilterChip(
                        selected = selectedFunc.name == func.name,
                        onClick = { selectedFunc = func },
                        label = { Text("${func.name}: ${func.formulaLatex}") }
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val centerX = width / 2f
                    val centerY = height / 2f
                    val axisColor = Color.Gray.copy(alpha = 0.6f)
                    val gridColor = Color.Gray.copy(alpha = 0.2f)
                    val curveColor = Color(0xFF3B82F6)
                    val step = scale
                    var xGrid = centerX % step
                    while (xGrid < width) {
                        drawLine(gridColor, Offset(xGrid, 0f), Offset(xGrid, height), strokeWidth = 1f)
                        xGrid += step
                    }
                    var yGrid = centerY % step
                    while (yGrid < height) {
                        drawLine(gridColor, Offset(0f, yGrid), Offset(width, yGrid), strokeWidth = 1f)
                        yGrid += step
                    }
                    drawLine(axisColor, Offset(0f, centerY), Offset(width, centerY), strokeWidth = 3f)
                    drawLine(axisColor, Offset(centerX, 0f), Offset(centerX, height), strokeWidth = 3f)
                    val path = Path()
                    var isFirst = true
                    for (px in 0..width.toInt() step 2) {
                        val xVal = (px - centerX) / scale
                        val yVal = selectedFunc.evaluator(xVal.toDouble())
                        if (!yVal.isNaN() && !yVal.isInfinite()) {
                            val py = centerY - (yVal.toFloat() * scale)
                            if (py in -500f..(height + 500f)) {
                                if (isFirst) {
                                    path.moveTo(px.toFloat(), py)
                                    isFirst = false
                                } else {
                                    path.lineTo(px.toFloat(), py)
                                }
                            } else {
                                isFirst = true
                            }
                        } else {
                            isFirst = true
                        }
                    }
                    drawPath(
                        path = path,
                        color = curveColor,
                        style = Stroke(width = 4f)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Función: ${selectedFunc.formulaLatex}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { if (scale > 15f) scale -= 10f }) {
                        Text("🔍 -")
                    }
                    Button(onClick = { if (scale < 120f) scale += 10f }) {
                        Text("🔍 +")
                    }
                }
            }
        }
    }
}
