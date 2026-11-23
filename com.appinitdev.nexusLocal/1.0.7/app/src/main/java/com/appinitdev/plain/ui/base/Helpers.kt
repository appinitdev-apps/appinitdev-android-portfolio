
@Composable
fun measureTextWidth(text: String, style: TextStyle): Dp {
    val textMeasurer = rememberTextMeasurer()
    val widthInPixels = textMeasurer.measure(text, style).size.width
    return with(LocalDensity.current) { widthInPixels.toDp() }
}

@Composable
fun measureTextHeight(text: String, style: TextStyle): Dp {
    val textMeasurer = rememberTextMeasurer()
    val heightInPixels = textMeasurer.measure(text, style).size.height
    return with(LocalDensity.current) { heightInPixels.toDp() }
}