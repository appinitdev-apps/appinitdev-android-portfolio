
data class PathState(
    val path: Path,
    val color: Color,
    val strokeWidth: Float
)

fun createPathFromPoints(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path

    path.moveTo(points.first().x, points.first().y)
    if (points.size == 1) return path

    for (i in 1 until points.size) {
        val prev = points[i - 1]
        val curr = points[i]
        path.quadraticTo(
            prev.x,
            prev.y,
            (prev.x + curr.x) / 2f,
            (prev.y + curr.y) / 2f
        )
    }
    path.lineTo(points.last().x, points.last().y)
    return path
}

@Composable
fun DrawingCanvasDialog(
    onDismiss: () -> Unit,
    onDrawingFinished: (Bitmap) -> Unit
) {
    var paths by remember { mutableStateOf(listOf<PathState>()) }
    var currentPoints by remember { mutableStateOf(listOf<Offset>()) }
    var selectedColor by remember { mutableStateOf(Color.Red) }
    var strokeWidth by remember { mutableFloatStateOf(12f) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    val colors = listOf(
        Color.Red,
        Color(0xFF2196F3),
        Color(0xFF4CAF50),
        Color(0xFFFFEB3B),
        Color(0xFF9C27B0),
        Color(0xFFFF9800),
        Color.Black,
        Color.White
    )

    val strokeSizes = listOf(6f, 12f, 24f)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Brush,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.pinta_tu_puzzle),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                if (paths.isNotEmpty()) {
                                    paths = paths.dropLast(1)
                                }
                            },
                            enabled = paths.isNotEmpty()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Deshacer",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(onClick = { paths = emptyList(); currentPoints = emptyList() }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Limpiar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(2.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                        .onGloballyPositioned { coordinates ->
                            if (coordinates.size.width > 0 && coordinates.size.height > 0) {
                                canvasSize = coordinates.size.toSize()
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPoints = listOf(offset)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentPoints = currentPoints + change.position
                                },
                                onDragEnd = {
                                    if (currentPoints.isNotEmpty()) {
                                        val finishedPath = createPathFromPoints(currentPoints)
                                        paths = paths + PathState(finishedPath, selectedColor, strokeWidth)
                                        currentPoints = emptyList()
                                    }
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // 1. Dibuja trazos ya guardados
                        paths.forEach { pathState ->
                            drawPath(
                                path = pathState.path,
                                color = pathState.color,
                                style = Stroke(
                                    width = pathState.strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }

                        // 2. Dibuja el trazo actual EN TIEMPO REAL mientras mueves el dedo
                        if (currentPoints.size > 1) {
                            val activePath = createPathFromPoints(currentPoints)
                            drawPath(
                                path = activePath,
                                color = selectedColor,
                                style = Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        } else if (currentPoints.size == 1) {
                            drawCircle(
                                color = selectedColor,
                                radius = strokeWidth / 2f,
                                center = currentPoints.first()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Selector de colores y tamaño de pincel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        items(colors) { color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(color, CircleShape)
                                    .border(
                                        if (selectedColor == color) 3.dp else 1.dp,
                                        if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.Gray,
                                        CircleShape
                                    )
                                    .clickable { selectedColor = color }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Grosor de pincel
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        strokeSizes.forEach { size ->
                            val isSelected = strokeWidth == size
                            FilterChip(
                                selected = isSelected,
                                onClick = { strokeWidth = size },
                                label = {
                                    Text(
                                        text = when (size) {
                                            6f -> stringResource(R.string.fino)
                                            12f -> stringResource(R.string.medio)
                                            else -> stringResource(R.string.grueso)
                                        },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                shape = CircleShape
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val targetSize = 1000
                        val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(bitmap)
                        canvas.drawColor(android.graphics.Color.WHITE)

                        // Mapeo 1:1 hiper-preciso utilizando las dimensiones reales del canvas UI
                        val uiWidth = if (canvasSize.width > 0f) canvasSize.width else targetSize.toFloat()
                        val uiHeight = if (canvasSize.height > 0f) canvasSize.height else targetSize.toFloat()

                        val scaleX = targetSize.toFloat() / uiWidth
                        val scaleY = targetSize.toFloat() / uiHeight

                        val matrix = Matrix().apply {
                            setScale(scaleX, scaleY)
                        }

                        val paint = Paint().apply {
                            isAntiAlias = true
                            style = Paint.Style.STROKE
                            strokeCap = Paint.Cap.ROUND
                            strokeJoin = Paint.Join.ROUND
                        }

                        paths.forEach { pathState ->
                            paint.color = pathState.color.toArgb()
                            paint.strokeWidth = pathState.strokeWidth * scaleX

                            // Creamos una copia del path para transformar sin alterar la vista en pantalla
                            val androidPath = AndroidPath(pathState.path.asAndroidPath())
                            androidPath.transform(matrix)

                            canvas.drawPath(androidPath, paint)
                        }

                        onDrawingFinished(bitmap)
                        onDismiss()
                    },
                    shape = CircleShape,
                    modifier = Modifier.fillMaxWidth(0.85f),
                    enabled = paths.isNotEmpty()
                ) {
                    Text(stringResource(R.string.convertir_en_puzzle), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
