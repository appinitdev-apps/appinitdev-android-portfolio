@Composable
fun ReorderableCollectionItemScope.AudioPlaylistItemRow(
    audio: DPlaylistAudio,
    index: Int,
    isPlaying: Boolean,
    audioPlaylistVM: AudioPlaylistViewModel,
    scope: CoroutineScope,
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { Permissions.checkNotification(context, Res.string.audio_notification_prompt) { AudioPlayer.justPlay(context, audio) } },
        colors = CardDefaults.cardColors(containerColor = if (isPlaying) MaterialTheme.colorScheme.cardBackgroundActive else MaterialTheme.colorScheme.cardBackgroundNormal),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape)
                    .background(if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.circleBackground)
                    .draggableHandle(),
                contentAlignment = Alignment.Center
            ) {
                if (isPlaying) {
                    PulsatingWave(isPlaying = true, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.align(Alignment.Center))
                } else {
                    Text(text = "${index + 1}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondaryTextColor)
                }
            }
            HorizontalSpace(16.dp)
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(text = audio.title, style = MaterialTheme.typography.listItemTitle())
                VerticalSpace(4.dp)
                Text(text = audio.artist, style = MaterialTheme.typography.listItemSubtitle())
            }
            PIconButton(icon = Res.drawable.playlist_remove, tint = MaterialTheme.colorScheme.red,
                contentDescription = stringResource(Res.string.remove_from_playlist),
                click = { scope.launch(Dispatchers.IO) { audioPlaylistVM.removeAsync(context, audio.path) } })
        }
    }
}
