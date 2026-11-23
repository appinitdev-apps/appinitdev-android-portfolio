
@Composable
fun GenericMetricChart(
    title: String,
    measurements: List<ProfileMeasurement>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
) {
    var selectedIndex by remember(measurements) { mutableStateOf<Int?>(null) }

    // Pulsing Infinite Animation for Latest Data Point
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransitionMetric")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "PulseScaleMetric",
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "PulseAlphaMetric",
    )

    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${measurements.size} ${stringResource(id = R.string.registros)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (measurements.isNotEmpty()) {
                    val latest = measurements.last()
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = lineColor.copy(alpha = 0.15f),
                    ) {
                        Text(
                            text = "${String.format(Locale.US, "%.1f", latest.valueNumeric)} ${latest.unitLabel}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = lineColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            if (measurements.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(id = R.string.registra_al_menos_2_mediciones_tendencia),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            } else {
                val values = measurements.map { it.valueNumeric.toFloat() }
                val minVal = values.minOrNull() ?: 0f
                val maxVal = values.maxOrNull() ?: 100f
                val rangeVal = (maxVal - minVal).coerceAtLeast(1f)

                val dates = measurements.map {
                    DateFormat.format("dd/MM", Date(it.timestamp)).toString()
                }

                val labelStyle = TextStyle(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = lineColor,
                )

                // Interactive Selected Point Tooltip Box
                val activeIndex = selectedIndex
                if (activeIndex != null && (activeIndex in measurements.indices)) {
                    val selRecord = measurements[activeIndex]
                    val selDate = DateFormat.format("dd MMM, yyyy", Date(selRecord.timestamp)).toString()
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = lineColor,
                        shadowElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(id = R.string.registro_numero_fecha, activeIndex + 1, selDate),
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f),
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.1f", selRecord.valueNumeric)} ${selRecord.unitLabel}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .pointerInput(measurements) {
                                detectTapGestures { tapOffset ->
                                    val width = size.width
                                    val stepX = width / (measurements.size - 1).coerceAtLeast(1)
                                    val clickedIndex = ((tapOffset.x + (stepX / 2f)) / stepX).toInt().coerceIn(measurements.indices)
                                    selectedIndex = if (selectedIndex == clickedIndex) null else clickedIndex
                                }
                            },
                    ) {
                        val width = size.width
                        val height = size.height
                        val points = mutableListOf<Offset>()

                        val stepX = width / (measurements.size - 1).coerceAtLeast(1)

                        for (i in measurements.indices) {
                            val x = i * stepX
                            val normalizedY = (measurements[i].valueNumeric.toFloat() - minVal) / rangeVal
                            val y = height - (normalizedY * (height * 0.65f)) - (height * 0.18f)
                            points.add(Offset(x, y))
                        }

                        // Grid lines
                        for (g in 0..3) {
                            val gy = height * (g / 3f)
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.15f),
                                start = Offset(0f, gy),
                                end = Offset(width, gy),
                                strokeWidth = 1.dp.toPx(),
                            )
                        }

                        // Gradient Area Path
                        val areaPath = Path().apply {
                            if (points.isNotEmpty()) {
                                moveTo(points.first().x, height)
                                lineTo(points.first().x, points.first().y)
                                for (p in points.drop(1)) {
                                    lineTo(p.x, p.y)
                                }
                                lineTo(points.last().x, height)
                                close()
                            }
                        }

                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    lineColor.copy(alpha = 0.38f),
                                    lineColor.copy(alpha = 0.02f),
                                ),
                            ),
                        )

                        // Line
                        val linePath = Path().apply {
                            if (points.isNotEmpty()) {
                                moveTo(points.first().x, points.first().y)
                                for (p in points.drop(1)) {
                                    lineTo(p.x, p.y)
                                }
                            }
                        }

                        drawPath(
                            path = linePath,
                            color = lineColor,
                            style = Stroke(width = 3.8.dp.toPx(), cap = StrokeCap.Round),
                        )

                        // Live Pulse Effect on the Last Point
                        if (points.isNotEmpty()) {
                            val lastPoint = points.last()
                            drawCircle(
                                color = lineColor.copy(alpha = pulseAlpha),
                                radius = 7.dp.toPx() * pulseScale,
                                center = lastPoint,
                            )
                        }

                        // Dots & Labels
                        for (i in points.indices) {
                            val p = points[i]
                            val isSelected = (i == activeIndex)

                            if (isSelected) {
                                drawLine(
                                    color = lineColor.copy(alpha = 0.4f),
                                    start = Offset(p.x, 0f),
                                    end = Offset(p.x, height),
                                    strokeWidth = 1.5.dp.toPx(),
                                )
                                drawCircle(
                                    color = lineColor.copy(alpha = 0.3f),
                                    radius = 12.dp.toPx(),
                                    center = p,
                                )
                            }

                            drawCircle(color = Color.White, radius = if (isSelected) 8.dp.toPx() else 6.dp.toPx(), center = p)
                            drawCircle(color = if (isSelected) Color(0xFFFF9800) else lineColor, radius = if (isSelected) 5.dp.toPx() else 4.dp.toPx(), center = p)

                            if (measurements.size <= 8 || isSelected || i == 0 || i == points.lastIndex) {
                                val valueText = String.format(Locale.US, "%.1f", measurements[i].valueNumeric)
                                val textLayoutResult = textMeasurer.measure(
                                    text = valueText,
                                    style = labelStyle,
                                )
                                val labelX = (p.x - textLayoutResult.size.width / 2f).coerceIn(0f, width - textLayoutResult.size.width)
                                val labelY = (p.y - 18.dp.toPx()).coerceAtLeast(0f)

                                drawText(
                                    textLayoutResult = textLayoutResult,
                                    topLeft = Offset(labelX, labelY),
                                )
                            }
                        }
                    }

                    // Dates X-Axis Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(top = 144.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(text = dates.firstOrNull() ?: "", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (dates.size > 2) {
                            Text(text = dates[dates.size / 2], fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(text = dates.lastOrNull() ?: "", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
