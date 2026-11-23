
@Composable
fun WhoPercentileChart(
    records: List<GrowthRecord>,
    modifier: Modifier = Modifier,
) {
    var selectedIndex by remember(records) { mutableStateOf<Int?>(null) }

    
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransitionWHO")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "PulseScaleWHO",
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "PulseAlphaWHO",
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
                        text = stringResource(id = R.string.curvas_percentiles_oms),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(id = R.string.franjas_oficiales_crecimiento),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = stringResource(id = R.string.p50_mediana),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            
            val activeIndex = selectedIndex
            if (activeIndex != null && (activeIndex in records.indices)) {
                val selRecord = records[activeIndex]
                val selDate = DateFormat.format("dd MMM, yyyy", Date(selRecord.timestamp)).toString()
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0288D1),
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
                            text = stringResource(id = R.string.medicion_numero_fecha, activeIndex + 1, selDate),
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f),
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.1f", selRecord.weightKg)} kg • ${selRecord.heightCm.toInt()} cm",
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
                    .height(170.dp),
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .pointerInput(records) {
                            detectTapGestures { tapOffset ->
                                if (records.isNotEmpty()) {
                                    val width = size.width
                                    val stepX = width / records.size.coerceAtLeast(1)
                                    val clickedIndex = ((tapOffset.x) / stepX).toInt().coerceIn(records.indices)
                                    selectedIndex = if (selectedIndex == clickedIndex) null else clickedIndex
                                }
                            }
                        },
                ) {
                    val width = size.width
                    val height = size.height

                    
                    val p97Y = height * 0.10f
                    val p85Y = height * 0.28f
                    val p50Y = height * 0.50f 
                    val p15Y = height * 0.72f
                    val p3Y = height * 0.90f

                    
                    drawLine(color = Color(0xFFEF4444).copy(alpha = 0.5f), start = Offset(0f, p97Y), end = Offset(width, p97Y), strokeWidth = 1.5.dp.toPx())
                    drawLine(color = Color(0xFFF59E0B).copy(alpha = 0.5f), start = Offset(0f, p85Y), end = Offset(width, p85Y), strokeWidth = 1.5.dp.toPx())
                    drawLine(color = Color(0xFF10B981), start = Offset(0f, p50Y), end = Offset(width, p50Y), strokeWidth = 2.5.dp.toPx()) 
                    drawLine(color = Color(0xFFF59E0B).copy(alpha = 0.5f), start = Offset(0f, p15Y), end = Offset(width, p15Y), strokeWidth = 1.5.dp.toPx())
                    drawLine(color = Color(0xFFEF4444).copy(alpha = 0.5f), start = Offset(0f, p3Y), end = Offset(width, p3Y), strokeWidth = 1.5.dp.toPx())

                    
                    if (records.isNotEmpty()) {
                        val weights = records.map { it.weightKg.toFloat() }
                        val minW = weights.minOrNull() ?: 0f
                        val maxW = weights.maxOrNull() ?: 10f
                        val rangeW = (maxW - minW).coerceAtLeast(1f)

                        val childPoints = mutableListOf<Offset>()
                        val stepX = width / records.size.coerceAtLeast(1)

                        for (i in records.indices) {
                            val x = (i + 0.5f) * stepX
                            val normY = (records[i].weightKg.toFloat() - minW) / rangeW
                            val y = height - (normY * (height * 0.70f)) - (height * 0.15f)
                            childPoints.add(Offset(x, y))
                        }

                        
                        val childPath = Path().apply {
                            if (childPoints.isNotEmpty()) {
                                moveTo(childPoints.first().x, childPoints.first().y)
                                for (p in childPoints.drop(1)) {
                                    lineTo(p.x, p.y)
                                }
                            }
                        }

                        drawPath(
                            path = childPath,
                            color = Color(0xFF0288D1),
                            style = Stroke(width = 3.8.dp.toPx(), cap = StrokeCap.Round),
                        )

                        
                        if (childPoints.isNotEmpty()) {
                            val lastPoint = childPoints.last()
                            drawCircle(
                                color = Color(0xFF0288D1).copy(alpha = pulseAlpha),
                                radius = 7.dp.toPx() * pulseScale,
                                center = lastPoint,
                            )
                        }

                        val labelStyle = TextStyle(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0288D1),
                        )

                        for (i in childPoints.indices) {
                            val p = childPoints[i]
                            val isSelected = (i == activeIndex)

                            if (isSelected) {
                                drawLine(
                                    color = Color(0xFF0288D1).copy(alpha = 0.4f),
                                    start = Offset(p.x, 0f),
                                    end = Offset(p.x, height),
                                    strokeWidth = 1.5.dp.toPx(),
                                )
                                drawCircle(
                                    color = Color(0xFF0288D1).copy(alpha = 0.3f),
                                    radius = 12.dp.toPx(),
                                    center = p,
                                )
                            }

                            drawCircle(color = Color.White, radius = if (isSelected) 8.dp.toPx() else 6.dp.toPx(), center = p)
                            drawCircle(color = if (isSelected) Color(0xFFFF9800) else Color(0xFF0288D1), radius = if (isSelected) 5.dp.toPx() else 4.dp.toPx(), center = p)

                            if (records.size <= 8 || isSelected || i == 0 || i == childPoints.lastIndex) {
                                val valueText = String.format(Locale.US, "%.1f", records[i].weightKg)
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
                }

                
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = "P97", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    Text(text = "P85", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                    Text(text = "P50 (OMS)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    Text(text = "P15", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                    Text(text = "P3", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                }
            }
        }
    }
}
