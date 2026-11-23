@Composable
internal fun FileListItemPlayer(
    isSelected: Boolean, isPlaying: Boolean, progress: Float, duration: Float,
    onProgressChange: (Float) -> Unit, onShowFullPlayer: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 4.dp)
        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    PlayerSlider(progress = if (duration == 0f) 0f else progress / duration, bufferedProgress = 0f,
                        modifier = Modifier.fillMaxWidth().height(20.dp),
                        onProgressChange = onProgressChange,
                        onValueChangeFinished = { AudioPlayer.seekTo(progress.toLong()) },
                        trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                        progressColor = MaterialTheme.colorScheme.primary, thumbColor = MaterialTheme.colorScheme.primary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = progress.toLong().formatDuration(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = duration.toLong().formatDuration(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                HorizontalSpace(16.dp)
                Row {
                    IconButton(onClick = { if (isPlaying) AudioPlayer.pause() else AudioPlayer.play() },
                        modifier = Modifier.size(40.dp).shadow(2.dp, CircleShape).clip(CircleShape)
                            .background(if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary)) {
                        Icon(painter = painterResource(if (isPlaying) Res.drawable.pause else Res.drawable.play_arrow),
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = if (isPlaying) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp))
                    }
                    HorizontalSpace(8.dp)
                    IconButton(onClick = onShowFullPlayer,
                        modifier = Modifier.size(36.dp).shadow(2.dp, CircleShape).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        Icon(painter = painterResource(Res.drawable.music2), contentDescription = "Full player",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
