@Composable
fun AudioPlayerControls(
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val progress = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f
        PlayerSlider(
            modifier = Modifier.fillMaxWidth().height(24.dp),
            progress = progress,
            bufferedProgress = 0f,
            onProgressChange = onSeek,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = positionMs.formatMinSec(), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
            Text(text = durationMs.formatMinSec(), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
        }
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.15f),
            modifier = Modifier.size(72.dp),
        ) {
            IconButton(modifier = Modifier.size(72.dp), onClick = onPlayPause) {
                Icon(
                    painter = painterResource(if (isPlaying) Res.drawable.pause else Res.drawable.play_arrow),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp),
                )
            }
        }
    }
}
