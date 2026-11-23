
@Composable
fun AudioListItemLeadingIcon(
    item: DAudio,
    dragSelectState: DragSelectState,
    castMode: Boolean,
    isCurrentItemLoading: Boolean,
    isCurrentlyPlayingByCast: Boolean,
    isCurrentlyPlaying: Boolean,
) {
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        if (dragSelectState.selectMode) {
            Checkbox(
                checked = dragSelectState.isSelected(item.id),
                onCheckedChange = { dragSelectState.select(item.id) }
            )
        } else if (castMode) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isCurrentItemLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                } else if (isCurrentlyPlayingByCast) {
                    PulsatingWave(isPlaying = true, modifier = Modifier.align(Alignment.Center))
                } else {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(Res.drawable.cast),
                        contentDescription = stringResource(Res.string.cast),
                        tint = MaterialTheme.colorScheme.blue
                    )
                }
            }
        } else if (!isCurrentlyPlaying) {
            AudioCoverOrIcon(path = item.path, modifier = Modifier.size(40.dp))
        } else {
            PulsatingWave(isPlaying = true, modifier = Modifier.align(Alignment.Center))
        }
    }
}
