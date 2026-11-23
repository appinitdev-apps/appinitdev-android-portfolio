@Composable
fun SwipeActionButton(text: String, color: Color, onClick: () -> Unit) {
    TextButton(
        onClick
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(color = color)
        )
    }
}