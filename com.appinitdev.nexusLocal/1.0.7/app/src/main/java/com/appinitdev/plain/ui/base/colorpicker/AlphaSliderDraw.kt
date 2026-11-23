internal fun Canvas.drawAlphaSliderContent(
    backgroundBitmap: ImageBitmap,
    bitmapSize: IntSize,
    controller: ColorPickerController,
    colorPaint: Paint,
    borderRadius: Dp,
    wheelImageBitmap: ImageBitmap?,
    wheelRadius: Dp,
    wheelPaint: Paint,
    density: Float,
) {
    drawImage(backgroundBitmap, Offset.Zero, Paint())
    val startColor = controller.pureSelectedColor.value.copy(alpha = 0f)
    val endColor = controller.pureSelectedColor.value.copy(alpha = 1f)
    val shader = LinearGradientShader(
        colors = listOf(startColor, endColor),
        from = Offset.Zero,
        to = Offset(bitmapSize.width.toFloat(), bitmapSize.height.toFloat()),
        tileMode = TileMode.Clamp,
    )
    colorPaint.shader = shader
    drawRoundRect(
        left = 0f,
        top = 0f,
        right = bitmapSize.width.toFloat(),
        bottom = bitmapSize.height.toFloat(),
        radiusX = borderRadius.value,
        radiusY = borderRadius.value,
        paint = colorPaint,
    )
    val position = controller.alpha.value
    val point = (bitmapSize.width * position).coerceIn(
        minimumValue = 0f,
        maximumValue = bitmapSize.width.toFloat(),
    )
    if (wheelImageBitmap == null) {
        drawCircle(
            Offset(x = point, y = bitmapSize.height / 2f),
            wheelRadius.value * density,
            wheelPaint,
        )
    } else {
        drawImage(
            wheelImageBitmap,
            Offset(
                x = point - (wheelImageBitmap.width / 2),
                y = bitmapSize.height / 2f - wheelImageBitmap.height / 2,
            ),
            Paint(),
        )
    }
}
