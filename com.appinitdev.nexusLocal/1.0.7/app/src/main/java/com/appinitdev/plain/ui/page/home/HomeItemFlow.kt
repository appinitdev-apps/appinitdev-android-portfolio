@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeItemFlow(content: @Composable FlowRowScope.() -> Unit) {
    FlowRow(
        modifier =
            Modifier
                .fillMaxWidth(),
        maxItemsInEachRow = 3,
        content = content,
    )
}
