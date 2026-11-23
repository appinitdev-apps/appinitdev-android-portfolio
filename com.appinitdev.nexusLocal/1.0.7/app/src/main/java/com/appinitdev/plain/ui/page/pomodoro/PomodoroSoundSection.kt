@Composable
fun PomodoroSoundSection(
    soundPath: String,
    originalFileName: String,
    onClear: () -> Unit,
) {
    Column {
        Text(
            text = stringResource(Res.string.custom_sound),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            if (soundPath.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Text(
                        text = originalFileName, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface, maxLines = 2,
                        overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { sendEvent(PickFileEvent(PickFileTag.POMODORO, PickFileType.FILE, multiple = false)) }) {
                            Text(stringResource(Res.string.change))
                        }
                        TextButton(onClick = onClear) {
                            Text(text = stringResource(Res.string.clear), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.Center) {
                    TextButton(onClick = { sendEvent(PickFileEvent(PickFileTag.POMODORO, PickFileType.FILE, multiple = false)) }) {
                        Text(stringResource(Res.string.select_sound))
                    }
                }
            }
        }
    }
}
