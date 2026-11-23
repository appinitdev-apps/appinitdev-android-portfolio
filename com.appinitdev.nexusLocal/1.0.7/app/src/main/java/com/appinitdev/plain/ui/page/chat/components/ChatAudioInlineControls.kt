@Composable
fun ChatAudioInlineControls(
    progress: Float,
    duration: Float,
    isPlaying: Boolean,
    onProgressChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    onShowFullPlayer: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            PlayerSlider(
                progress = if (duration == 0f) 0f else progress / duration,
                bufferedProgress = 0f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                onProgressChange = onProgressChange,
                onValueChangeFinished = onValueChangeFinished,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                progressColor = MaterialTheme.colorScheme.primary,
                thumbColor = MaterialTheme.colorScheme.primary,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = progress.toLong().formatDuration(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = duration.toLong().formatDuration(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HorizontalSpace(16.dp)
        Row {
            if (isPlaying) {
                IconButton(
                    onClick = { AudioPlayer.pause() },
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.pause),
                        contentDescription = stringResource(Res.string.pause),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp),
                    )
                }
            } else {
                IconButton(
                    onClick = { AudioPlayer.play() },
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.play_arrow),
                        contentDescription = stringResource(Res.string.play),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            HorizontalSpace(8.dp)
            IconButton(
                onClick = onShowFullPlayer,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.music2),
                    contentDescription = "Full player",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
