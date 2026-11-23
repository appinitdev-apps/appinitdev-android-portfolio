
@Composable
fun BmiGaugeView(
    bmiValue: Double,
    isBabyCategory: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Dynamic scaling: Babies 2.0 to 15.0 kg, Adults/Children 12.0 to 42.0 BMI
    val minScale = if (isBabyCategory) 2.0 else 12.0
    val maxScale = if (isBabyCategory) 15.0 else 42.0

    val clampedValue = bmiValue.coerceIn(minScale, maxScale)
    val fraction = ((clampedValue - minScale) / (maxScale - minScale)).toFloat()

    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "GaugeNeedleAnimation"
    )

    val segments = listOf(
        Pair(0.20f, UnderweightBlue),  // Underweight
        Pair(0.25f, NormalGreen),      // Normal
        Pair(0.20f, OverweightYellow), // Overweight
        Pair(0.15f, ObesityOrange),    // Obesity I
        Pair(0.10f, ObesityRed),       // Obesity II
        Pair(0.10f, ObesityPurple)     // Obesity III
    )

    val unitText = if (isBabyCategory) "kg" else "kg/m²"

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val strokeWidth = 26.dp.toPx()
                val radius = (canvasWidth.coerceAtMost(canvasHeight * 2) - strokeWidth - 48.dp.toPx()) / 2f
                val center = Offset(canvasWidth / 2f, canvasHeight - 16.dp.toPx())

                val arcSize = Size(radius * 2, radius * 2)
                val topLeft = Offset(center.x - radius, center.y - radius)

                var startAngle = 180f
                val totalSweep = 180f

                // Outer Background Shadow Arc
                drawArc(
                    color = Color.Black.copy(alpha = 0.12f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(topLeft.x, topLeft.y + 4.dp.toPx()),
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Colored Arc Segments
                for ((weight, color) in segments) {
                    val sweepAngle = totalSweep * weight
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle - 3f, // Clean gap
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += sweepAngle
                }

                // Needle Math
                val needleAngleRad = Math.toRadians((180f + (totalSweep * animatedFraction)).toDouble())
                val needleLength = radius - 10.dp.toPx()
                val needleEnd = Offset(
                    x = center.x + (needleLength * cos(needleAngleRad)).toFloat(),
                    y = center.y + (needleLength * sin(needleAngleRad)).toFloat()
                )

                // Sleek High-Contrast Pointer Needle
                val needlePath = Path().apply {
                    val perpAngle = needleAngleRad + Math.PI / 2
                    val baseWidth = 8.dp.toPx()
                    val leftBase = Offset(
                        (center.x + baseWidth * cos(perpAngle)).toFloat(),
                        (center.y + baseWidth * sin(perpAngle)).toFloat()
                    )
                    val rightBase = Offset(
                        (center.x - baseWidth * cos(perpAngle)).toFloat(),
                        (center.y - baseWidth * sin(perpAngle)).toFloat()
                    )

                    moveTo(leftBase.x, leftBase.y)
                    lineTo(needleEnd.x, needleEnd.y)
                    lineTo(rightBase.x, rightBase.y)
                    close()
                }

                drawPath(
                    path = needlePath,
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White, Color(0xFFE2E8F0))
                    )
                )

                // Needle Accent Line
                drawLine(
                    color = Color(0xFF0F172A),
                    start = center,
                    end = needleEnd,
                    strokeWidth = 2.dp.toPx()
                )

                // Center Pin Base with High Contrast
                drawCircle(
                    color = Color(0xFF0F172A),
                    radius = 16.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = NormalGreen,
                    radius = 4.dp.toPx(),
                    center = center
                )
            }

            // Display Score Positioned High Above Pin (Zero Overlap!)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 52.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = String.format("%.1f", bmiValue),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-1).sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = unitText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
