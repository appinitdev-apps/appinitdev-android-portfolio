val Configuration.isPortrait: Boolean
    get() = this.orientation == Configuration.ORIENTATION_PORTRAIT
val Configuration.screenWidth: Dp
    get() = if (isPortrait) screenWidthDp.dp else screenHeightDp.dp
val Configuration.screenHeight: Dp
    get() = if (isPortrait) screenHeightDp.dp else screenWidthDp.dp
fun convertSpToDp(
    density: Density,
    spValue: Float,
): Float = with(density) { spValue.sp.toDp().value }
