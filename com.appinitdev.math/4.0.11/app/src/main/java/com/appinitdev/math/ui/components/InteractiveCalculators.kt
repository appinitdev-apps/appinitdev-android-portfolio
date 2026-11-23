
@Composable
fun InteractiveCalculator(
    topicId: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when {
            topicId.contains("quadratic") -> QuadraticCalculator()
            topicId.contains("pythagorean") -> PythagoreanCalculator()
            topicId.contains("sine") || topicId.contains("cosine") || topicId.contains("trigonometry") -> TriangleCalculator()
            topicId.contains("complex") -> ComplexCalculator()
            topicId.contains("matrix") || topicId.contains("vector") -> MatrixCalculator()
            else -> GenericFunctionCalculator()
        }
    }
}

// 1. Quadratic Equation Calculator: ax^2 + bx + c = 0
@Composable
fun QuadraticCalculator() {
    var aStr by remember { mutableStateOf("1") }
    var bStr by remember { mutableStateOf("-5") }
    var cStr by remember { mutableStateOf("6") }

    val a = aStr.toDoubleOrNull() ?: 1.0
    val b = bStr.toDoubleOrNull() ?: 0.0
    val c = cStr.toDoubleOrNull() ?: 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🧮 Calculadora de Ecuación Cuadrática",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = aStr,
                    onValueChange = { aStr = it },
                    label = { Text("a (x²)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = bStr,
                    onValueChange = { bStr = it },
                    label = { Text("b (x)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = cStr,
                    onValueChange = { cStr = it },
                    label = { Text("c (indep.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Procedimiento y Resultados:",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (a == 0.0) {
                        if (b != 0.0) {
                            val x = -c / b
                            Text("• Ecuación Lineal (a = 0): ${b}x + ($c) = 0")
                            Text("• Solución Única: x = $x")
                        } else {
                            Text("• Ecuación Invalida (a = 0 y b = 0)")
                        }
                    } else {
                        val delta = b * b - 4 * a * c
                        val h = -b / (2 * a)
                        val k = a * h * h + b * h + c

                        Text("• Ecuación Estándar: (${a})x² + (${b})x + (${c}) = 0")
                        Text("• Discriminante (Δ = b² - 4ac):")
                        Text("   Δ = ($b)² - 4($a)($c) = $delta")

                        if (delta > 0) {
                            val x1 = (-b + sqrt(delta)) / (2 * a)
                            val x2 = (-b - sqrt(delta)) / (2 * a)
                            Text("• 2 Raíces Reales Distintas (Δ > 0):")
                            Text("   x₁ = (-($b) + √$delta) / (2·$a) = ${String.format(Locale.US, "%.4f", x1)}")
                            Text("   x₂ = (-($b) - √$delta) / (2·$a) = ${String.format(Locale.US, "%.4f", x2)}")
                        } else if (delta == 0.0) {
                            val x = -b / (2 * a)
                            Text("• 1 Raíz Reale Doble (Δ = 0):")
                            Text("   x₁ = x₂ = $x")
                        } else {
                            val realPart = -b / (2 * a)
                            val imagPart = sqrt(abs(delta)) / (2 * a)
                            Text("• 2 Raíces Complejas Conjugadas (Δ < 0):")
                            Text("   x₁ = ${String.format(Locale.US, "%.4f", realPart)} + ${String.format(Locale.US, "%.4f", imagPart)}i")
                            Text("   x₂ = ${String.format(Locale.US, "%.4f", realPart)} - ${String.format(Locale.US, "%.4f", imagPart)}i")
                        }

                        Text("• Vértice de la Parábola (h, k):")
                        Text("   (h = -b/2a, k = f(h)) = (${String.format(Locale.US, "%.4f", h)}, ${String.format(Locale.US, "%.4f", k)})")
                    }
                }
            }
        }
    }
}

// 2. Pythagorean Theorem Calculator
@Composable
fun PythagoreanCalculator() {
    var aStr by remember { mutableStateOf("3") }
    var bStr by remember { mutableStateOf("4") }

    val a = aStr.toDoubleOrNull() ?: 3.0
    val b = bStr.toDoubleOrNull() ?: 4.0
    val c = sqrt(a * a + b * b)

    val angleA = Math.toDegrees(asin(a / c))
    val angleB = 90.0 - angleA

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📐 Calculadora del Teorema de Pitágoras",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = aStr,
                    onValueChange = { aStr = it },
                    label = { Text("Cateto a") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = bStr,
                    onValueChange = { bStr = it },
                    label = { Text("Cateto b") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Resultados Precisos:",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Hipotenusa c = √(a² + b²) = √(${a*a} + ${b*b}) = ${String.format(Locale.US, "%.4f", c)}")
                    Text("• Ángulo α (opuesto a 'a'): ${String.format(Locale.US, "%.2f", angleA)}°")
                    Text("• Ángulo β (opuesto a 'b'): ${String.format(Locale.US, "%.2f", angleB)}°")
                    Text("• Área del Triángulo = (a · b) / 2 = ${String.format(Locale.US, "%.2f", (a * b) / 2.0)}")
                    Text("• Perímetro = a + b + c = ${String.format(Locale.US, "%.2f", a + b + c)}")
                }
            }
        }
    }
}

// 3. Triangle Law of Sines & Cosines Solver
@Composable
fun TriangleCalculator() {
    var aStr by remember { mutableStateOf("5") }
    var bStr by remember { mutableStateOf("7") }
    var cStr by remember { mutableStateOf("8") }

    val a = aStr.toDoubleOrNull() ?: 5.0
    val b = bStr.toDoubleOrNull() ?: 7.0
    val c = cStr.toDoubleOrNull() ?: 8.0

    val isValidTriangle = (a + b > c) && (a + c > b) && (b + c > a)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔺 Resolución de Triángulos (Ley de Senos y Cosenos)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = aStr,
                    onValueChange = { aStr = it },
                    label = { Text("Lado a") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = bStr,
                    onValueChange = { bStr = it },
                    label = { Text("Lado b") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = cStr,
                    onValueChange = { cStr = it },
                    label = { Text("Lado c") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (isValidTriangle) {
                        val cosA = ((b * b + c * c - a * a) / (2 * b * c)).coerceIn(-1.0, 1.0)
                        val angleA = Math.toDegrees(acos(cosA))

                        val cosB = ((a * a + c * c - b * b) / (2 * a * c)).coerceIn(-1.0, 1.0)
                        val angleB = Math.toDegrees(acos(cosB))

                        val angleC = 180.0 - angleA - angleB
                        val s = (a + b + c) / 2.0
                        val area = sqrt((s * (s - a) * (s - b) * (s - c)).coerceAtLeast(0.0))
                        val circumradius = (a * b * c) / (4 * area)

                        Text("• Ángulo α (opuesto a 'a'): ${String.format(Locale.US, "%.2f", angleA)}°")
                        Text("• Ángulo β (opuesto a 'b'): ${String.format(Locale.US, "%.2f", angleB)}°")
                        Text("• Ángulo γ (opuesto a 'c'): ${String.format(Locale.US, "%.2f", angleC)}°")
                        Text("• Área (Fórmula de Herón): ${String.format(Locale.US, "%.4f", area)}")
                        Text("• Radio del Círculo Circunscrito (R): ${String.format(Locale.US, "%.4f", circumradius)}")
                    } else {
                        Text("⚠️ Los tres lados no cumplen la desigualdad triangular (a + b > c).")
                    }
                }
            }
        }
    }
}

// 4. Complex Number Calculator
@Composable
fun ComplexCalculator() {
    var real1Str by remember { mutableStateOf("3") }
    var imag1Str by remember { mutableStateOf("4") }
    var real2Str by remember { mutableStateOf("1") }
    var imag2Str by remember { mutableStateOf("2") }

    val r1 = real1Str.toDoubleOrNull() ?: 3.0
    val i1 = imag1Str.toDoubleOrNull() ?: 4.0
    val r2 = real2Str.toDoubleOrNull() ?: 1.0
    val i2 = imag2Str.toDoubleOrNull() ?: 2.0

    val mod1 = sqrt(r1 * r1 + i1 * i1)
    val argRad1 = atan2(i1, r1)
    val argDeg1 = Math.toDegrees(argRad1)

    // Operations z1 + z2, z1 * z2, z1 / z2
    val sumR = r1 + r2
    val sumI = i1 + i2

    val prodR = r1 * r2 - i1 * i2
    val prodI = r1 * i2 + i1 * r2

    val divDenom = r2 * r2 + i2 * i2
    val divR = if (divDenom != 0.0) (r1 * r2 + i1 * i2) / divDenom else Double.NaN
    val divI = if (divDenom != 0.0) (i1 * r2 - r1 * i2) / divDenom else Double.NaN

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🌀 Operaciones con Números Complejos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text("Número Complejo z₁:", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = real1Str,
                    onValueChange = { real1Str = it },
                    label = { Text("Real a₁") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = imag1Str,
                    onValueChange = { imag1Str = it },
                    label = { Text("Imag. b₁") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Número Complejo z₂:", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = real2Str,
                    onValueChange = { real2Str = it },
                    label = { Text("Real a₂") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = imag2Str,
                    onValueChange = { imag2Str = it },
                    label = { Text("Imag. b₂") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("• Propiedades de z₁ = $r1 + ${i1}i:")
                    Text("   - Módulo |z₁| = ${String.format(Locale.US, "%.4f", mod1)}")
                    Text("   - Argumento Arg(z₁) = ${String.format(Locale.US, "%.2f", argDeg1)}° (${String.format(Locale.US, "%.4f", argRad1)} rad)")
                    Text("   - Forma Polar z₁ = ${String.format(Locale.US, "%.2f", mod1)} e^(${String.format(Locale.US, "%.2f", argRad1)}i)")
                    Text("• Suma z₁ + z₂ = $sumR + ${sumI}i")
                    Text("• Producto z₁ · z₂ = $prodR + ${prodI}i")
                    if (!divR.isNaN()) {
                        Text("• Cociente z₁ / z₂ = ${String.format(Locale.US, "%.4f", divR)} + ${String.format(Locale.US, "%.4f", divI)}i")
                    } else {
                        Text("• Cociente z₁ / z₂: Indefinido (división por 0)")
                    }
                }
            }
        }
    }
}

// 5. Matrix 2x2 Calculator
@Composable
fun MatrixCalculator() {
    var aStr by remember { mutableStateOf("1") }
    var bStr by remember { mutableStateOf("2") }
    var cStr by remember { mutableStateOf("3") }
    var dStr by remember { mutableStateOf("4") }

    val a = aStr.toDoubleOrNull() ?: 1.0
    val b = bStr.toDoubleOrNull() ?: 2.0
    val c = cStr.toDoubleOrNull() ?: 3.0
    val d = dStr.toDoubleOrNull() ?: 4.0

    val det = a * d - b * c
    val trace = a + d
    val eigenDisc = trace * trace - 4 * det

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔢 Calculadora de Matriz 2x2 [a  b; c  d]",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = aStr,
                    onValueChange = { aStr = it },
                    label = { Text("a₁₁") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = bStr,
                    onValueChange = { bStr = it },
                    label = { Text("a₁₂") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = cStr,
                    onValueChange = { cStr = it },
                    label = { Text("a₂₁") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = dStr,
                    onValueChange = { dStr = it },
                    label = { Text("a₂₂") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("• Determinante det(A) = ad - bc = ($a · $d) - ($b · $c) = $det")
                    Text("• Traza tr(A) = a + d = $trace")

                    if (eigenDisc >= 0) {
                        val l1 = (trace + sqrt(eigenDisc)) / 2.0
                        val l2 = (trace - sqrt(eigenDisc)) / 2.0
                        Text("• Autovalores (Eigenvalues λ):")
                        Text("   λ₁ = ${String.format(Locale.US, "%.4f", l1)}")
                        Text("   λ₂ = ${String.format(Locale.US, "%.4f", l2)}")
                    } else {
                        val realL = trace / 2.0
                        val imagL = sqrt(abs(eigenDisc)) / 2.0
                        Text("• Autovalores Complejos λ = ${String.format(Locale.US, "%.4f", realL)} ± ${String.format(Locale.US, "%.4f", imagL)}i")
                    }

                    if (det != 0.0) {
                        Text("• Matriz Inversa A⁻¹:")
                        Text("   [ ${String.format(Locale.US, "%.4f", d/det)}   ${String.format(Locale.US, "%.4f", -b/det)} ]")
                        Text("   [ ${String.format(Locale.US, "%.4f", -c/det)}   ${String.format(Locale.US, "%.4f", a/det)} ]")
                    } else {
                        Text("• Matriz Singular (no invertible det = 0)")
                    }
                }
            }
        }
    }
}

// 6. Generic Evaluator Calculator
@Composable
fun GenericFunctionCalculator() {
    var xStr by remember { mutableStateOf("2") }
    val x = xStr.toDoubleOrNull() ?: 2.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚡ Evaluador Rápido de Funciones en x = $x",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = xStr,
                onValueChange = { xStr = it },
                label = { Text("Valor de x") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("• f(x) = x² = ${x * x}")
                    Text("• f(x) = √x = ${if (x >= 0) String.format(Locale.US, "%.4f", sqrt(x)) else "No real"}")
                    Text("• f(x) = e^x = ${String.format(Locale.US, "%.4f", Math.exp(x))}")
                    Text("• f(x) = ln(x) = ${if (x > 0) String.format(Locale.US, "%.4f", Math.log(x)) else "Indefinido"}")
                    Text("• f(x) = sin(x) = ${String.format(Locale.US, "%.4f", sin(x))}")
                    Text("• f(x) = cos(x) = ${String.format(Locale.US, "%.4f", cos(x))}")
                }
            }
        }
    }
}
