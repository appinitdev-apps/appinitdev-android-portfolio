
@Composable
internal fun ReorderableCollectionItemScope.AudioCastPlaylistItemContent(
    item: IMedia, index: Int, isPlaying: Boolean, isLoading: Boolean, onRemove: () -> Unit,
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }

    LaunchedEffect(item.path) {
        val audio = withIO { DPlaylistAudio.fromPath(context, item.path) }
        title = audio.title; artist = audio.artist
    }

    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).clip(CircleShape)
            .background(if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.circleBackground)
            .draggableHandle(), contentAlignment = Alignment.Center) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                isPlaying -> PulsatingWave(isPlaying = true, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.align(Alignment.Center))
                else -> Text(text = "${index + 1}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondaryTextColor)
            }
        }
        HorizontalSpace(16.dp)
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(text = title, style = MaterialTheme.typography.listItemTitle())
            VerticalSpace(4.dp)
            Text(text = artist, style = MaterialTheme.typography.listItemSubtitle())
        }
        PIconButton(icon = Res.drawable.playlist_remove, tint = MaterialTheme.colorScheme.red,
            contentDescription = stringResource(Res.string.remove_from_cast_queue), click = onRemove)
    }
}
