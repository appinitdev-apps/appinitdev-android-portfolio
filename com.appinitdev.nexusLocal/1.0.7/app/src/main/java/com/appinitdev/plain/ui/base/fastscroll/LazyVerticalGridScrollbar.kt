@Composable
fun LazyVerticalGridScrollbar(
    state: LazyGridState,
    modifier: Modifier = Modifier,
    settings: ScrollbarSettings = ScrollbarSettings.Default,
    indicatorContent: (@Composable (index: Int, isThumbSelected: Boolean) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (!settings.enabled) content()
    else Box(modifier) {
        content()
        val controller = rememberLazyGridStateController(
            state = state,
            thumbMinLength = settings.thumbMinLength,
            alwaysShowScrollBar = settings.alwaysShowScrollbar,
            selectionMode = settings.selectionMode,
        )
        ElementScrollbar(
            stateController = controller,
            modifier = modifier,
            settings = settings,
            indicatorContent = indicatorContent
        )
    }
}
