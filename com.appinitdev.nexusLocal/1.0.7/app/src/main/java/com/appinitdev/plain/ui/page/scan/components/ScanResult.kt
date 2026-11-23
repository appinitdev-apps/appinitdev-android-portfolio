
@Composable
fun ScanResult(
    context: Context,
    text: String,
) {
    PCard {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val newText = text.linkify()
            SelectionContainer {
                ClickableText(
                    text = newText,
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    onClick = { position -> newText.urlAt(context, position) },
                )
            }
        }
    }
}
