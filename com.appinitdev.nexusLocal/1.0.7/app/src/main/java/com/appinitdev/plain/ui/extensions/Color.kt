@Composable
fun ColorScheme.surfaceColorAtElevation(
    elevation: Dp,
    color: Color = surface,
): Color = remember(this, elevation, color) { color.atElevation(surfaceTint, elevation) }
fun Color.atElevation(
    sourceColor: Color,
    elevation: Dp,
): Color {
    if (elevation == 0.dp) return this
    return sourceColor.copy(alpha = elevation.alphaLN(constant = 4.5f)).compositeOver(this)
}
fun Dp.alphaLN(
    constant: Float = 1f,
    weight: Float = 0f,
): Float = ((constant * ln(value + 1) + weight) + 2f) / 100f
