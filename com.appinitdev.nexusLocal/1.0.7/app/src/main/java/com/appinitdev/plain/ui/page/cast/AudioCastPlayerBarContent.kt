@Composable
internal fun AudioCastPlayerBarContent(
    castVM: CastViewModel,
    title: String,
    artist: String,
    isPlaying: Boolean,
    progress: Float,
    duration: Float,
    supportsCallback: Boolean,
    currentUri: String,
    onShowPlaylist: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = {
                if (supportsCallback && duration > 0f) progress / duration else 0f
            },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).weight(1f)
                    .clickable { onShowPlaylist() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    text = if (title.isNotEmpty()) title else stringResource(Res.string.casting),
                    style = MaterialTheme.typography.listItemTitle(),
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                VerticalSpace(4.dp)
                Text(
                    text = if (artist.isNotEmpty()) artist else (CastPlayer.currentDevice?.description?.device?.friendlyName ?: ""),
                    style = MaterialTheme.typography.listItemSubtitle(),
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (currentUri.isNotEmpty()) {
                IconButton(
                    onClick = { if (isPlaying) castVM.pauseCast() else castVM.playCast() },
                    modifier = Modifier.size(48.dp).shadow(2.dp, CircleShape).clip(CircleShape)
                        .background(if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary),
                ) {
                    Icon(
                        painter = painterResource(if (isPlaying) Res.drawable.pause else Res.drawable.play_arrow),
                        contentDescription = if (isPlaying) stringResource(Res.string.pause) else stringResource(Res.string.play),
                        tint = if (isPlaying) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                    )
                }
                HorizontalSpace(8.dp)
            }
            IconButton(
                onClick = { onShowPlaylist() },
                modifier = Modifier.size(42.dp).clip(CircleShape),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.list_music),
                    contentDescription = stringResource(Res.string.playlist),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
