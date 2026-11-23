@Composable
internal fun AnimatedIconContainer(
    iconRes: DrawableResource
) {
    Box(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .size(110.dp),
        contentAlignment = Alignment.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
        Surface(
            modifier = Modifier
                .size(110.dp)
                .scale(scale)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
        ) {
            Box(modifier = Modifier.fillMaxSize())
        }
        Surface(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(45.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
