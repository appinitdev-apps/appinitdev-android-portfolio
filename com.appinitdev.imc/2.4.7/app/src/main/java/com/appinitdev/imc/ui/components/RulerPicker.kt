
@Composable
fun InteractiveRulerPicker(
    title: String,
    value: Double,
    unitLabel: String,
    range: ClosedRange<Double>,
    step: Double = 0.5,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val density = LocalDensity.current
    val lineSpacingPx = remember(density) { with(density) { 16.dp.toPx() } }

    val currentValue by rememberUpdatedState(value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    var accumulatedValue by remember(range) { mutableDoubleStateOf(value) }

    LaunchedEffect(value) {
        if (abs(value - accumulatedValue) > 0.5) {
            accumulatedValue = value
        }
    }

    val draggableState = rememberDraggableState { deltaPx ->
        val deltaUnits = -deltaPx * (step / lineSpacingPx)
        val newRaw = (accumulatedValue + deltaUnits).coerceIn(range.start, range.endInclusive)
        accumulatedValue = newRaw

        val snapped = (newRaw / step).roundToInt() * step
        val rounded = (snapped * 10).roundToInt() / 10.0
        if (rounded != currentValue) {
            currentOnValueChange(rounded)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Title & Unit Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = unitLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Display: Large Number + Quick Increments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val newValue = (value - step).coerceIn(range.start, range.endInclusive)
                        val rounded = (newValue * 10).roundToInt() / 10.0
                        accumulatedValue = rounded
                        onValueChange(rounded)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = String.format("%.1f", value),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = unitLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                IconButton(
                    onClick = {
                        val newValue = (value + step).coerceIn(range.start, range.endInclusive)
                        val rounded = (newValue * 10).roundToInt() / 10.0
                        accumulatedValue = rounded
                        onValueChange(rounded)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Ruler
            val surfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            val onSurfaceColor = MaterialTheme.colorScheme.onSurface
            val containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(
                        color = containerColor,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .draggable(
                        state = draggableState,
                        orientation = Orientation.Horizontal
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val centerX = width / 2f

                    val visibleTicks = (centerX / lineSpacingPx).toInt() + 3

                    val centerTickIndex = (accumulatedValue / step).roundToInt()
                    val valueOffset = accumulatedValue - (centerTickIndex * step)
                    val pxOffset = (valueOffset / step) * lineSpacingPx

                    for (i in -visibleTicks..visibleTicks) {
                        val tickIndex = centerTickIndex + i
                        val tickValue = tickIndex * step

                        if (tickValue in range) {
                            val x = centerX + (i * lineSpacingPx) - pxOffset.toFloat()
                            if (x in 0f..width) {
                                val isMajor = (tickValue * 2).roundToInt() % 20 == 0
                                val isHalf = (tickValue * 2).roundToInt() % 2 == 0

                                val lineLength = when {
                                    isMajor -> height * 0.65f
                                    isHalf -> height * 0.45f
                                    else -> height * 0.30f
                                }

                                val strokeWidth = if (isMajor) 3.dp.toPx() else 1.5.dp.toPx()
                                val color = if (isMajor) onSurfaceColor else surfaceVariantColor

                                drawLine(
                                    color = color,
                                    start = Offset(x, height),
                                    end = Offset(x, height - lineLength),
                                    strokeWidth = strokeWidth
                                )
                            }
                        }
                    }

                    // Indicador Central Fijo (Aguja con Gradiente)
                    val indicatorWidth = 4.dp.toPx()
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(accentColor, accentColor.copy(alpha = 0.5f))
                        ),
                        start = Offset(centerX, height),
                        end = Offset(centerX, 0f),
                        strokeWidth = indicatorWidth
                    )
                    drawCircle(
                        color = accentColor,
                        radius = 6.dp.toPx(),
                        center = Offset(centerX, height)
                    )
                }

                // Edge Fade Gradient Overlay for smooth 3D viewport effect
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                    Color.Transparent,
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
            }
        }
    }
}
