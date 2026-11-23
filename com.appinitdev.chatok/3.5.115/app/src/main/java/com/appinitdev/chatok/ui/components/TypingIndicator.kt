
@Composable
fun TypingIndicator(color: Color = Color(0xFF238CDD)) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
        label = "dot1"
    )
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 200, easing = LinearEasing), RepeatMode.Reverse),
        label = "dot2"
    )
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 400, easing = LinearEasing), RepeatMode.Reverse),
        label = "dot3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Dot(color.copy(alpha = dot1Alpha))
        Dot(color.copy(alpha = dot2Alpha))
        Dot(color.copy(alpha = dot3Alpha))
    }
}

@Composable
private fun Dot(color: Color) {
    Box(
        modifier = Modifier
            .size(6.dp)
            .background(color, CircleShape)
    )
}
