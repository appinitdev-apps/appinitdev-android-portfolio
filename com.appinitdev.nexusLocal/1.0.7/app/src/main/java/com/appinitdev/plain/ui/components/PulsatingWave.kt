@Composable
fun PulsatingWave(
    isPlaying: Boolean,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    if (!isPlaying) return
    val infiniteTransition = rememberInfiniteTransition(label = "audio_playing_animation")
    val density = LocalDensity.current
    val scale1 = infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val scale2 = infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val scale3 = infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )
    Row(
        modifier = modifier.padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        val barWidth = with(density) { 3.dp.toPx() }
        val maxBarHeight = 16.dp
        Box(
            modifier = Modifier
                .width(with(density) { barWidth.toDp() })
                .height(with(density) { (maxBarHeight.toPx() * scale1.value).toDp() })
                .clip(RoundedCornerShape(1.dp))
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(with(density) { barWidth.toDp() })
                .height(with(density) { (maxBarHeight.toPx() * scale2.value).toDp() })
                .clip(RoundedCornerShape(1.dp))
                .background(color)
        )
        Box(
            modifier = Modifier
                .width(with(density) { barWidth.toDp() })
                .height(with(density) { (maxBarHeight.toPx() * scale3.value).toDp() })
                .clip(RoundedCornerShape(1.dp))
                .background(color)
        )
    }
} 