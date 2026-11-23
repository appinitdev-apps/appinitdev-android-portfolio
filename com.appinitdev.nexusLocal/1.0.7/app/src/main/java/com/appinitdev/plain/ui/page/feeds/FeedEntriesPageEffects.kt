@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FeedEntriesPageEffects(
    feedEntriesVM: FeedEntriesViewModel, feedsVM: FeedsViewModel, tagsVM: TagsViewModel,
    feedId: String, scope: CoroutineScope, tabs: List<VTabData>,
    pagerState: PagerState, scrollBehavior: TopAppBarScrollBehavior,
    scrollStateMap: MutableMap<Int, LazyListState>,
    topRefreshLayoutState: RefreshLayoutState,
    isFirstTime: MutableState<Boolean>,
    onSearch: (String) -> Unit,
) {
    LaunchedEffect(Unit) {
        tagsVM.dataType.value = feedEntriesVM.dataType
        feedEntriesVM.feedId.value = feedId
        scope.launch(Dispatchers.IO) { feedsVM.loadAsync(); feedEntriesVM.loadAsync(tagsVM) }
    }
    LaunchedEffect(Channel.sharedFlow) {
        Channel.sharedFlow.collect { event ->
            if (event is FeedStatusEvent) {
                if (event.status == FeedWorkerStatus.COMPLETED) {
                    scope.launch(Dispatchers.IO) { feedEntriesVM.loadAsync(tagsVM) }
                    topRefreshLayoutState.setRefreshState(RefreshContentState.Finished)
                } else if (event.status == FeedWorkerStatus.ERROR) {
                    topRefreshLayoutState.setRefreshState(RefreshContentState.Failed)
                    if (feedId.isNotEmpty()) {
                        if (FeedFetchWorker.statusMap[feedId] == FeedWorkerStatus.ERROR) {
                            DialogHelper.showErrorDialog(FeedFetchWorker.errorMap[feedId] ?: "")
                        }
                    } else {
                        DialogHelper.showErrorDialog(FeedFetchWorker.errorMap.values.joinToString("\n"))
                    }
                }
            }
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        if (isFirstTime.value) { isFirstTime.value = false; return@LaunchedEffect }
        val tab = tabs.getOrNull(pagerState.currentPage)
        if (tab != null) {
            when (tab.value) {
                "all" -> { feedEntriesVM.filterType = FeedEntryFilterType.DEFAULT; feedEntriesVM.tag.value = null }
                "today" -> { feedEntriesVM.filterType = FeedEntryFilterType.TODAY; feedEntriesVM.tag.value = null }
                else -> { feedEntriesVM.filterType = FeedEntryFilterType.DEFAULT; feedEntriesVM.tag.value = tagsVM.itemsFlow.value.find { it.id == tab.value } }
            }
        }
        scope.launch { scrollBehavior.reset(); scrollStateMap[pagerState.currentPage]?.scrollToItem(0) }
        scope.launch(Dispatchers.IO) { feedEntriesVM.loadAsync(tagsVM) }
    }
    LaunchedEffect(feedEntriesVM.selectMode.value) {
        if (feedEntriesVM.selectMode.value) scrollBehavior.reset()
    }
    BackHandler(enabled = feedEntriesVM.selectMode.value || feedEntriesVM.showSearchBar.value) {
        if (feedEntriesVM.selectMode.value) {
            feedEntriesVM.exitSelectMode()
        } else if (feedEntriesVM.showSearchBar.value) {
            if (!feedEntriesVM.searchActive.value || feedEntriesVM.queryText.value.isEmpty()) {
                feedEntriesVM.exitSearchMode(); onSearch("")
            }
        }
    }
}
