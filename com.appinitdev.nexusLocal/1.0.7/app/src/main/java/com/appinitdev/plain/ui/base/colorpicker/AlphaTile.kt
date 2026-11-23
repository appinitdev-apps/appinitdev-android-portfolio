@Composable
public fun AlphaTile(
  modifier: Modifier,
  controller: ColorPickerController? = null,
  selectedColor: Color = Color.Transparent,
  tileOddColor: Color = defaultTileOddColor,
  tileEvenColor: Color = defaultTileEvenColor,
  tileSize: Dp = 12.dp,
) {
  val density = LocalDensity.current
  var backgroundBitmap: ImageBitmap? = null
  var bitmapSize = IntSize(0, 0)
  val colorPaint: Paint = Paint().apply {
    this.color = controller?.selectedColor?.value ?: selectedColor
  }
  Canvas(
    modifier = modifier
      .fillMaxSize()
      .onSizeChanged { newSize ->
        val size =
          newSize.takeIf { it.width != 0 && it.height != 0 } ?: return@onSizeChanged
        val drawable =
          AlphaTileDrawable(
            with(density) { tileSize.toPx() },
            tileOddColor,
            tileEvenColor,
          )
        backgroundBitmap
          ?.asAndroidBitmap()
          ?.recycle()
        backgroundBitmap =
          ImageBitmap(size.width, size.height, ImageBitmapConfig.Argb8888).apply {
            val backgroundCanvas = Canvas(this)
            drawable.setBounds(
              0,
              0,
              backgroundCanvas.nativeCanvas.width,
              backgroundCanvas.nativeCanvas.height,
            )
            drawable.draw(backgroundCanvas.nativeCanvas)
          }
        bitmapSize = size
      },
  ) {
    drawIntoCanvas { canvas ->
      backgroundBitmap?.let {
        canvas.drawImage(it, Offset.Zero, Paint())
        canvas.drawRect(
          0f,
          0f,
          bitmapSize.width.toFloat(),
          bitmapSize.height.toFloat(),
          colorPaint,
        )
      }
    }
  }
}
