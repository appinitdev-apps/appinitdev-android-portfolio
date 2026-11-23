
@Composable
fun LazyColumnScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier,
    settings: ScrollbarSettings = ScrollbarSettings.Default,
    indicatorContent: (@Composable (index: Int, isThumbSelected: Boolean) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (!settings.enabled) content()
    else Box(modifier) {
        content()
        val controller = rememberLazyListStateController(
            state = state,
            thumbMinLength = settings.thumbMinLength,
            alwaysShowScrollBar = settings.alwaysShowScrollbar,
            selectionMode = settings.selectionMode
        )

        ElementScrollbar(
            stateController = controller,
            modifier = modifier,
            settings = settings,
            indicatorContent = indicatorContent,
        )
    }
}
