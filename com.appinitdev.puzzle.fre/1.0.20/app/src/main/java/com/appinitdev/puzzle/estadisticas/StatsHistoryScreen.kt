

@Composable
fun StatsHistoryScreen(
    stats: List<PuzzleStatEntity>,
    onDismiss: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Evolución, 1: Histograma
    val statsByLevel = stats.groupBy { it.n }.toSortedMap()

    // Resumen Global KPI
    val totalGames = stats.size
    val bestTime = stats.minOfOrNull { it.time } ?: 0
    val avgTime = if (totalGames > 0) stats.map { it.time }.average().toInt() else 0
    val avgMoves = if (totalGames > 0) stats.map { it.moves }.average().toInt() else 0

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.85f),
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cerrar)) }
        },
        title = {
            Text(stringResource(R.string.historial_de_progreso), style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column(modifier = Modifier.fillMaxSize()) {
                // KPIs de resumen
                if (totalGames > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            KpiItem(stringResource(R.string.completados), "$totalGames")
                            KpiItem(stringResource(R.string.mejor_tiempo), "${bestTime}s")
                            KpiItem(stringResource(R.string.prom_tiempo), "${avgTime}s")
                            KpiItem(stringResource(R.string.prom_movs), "$avgMoves")
                        }
                    }

                    // Tabs para cambiar vista
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text(stringResource(R.string.evolucion), fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text(stringResource(R.string.histograma), fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                // El LazyColumn hace que todo el contenido interno sea scroleable
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (stats.isEmpty()) {
                        item {
                            Text(stringResource(R.string.no_hay_datos_disponibles_a_n))
                        }
                    }

                    statsByLevel.forEach { (n, levelStats) ->
                        item {
                            Text(
                                text = stringResource(R.string.nivel) + " ${n}x${n}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (selectedTab == 0) {
                            // 1. Gráfica de mejora de línea
                            if (levelStats.size >= 2) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                                alpha = 0.5f
                                            )
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                stringResource(R.string.evoluci_n_de_tiempo_seg),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                            SimpleLineChart(
                                                stats = levelStats.sortedBy { it.date },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(120.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // 2. Histograma de distribución
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                            alpha = 0.5f
                                        )
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            stringResource(R.string.distribucion_de_tiempos),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        HistogramChart(
                                            stats = levelStats,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(130.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Lista detallada de ese nivel
                        items(levelStats.sortedByDescending { it.date }) { stat ->
                            HorizontalStatRow(stat)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun KpiItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun HistogramChart(stats: List<PuzzleStatEntity>, modifier: Modifier = Modifier) {
    val colorBar = MaterialTheme.colorScheme.primary
    val colorText = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f).toArgb()

    // Bins de tiempo: <30s, 30-60s, 60-120s, >120s
    val bin1 = stats.count { it.time < 30 }
    val bin2 = stats.count { it.time in 30..59 }
    val bin3 = stats.count { it.time in 60..119 }
    val bin4 = stats.count { it.time >= 120 }

    val counts = listOf(bin1, bin2, bin3, bin4)
    val labels = listOf("<30s", "30-60s", "1-2m", ">2m")
    val maxCount = counts.maxOrNull()?.coerceAtLeast(1) ?: 1

    Canvas(modifier = modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
        val barWidth = size.width / (counts.size * 2f)
        val textPaint = android.graphics.Paint().apply {
            color = colorText
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        counts.forEachIndexed { index, count ->
            val left = index * (barWidth * 2f) + (barWidth / 2f)
            val barHeight = (count.toFloat() / maxCount.toFloat()) * (size.height * 0.7f)
            val top = size.height - barHeight

            // Dibujar barra con esquinas redondeadas
            val cornerRadius = 8.dp.toPx()
            drawRoundRect(
                color = colorBar,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight.coerceAtLeast(4.dp.toPx())),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
            )

            // Cantidad encima
            if (count > 0) {
                drawContext.canvas.nativeCanvas.drawText(
                    "$count",
                    left + (barWidth / 2f),
                    top - 10f,
                    textPaint
                )
            }

            // Etiqueta abajo
            drawContext.canvas.nativeCanvas.drawText(
                labels[index],
                left + (barWidth / 2f),
                size.height + 30f,
                textPaint
            )
        }
    }
}

@Composable
fun SimpleLineChart(stats: List<PuzzleStatEntity>, modifier: Modifier = Modifier) {
    val colorLine = MaterialTheme.colorScheme.primary
    val colorDot = MaterialTheme.colorScheme.secondary
    val colorText = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f).toArgb()


    Canvas(modifier = modifier.padding(horizontal = 30.dp, vertical = 30.dp)) {
        if (stats.isEmpty()) return@Canvas

        // 1. Configuraciones iniciales y evitar división por cero
        val isSinglePoint = stats.size == 1
        val maxTime = stats.maxOf { it.time }.toFloat()
        val minTime = stats.minOf { it.time }.toFloat()

        // Si todos los tiempos son iguales, el rango es 1 para evitar división por cero
        val range = (maxTime - minTime).coerceAtLeast(1f)

        // Si hay un solo punto, lo ponemos al centro, si no, calculamos espacio
        val spacing = if (isSinglePoint) 0f else size.width / (stats.size - 1)

        val textPaint = android.graphics.Paint().apply {
            color = colorText
            textSize = 28f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        // 2. Cálculo de puntos
        val points = stats.mapIndexed { index, stat ->
            val x = if (isSinglePoint) size.width / 2 else index * spacing
            // Calculamos Y: El tiempo mínimo (mejor) estará más ARRIBA en el Canvas
            // Si quieres que el tiempo más alto esté arriba, quita el "size.height -"
            val y = if (isSinglePoint) size.height / 2
            else size.height - ((stat.time - minTime) / range * size.height)
            Offset(x, y)
        }

        // 3. Dibujar Guías de Fondo (Ejes)
        drawLine(
            color = Color.Gray.copy(alpha = 0.3f),
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 1.dp.toPx()
        )

        // 4. Dibujar la Línea (solo si hay más de un punto)
        if (!isSinglePoint) {
            val path = Path().apply {
                points.forEachIndexed { i, pt ->
                    if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                }
            }
            drawPath(
                path = path,
                color = colorLine,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // 5. Dibujar Puntos y Etiquetas
        points.forEachIndexed { index, pt ->
            // Sombra suave para el punto
            drawCircle(
                color = colorDot.copy(alpha = 0.2f),
                radius = 7.dp.toPx(),
                center = pt
            )

            // Punto principal
            drawCircle(
                color = colorDot,
                radius = 4.dp.toPx(),
                center = pt
            )

            // Etiqueta de Tiempo (Segundos) encima del punto
            drawContext.canvas.nativeCanvas.drawText(
                "${stats[index].time}s",
                pt.x,
                pt.y - 25f,
                textPaint
            )

            // Etiqueta de Fecha debajo (solo en extremos o si hay pocos puntos)
            if (isSinglePoint || index == 0 || index == stats.size - 1) {
                val dateLabel = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())
                    .format(java.util.Date(stats[index].date))

                drawContext.canvas.nativeCanvas.drawText(
                    dateLabel,
                    pt.x,
                    size.height + 40f,
                    textPaint.apply {
                        textSize = 22f
                        //fontWeight = Paint.FontMetrics().top // Estilo más ligero
                    }
                )
            }
        }
    }
}

@Composable
fun HorizontalStatRow(stat: PuzzleStatEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = SimpleDateFormat(
                    "dd/MM/yy HH:mm",
                    Locale.getDefault()
                ).format(Date(stat.date)),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text("${stat.moves} "+ stringResource(R.string.movimientos), style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = "${stat.time}s",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}