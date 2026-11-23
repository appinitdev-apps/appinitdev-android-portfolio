@Composable
fun PCard(
    horizontal: Dp = PlainTheme.PAGE_HORIZONTAL_MARGIN,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontal),
        shape = RoundedCornerShape(PlainTheme.CARD_RADIUS),
        colors = CardDefaults.cardColors().copy(
            containerColor = MaterialTheme.colorScheme.cardBackgroundNormal
        )
    ) {
        content()
    }
}