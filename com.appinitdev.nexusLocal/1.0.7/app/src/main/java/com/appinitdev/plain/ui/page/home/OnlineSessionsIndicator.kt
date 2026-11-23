@Composable
fun OnlineSessionsIndicator(count: Int, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "ripple")
    val progress1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "ring1",
    )
    val progress2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1800, 900, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "ring2",
    )
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.greenPill,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val dotColor = MaterialTheme.colorScheme.greenDot
            Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(20.dp)) {
                    val center = this.center
                    listOf(progress1, progress2).forEach { p ->
                        drawCircle(
                            color = dotColor.copy(alpha = (1f - p) * 0.5f),
                            radius = (size.minDimension / 2f) * p,
                            center = center,
                        )
                    }
                    drawCircle(
                        color = dotColor,
                        radius = size.minDimension * 0.22f,
                        center = center
                    )
                }
            }
            Text(
                text = stringResource(Res.string.clients_online, count),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.greenText,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}