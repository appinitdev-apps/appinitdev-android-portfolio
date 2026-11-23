
@Composable
fun VerticalRefreshableLayout(
    topRefreshLayoutState: RefreshLayoutState,
    bottomRefreshLayoutState: RefreshLayoutState,
    modifier: Modifier = Modifier,
    topRefreshContent: @Composable RefreshLayoutState.() -> Unit = remember {
        { PullToRefreshContent() }
    },
    bottomIsLoadFinish: Boolean = false,
    bottomRefreshContent: @Composable RefreshLayoutState.() -> Unit = remember(bottomIsLoadFinish) {
        { LoadMoreRefreshContent(bottomIsLoadFinish) }
    },
    topUserEnable: Boolean = true,
    bottomUserEnable: Boolean = true,
    content: @Composable () -> Unit
) {
    LaunchedEffect(bottomIsLoadFinish) {
        bottomRefreshLayoutState.canCallRefreshListener = !bottomIsLoadFinish
    }
    RefreshLayout(
        modifier = modifier,
        refreshContent = topRefreshContent,
        refreshLayoutState = topRefreshLayoutState,
        userEnable = topUserEnable,
    ) {
        RefreshLayout(
            modifier = Modifier.fillMaxSize(),
            refreshContent = bottomRefreshContent,
            refreshLayoutState = bottomRefreshLayoutState,
            composePosition = ComposePosition.Bottom,
            userEnable = bottomUserEnable,
            refreshingCanScroll = true,
            content = content
        )
    }
}

