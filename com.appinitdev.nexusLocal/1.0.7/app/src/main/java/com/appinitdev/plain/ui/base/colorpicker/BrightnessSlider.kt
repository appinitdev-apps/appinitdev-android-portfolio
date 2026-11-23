
@Composable
public fun BrightnessSlider(
    modifier: Modifier = Modifier,
    controller: ColorPickerController,
    borderRadius: Dp = 6.dp,
    borderSize: Dp = 5.dp,
    borderColor: Color = Color.LightGray,
    wheelImageBitmap: ImageBitmap? = null,
    wheelRadius: Dp = 12.dp,
    wheelColor: Color = Color.White,
    @FloatRange(from = 0.0, to = 1.0) wheelAlpha: Float = 1.0f,
    wheelPaint: Paint = Paint().apply {
        color = wheelColor
        alpha = wheelAlpha
    },
    initialColor: Color? = null,
) {
    var backgroundBitmap: ImageBitmap? = null
    var bitmapSize = IntSize(0, 0)
    val borderPaint: Paint = Paint().apply {
        style = PaintingStyle.Stroke
        strokeWidth = with(LocalDensity.current) { borderSize.toPx() }
        color = borderColor
    }
    val colorPaint: Paint = Paint().apply {
        color = controller.pureSelectedColor.value
    }
    var isInitialized by remember { mutableStateOf(false) }

    SideEffect {
        controller.isAttachedBrightnessSlider = true
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(borderRadius))
            .onSizeChanged { newSize ->
                val size = newSize.takeIf { it.width != 0 && it.height != 0 } ?: return@onSizeChanged
                backgroundBitmap?.asAndroidBitmap()?.recycle()
                backgroundBitmap = ImageBitmap(size.width, size.height, ImageBitmapConfig.Argb8888).apply {
                    val backgroundCanvas = Canvas(this)
                    backgroundCanvas.drawRoundRect(
                        left = 0f, top = 0f, right = size.width.toFloat(), bottom = size.height.toFloat(),
                        radiusX = borderRadius.value, radiusY = borderRadius.value, paint = borderPaint,
                    )
                }
                bitmapSize = size
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val point = offset.x.coerceIn(0f, bitmapSize.width.toFloat())
                    controller.setBrightness((point / bitmapSize.width).coerceIn(0f, 1f), fromUser = true)
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    val point = change.position.x.coerceIn(0f, bitmapSize.width.toFloat())
                    controller.setBrightness((point / bitmapSize.width).coerceIn(0f, 1f), fromUser = true)
                }
            },
    ) {
        drawIntoCanvas { canvas ->
            backgroundBitmap?.let {
                canvas.drawBrightnessSliderContent(
                    backgroundBitmap = it, bitmapSize = bitmapSize,
                    controller = controller, colorPaint = colorPaint,
                    borderRadius = borderRadius, wheelImageBitmap = wheelImageBitmap,
                    wheelRadius = wheelRadius, wheelPaint = wheelPaint,
                    density = wheelRadius.toPx() / wheelRadius.value,
                )
                if (initialColor != null && !isInitialized) {
                    isInitialized = true
                    val brightness = max(max(initialColor.red, initialColor.green), initialColor.blue)
                    controller.setBrightness(brightness, fromUser = false)
                }
            }
        }
    }
}
