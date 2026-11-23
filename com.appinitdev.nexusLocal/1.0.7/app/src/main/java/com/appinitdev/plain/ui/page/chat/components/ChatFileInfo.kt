@Composable
fun ChatFileInfo(
    modifier: Modifier = Modifier,
    fileName: String,
    size: Long,
    duration: Long,
    summary: String,
    isCurrentlyPlaying: Boolean,
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, end = 8.dp),
            text = fileName,
            style = MaterialTheme.typography.listItemTitle(),
            color = if (isCurrentlyPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            text = size.formatBytes() + if (duration > 0) " / ${duration.formatDuration()}" else "",
            style = MaterialTheme.typography.listItemSubtitle(),
        )
        if (fileName.isTextFile() && summary.isNotEmpty()) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, end = 8.dp),
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
            )
        }
    }
}
