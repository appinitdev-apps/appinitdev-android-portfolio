
private data class TrendBadgeInfo(
    val bg: Color,
    val fg: Color,
    val icon: ImageVector,
    val text: String,
)

@Composable
fun WeightEvolutionChart(
    records: List<GrowthRecord>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
) {
    var selectedIndex by remember(records) { mutableStateOf<Int?>(null) }

    
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransitionWeight")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "PulseScaleWeight",
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "PulseAlphaWeight",
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
                        text = stringResource(id = R.string.evolucion_de_peso),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(id = R.string.mediciones_registradas, records.size),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    
                    if (records.size >= 2) {
                        val first = records.first().weightKg
                        val last = records.last().weightKg
                        val diff = last - first

                        val badgeInfo = when {
                            diff < -0.05 -> TrendBadgeInfo(
                                Color(0xFF10B981).copy(alpha = 0.15f),
                                Color(0xFF059669),
                                Icons.Default.ArrowDownward,
                                String.format(Locale.US, "%.1f kg", diff),
                            )
                            diff > 0.05 -> TrendBadgeInfo(
                                Color(0xFFF59E0B).copy(alpha = 0.15f),
                                Color(0xFFD97706),
                                Icons.Default.ArrowUpward,
                                String.format(Locale.US, "+%.1f kg", diff),
                            )
                            else -> TrendBadgeInfo(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.onSurfaceVariant,
                                Icons.Default.Remove,
                                "0.0 kg",
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = badgeInfo.bg,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = badgeInfo.icon,
                                    contentDescription = null,
                                    tint = badgeInfo.fg,
                                    modifier = Modifier.size(13.dp),
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = badgeInfo.text,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeInfo.fg,
                                )
                            }
                        }
                    }

                    
                    if (records.isNotEmpty()) {
                        val latest = records.last().weightKg
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = lineColor.copy(alpha = 0.15f),
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1f kg", latest),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = lineColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }

            if (records.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(id = R.string.registra_al_menos_2_mediciones_evolucion),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            } else {
                val weights = records.map { it.weightKg.toFloat() }
                val minWeight = weights.minOrNull() ?: 0f
                val maxWeight = weights.maxOrNull() ?: 10f
                val range = (maxWeight - minWeight).coerceAtLeast(1f)

                val dates = records.map {
                    DateFormat.format("dd/MM", Date(it.timestamp)).toString()
                }

                val labelStyle = TextStyle(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = lineColor,
                )

                
                val activeIndex = selectedIndex
                if (activeIndex != null && (activeIndex in records.indices)) {
                    val selRecord = records[activeIndex]
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
                                text = stringResource(id = R.string.medicion_numero_fecha, activeIndex + 1, selDate),
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f),
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.1f", selRecord.weightKg)} kg",
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
                            .pointerInput(records) {
                                detectTapGestures { tapOffset ->
                                    val width = size.width
                                    val stepX = width / (records.size - 1).coerceAtLeast(1)
                                    val clickedIndex = ((tapOffset.x + (stepX / 2f)) / stepX).toInt().coerceIn(records.indices)
                                    selectedIndex = if (selectedIndex == clickedIndex) null else clickedIndex
                                }
                            },
                    ) {
                        val width = size.width
                        val height = size.height
                        val points = mutableListOf<Offset>()

                        val stepX = width / (records.size - 1).coerceAtLeast(1)

                        for (i in records.indices) {
                            val x = i * stepX
                            val normalizedY = (records[i].weightKg.toFloat() - minWeight) / range
                            val y = height - (normalizedY * (height * 0.65f)) - (height * 0.18f)
                            points.add(Offset(x, y))
                        }

                        
                        val gridCount = 3
                        for (g in 0..gridCount) {
                            val gy = height * (g / gridCount.toFloat())
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.15f),
                                start = Offset(0f, gy),
                                end = Offset(width, gy),
                                strokeWidth = 1.dp.toPx(),
                            )
                        }

                        
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

                        
                        if (points.isNotEmpty()) {
                            val lastPoint = points.last()
                            drawCircle(
                                color = lineColor.copy(alpha = pulseAlpha),
                                radius = 7.dp.toPx() * pulseScale,
                                center = lastPoint,
                            )
                        }

                        
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

                            drawCircle(
                                color = Color.White,
                                radius = if (isSelected) 8.dp.toPx() else 6.dp.toPx(),
                                center = p,
                            )
                            drawCircle(
                                color = if (isSelected) Color(0xFFFF9800) else lineColor,
                                radius = if (isSelected) 5.dp.toPx() else 4.dp.toPx(),
                                center = p,
                            )

                            
                            if (records.size <= 8 || isSelected || i == 0 || i == points.lastIndex) {
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

@Composable
fun HeightEvolutionChart(
    records: List<GrowthRecord>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF0288D1),
) {
    var selectedIndex by remember(records) { mutableStateOf<Int?>(null) }

    
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransitionHeight")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "PulseScaleHeight",
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "PulseAlphaHeight",
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
                        text = stringResource(id = R.string.evolucion_de_talla_estatura),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(id = R.string.crecimiento_acumulado_cm),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    
                    if (records.size >= 2) {
                        val first = records.first().heightCm
                        val last = records.last().heightCm
                        val diff = last - first

                        if (diff > 0.1) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0288D1).copy(alpha = 0.15f),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = Color(0xFF0288D1),
                                        modifier = Modifier.size(13.dp),
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = String.format(Locale.US, "+%.0f cm", diff),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0288D1),
                                    )
                                }
                            }
                        }
                    }

                    
                    if (records.isNotEmpty()) {
                        val latest = records.last().heightCm
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = lineColor.copy(alpha = 0.15f),
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.0f cm", latest),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = lineColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }

            if (records.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(id = R.string.registra_al_menos_2_mediciones_crecimiento),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            } else {
                val heights = records.map { it.heightCm.toFloat() }
                val minHeight = heights.minOrNull() ?: 0f
                val maxHeight = heights.maxOrNull() ?: 100f
                val range = (maxHeight - minHeight).coerceAtLeast(1f)

                val dates = records.map {
                    DateFormat.format("dd/MM", Date(it.timestamp)).toString()
                }

                val labelStyle = TextStyle(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = lineColor,
                )

                
                val activeIndex = selectedIndex
                if (activeIndex != null && (activeIndex in records.indices)) {
                    val selRecord = records[activeIndex]
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
                                text = stringResource(id = R.string.medicion_numero_fecha, activeIndex + 1, selDate),
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f),
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.0f", selRecord.heightCm)} cm",
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
                            .pointerInput(records) {
                                detectTapGestures { tapOffset ->
                                    val width = size.width
                                    val stepX = width / (records.size - 1).coerceAtLeast(1)
                                    val clickedIndex = ((tapOffset.x + (stepX / 2f)) / stepX).toInt().coerceIn(records.indices)
                                    selectedIndex = if (selectedIndex == clickedIndex) null else clickedIndex
                                }
                            },
                    ) {
                        val width = size.width
                        val canvasHeight = size.height
                        val points = mutableListOf<Offset>()

                        val stepX = width / (records.size - 1).coerceAtLeast(1)

                        for (i in records.indices) {
                            val x = i * stepX
                            val normalizedY = (records[i].heightCm.toFloat() - minHeight) / range
                            val y = canvasHeight - (normalizedY * (canvasHeight * 0.65f)) - (canvasHeight * 0.18f)
                            points.add(Offset(x, y))
                        }

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

                        
                        if (points.isNotEmpty()) {
                            val lastPoint = points.last()
                            drawCircle(
                                color = lineColor.copy(alpha = pulseAlpha),
                                radius = 7.dp.toPx() * pulseScale,
                                center = lastPoint,
                            )
                        }

                        for (i in points.indices) {
                            val p = points[i]
                            val isSelected = (i == activeIndex)

                            if (isSelected) {
                                drawLine(
                                    color = lineColor.copy(alpha = 0.4f),
                                    start = Offset(p.x, 0f),
                                    end = Offset(p.x, canvasHeight),
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

                            if (records.size <= 8 || isSelected || i == 0 || i == points.lastIndex) {
                                val valueText = String.format(Locale.US, "%.0f", records[i].heightCm)
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
