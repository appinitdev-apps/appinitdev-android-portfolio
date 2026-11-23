@Composable
fun PBottomAppBar(
    content: @Composable RowScope.() -> Unit,
) {
    BottomAppBar(
        tonalElevation = 2.dp,
        containerColor = MaterialTheme.colorScheme.cardBackgroundNormal,
        content = content
    )
}