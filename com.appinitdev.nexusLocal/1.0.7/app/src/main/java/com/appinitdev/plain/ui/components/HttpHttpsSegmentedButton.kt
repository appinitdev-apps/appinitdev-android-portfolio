
@Composable
fun HttpHttpsSegmentedButton(isHttps: Boolean, onSelect: (Boolean) -> Unit) {
    val trackColor = MaterialTheme.colorScheme.cardBackgroundNormal
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedText = MaterialTheme.colorScheme.onSurfaceVariant
    val selectedText = MaterialTheme.colorScheme.onPrimary

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(trackColor)
            .padding(3.dp),
    ) {
        listOf(false to "HTTP", true to "HTTPS").forEach { (https, label) ->
            val selected = isHttps == https
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) selectedColor else trackColor)
                    .clickable { onSelect(https) }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) selectedText else unselectedText,
                )
            }
        }
    }
}