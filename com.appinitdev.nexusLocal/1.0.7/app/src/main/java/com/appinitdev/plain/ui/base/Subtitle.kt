
@Composable
fun Subtitle(
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Text(
        text = text,
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 32.dp, bottom = 8.dp),
        style = MaterialTheme.typography.titleMedium.copy(color = color),
    )
}
