 

















@Composable
fun ScreensaverMode(
    screensaverMode: Boolean,
    isActive: Boolean,
    screenWidth: Dp,
    yOffset: Animatable<Float, AnimationVector1D>,
) {
    val coroutineScope = rememberCoroutineScope()
    var animationJob: Job? by remember { mutableStateOf(null) }
    val configuration = LocalConfiguration.current

    LaunchedEffect(screensaverMode && isActive) {
        if (screensaverMode && isActive) {
            animationJob =
                coroutineScope.launch {
                    while (true) {
                        delay(30.seconds)
                        val max = (configuration.screenHeight - screenWidth) / 3
                        val maxValue = abs(max.value.toInt())
                        if (maxValue > 0) {
                            val newOffset = Random.nextInt(from = -maxValue, until = maxValue)
                            yOffset.animateTo(
                                newOffset.toFloat(),
                                animationSpec = tween(durationMillis = 2000, easing = EaseInOut),
                            )
                        }
                    }
                }
        } else {
            animationJob?.cancel()
            yOffset.animateTo(
                0f,
                animationSpec = tween(durationMillis = 150, easing = EaseInOut),
            )
        }
    }
}
