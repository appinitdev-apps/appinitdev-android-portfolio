
@Composable
fun rememberWindowInsetsController(): WindowInsetsControllerCompat {
    val window = with(LocalActivity.current as Activity) { return@with window }
    return remember { WindowCompat.getInsetsController(window, window.decorView) }
}