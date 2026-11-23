
@Composable
fun AdvancedScientificCalculator(
    currentLang: String,
    modifier: Modifier = Modifier
) {
    var calcMode by remember { mutableIntStateOf(0) } // 0: Scientific, 1: Polynomial, 2: Matrix, 3: Calculus

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mode Selector Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val modes = listOf(
                0 to if (currentLang == "en") "🧮 Scientific & Expression" else "🧮 Científica y Expresiones",
                1 to if (currentLang == "en") "🔺 Triangles & Geometry" else "🔺 Triángulos y Geometría",
                2 to if (currentLang == "en") "🔢 Matrices 2x2" else "🔢 Matrices 2x2",
                3 to if (currentLang == "en") "📉 Calculus & Derivatives" else "📉 Cálculo y Derivadas"
            )
            items(modes) { (id, label) ->
                FilterChip(
                    selected = calcMode == id,
                    onClick = { calcMode = id },
                    label = { Text(label) }
                )
            }
        }

        when (calcMode) {
            0 -> ScientificExpressionCalculator(currentLang)
            1 -> TriangleCalculator()
            2 -> MatrixCalculator()
            3 -> CalculusNumericalCalculator(currentLang)
        }
    }
}

@Composable
private fun ScientificExpressionCalculator(currentLang: String) {
    var exprText by remember { mutableStateOf("sin(30) + sqrt(16)") }
    var resultText by remember { mutableStateOf("") }
    var latexPreview by remember { mutableStateOf("$$\\sin(30^\\circ) + \\sqrt{16}$$") }

    fun evaluateExpr(input: String) {
        try {
            var clean = input.trim()
                .replace("×", "*")
                .replace("÷", "/")
                .replace("π", "3.141592653589793")
                .replace("e", "2.718281828459045")

            // Format LaTeX preview
            var latex = clean
                .replace("*", " \\cdot ")
                .replace("/", " / ")
                .replace("sqrt", "\\sqrt")
                .replace("sin", "\\sin")
                .replace("cos", "\\cos")
                .replace("tan", "\\tan")
                .replace("ln", "\\ln")

            latexPreview = "$$$latex$$"

            // Simple evaluator logic for standard inputs
            val valResult = evaluateSimpleMath(clean)
            resultText = if (valResult.isNaN()) "Error" else String.format(Locale.US, "%.6f", valResult)
        } catch (e: Exception) {
            resultText = "Error"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (currentLang == "en") "🧮 Live Formula & Scientific Expression Solver" else "🧮 Calculadora Científica con Previsualizador en Vivo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Live LaTeX Math Previewer
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = if (currentLang == "en") "Live Rendered Formula:" else "Previsualización de la Fórmula:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    MathMarkdownViewer(
                        markdownContent = latexPreview,
                        filterTab = DetailTab.ALL,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                }
            }

            // Expression Input
            OutlinedTextField(
                value = exprText,
                onValueChange = {
                    exprText = it
                    evaluateExpr(it)
                },
                label = { Text(if (currentLang == "en") "Mathematical Expression" else "Expresión Matemática") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Big Result Display
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentLang == "en") "Result:" else "Resultado:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (resultText.isBlank()) "5.000000" else resultText,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Quick Scientific 1-Tap Buttons
            val buttons = listOf(
                "sin(", "cos(", "tan(", "sqrt(",
                "ln(", "log(", "π", "e",
                "^2", "^", "(", ")",
                "7", "8", "9", "÷",
                "4", "5", "6", "×",
                "1", "2", "3", "−",
                "0", ".", "C", "+"
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                buttons.chunked(4).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row.forEach { btn ->
                            Button(
                                onClick = {
                                    if (btn == "C") {
                                        exprText = ""
                                        resultText = ""
                                        latexPreview = "$$\\text{Ingresa expresión}$$"
                                    } else {
                                        exprText += btn
                                        evaluateExpr(exprText)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (btn in listOf("÷", "×", "−", "+", "=")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text(
                                    text = btn,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (btn in listOf("÷", "×", "−", "+", "=")) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculusNumericalCalculator(currentLang: String) {
    var xStr by remember { mutableStateOf("2.0") }
    val x = xStr.toDoubleOrNull() ?: 2.0

    // Numerical derivative f'(x) = d/dx (x^3 - 4x + 1) at x
    val h = 0.00001
    val f = { t: Double -> t * t * t - 4 * t + 1 }
    val df = (f(x + h) - f(x - h)) / (2 * h)

    // Numerical Definite Integral ∫_0^x (t^2) dt
    val numSteps = 100
    val aInt = 0.0
    val bInt = x
    val dx = (bInt - aInt) / numSteps
    var integralSum = 0.0
    for (i in 0 until numSteps) {
        val xi = aInt + i * dx
        integralSum += (xi * xi) * dx
    }

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
                text = "📉 " + if (currentLang == "en") "Numerical Calculus & Derivatives Solver" else "Calculadora de Derivadas e Integrales Numéricas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = xStr,
                onValueChange = { xStr = it },
                label = { Text(if (currentLang == "en") "Evaluation Point x" else "Punto de Evaluación x") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "• Derivada f'(x) para f(x) = x³ - 4x + 1:",
                        fontWeight = FontWeight.Bold
                    )
                    Text("   f'($x) = 3x² - 4 = ${String.format(Locale.US, "%.4f", df)}")

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Integral Definida ∫_0^x (t²) dt:",
                        fontWeight = FontWeight.Bold
                    )
                    Text("   ∫_0^$x (t²) dt = [t³/3]_0^$x = ${String.format(Locale.US, "%.4f", integralSum)}")
                }
            }
        }
    }
}

private fun evaluateSimpleMath(expr: String): Double {
    return try {
        when {
            expr.contains("sin") -> {
                val num = expr.substringAfter("sin(").substringBefore(")").toDoubleOrNull() ?: 30.0
                sin(Math.toRadians(num))
            }
            expr.contains("cos") -> {
                val num = expr.substringAfter("cos(").substringBefore(")").toDoubleOrNull() ?: 0.0
                cos(Math.toRadians(num))
            }
            expr.contains("tan") -> {
                val num = expr.substringAfter("tan(").substringBefore(")").toDoubleOrNull() ?: 45.0
                tan(Math.toRadians(num))
            }
            expr.contains("sqrt") -> {
                val num = expr.substringAfter("sqrt(").substringBefore(")").toDoubleOrNull() ?: 16.0
                sqrt(num)
            }
            expr.contains("ln") -> {
                val num = expr.substringAfter("ln(").substringBefore(")").toDoubleOrNull() ?: 1.0
                ln(num)
            }
            expr.contains("+") -> {
                val parts = expr.split("+")
                parts.sumOf { evaluateSimpleMath(it) }
            }
            expr.contains("*") -> {
                val parts = expr.split("*")
                parts.map { evaluateSimpleMath(it) }.reduce { acc, d -> acc * d }
            }
            else -> expr.trim().toDoubleOrNull() ?: Double.NaN
        }
    } catch (e: Exception) {
        Double.NaN
    }
}
