
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FeedEntriesPageContent(
    feedEntriesVM: FeedEntriesViewModel, tagsVM: TagsViewModel,
    navController: NavHostController, scope: CoroutineScope,
    tabs: List<VTabData>, pagerState: PagerState,
    scrollBehavior: TopAppBarScrollBehavior,
    scrollStateMap: MutableMap<Int, LazyListState>,
    topRefreshLayoutState: RefreshLayoutState,
    feedsMap: State<Map<String, DFeed>>,
    tagsState: List<DTag>, tagsMapState: Map<String, List<DTagRelation>>,
    bottomPadding: Float,
) {
    val itemsState by feedEntriesVM.itemsFlow.collectAsState()

    if (!feedEntriesVM.selectMode.value) {
        PScrollableTabRow(selectedTabIndex = pagerState.currentPage, modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, s ->
                PFilterChip(
                    modifier = Modifier.padding(start = if (index == 0) 0.dp else 8.dp),
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.scrollToPage(index) } },
                    label = {
                        if (index < 2) Text(text = s.title + " (" + s.count + ")")
                        else Text(if (feedEntriesVM.feedId.value.isNotEmpty() || feedEntriesVM.queryText.value.isNotEmpty()) s.title else "${s.title} (${s.count})")
                    }
                )
            }
        }
    }
    if (pagerState.pageCount == 0) {
        NoDataColumn(loading = feedEntriesVM.showLoading.value, search = feedEntriesVM.showSearchBar.value)
        return
    }
    HorizontalPager(state = pagerState, userScrollEnabled = false) { index ->
        PullToRefresh(
            refreshLayoutState = topRefreshLayoutState,
            refreshContent = remember { {
                PullToRefreshContent(createText = {
                    when (it) {
                        RefreshContentState.Failed -> stringResource(Res.string.sync_failed)
                        RefreshContentState.Finished -> stringResource(Res.string.synced)
                        RefreshContentState.Refreshing -> stringResource(Res.string.syncing)
                        RefreshContentState.Dragging -> {
                            if (abs(getRefreshContentOffset()) < getRefreshContentThreshold())
                                stringResource(if (feedEntriesVM.feedId.value.isNotEmpty()) Res.string.pull_down_to_sync_current_feed else Res.string.pull_down_to_sync_all_feeds)
                            else stringResource(if (feedEntriesVM.feedId.value.isNotEmpty()) Res.string.release_to_sync_current_feed else Res.string.release_to_sync_all_feeds)
                        }
                    }
                })
            } },
        ) {
            AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                if (itemsState.isNotEmpty()) {
                    val scrollState = rememberLazyListState()
                    scrollStateMap[index] = scrollState
                    LazyColumnScrollbar(state = scrollState) {
                        LazyColumn(Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection), state = scrollState) {
                            item(key = "") { TopSpace() }
                            itemsIndexed(itemsState, key = { _, m -> m.id }) { idx, m ->
                                val tagIds = tagsMapState[m.id]?.map { it.tagId } ?: emptyList()
                                FeedEntryListItem(feedEntriesVM, idx, m, feedsMap.value[m.feedId], tagsState.filter { tagIds.contains(it.id) },
                                    onClick = { if (feedEntriesVM.selectMode.value) feedEntriesVM.select(m.id) else navController.navigate(Routing.FeedEntry(m.id)) },
                                    onLongClick = { if (!feedEntriesVM.selectMode.value) feedEntriesVM.selectedItem.value = m },
                                    onClickTag = { tag -> if (!feedEntriesVM.selectMode.value) { val i = tabs.indexOfFirst { it.value == tag.id }; if (i != -1) scope.launch { pagerState.scrollToPage(i) } } }
                                )
                                VerticalSpace(dp = 8.dp)
                            }
                            item(key = "") {
                                if (itemsState.isNotEmpty() && !feedEntriesVM.noMore.value) {
                                    LaunchedEffect(Unit) { scope.launch(Dispatchers.IO) { withIO { feedEntriesVM.moreAsync(tagsVM) } } }
                                }
                                LoadMoreRefreshContent(feedEntriesVM.noMore.value)
                                VerticalSpace(dp = bottomPadding.dp)
                            }
                        }
                    }
                } else {
                    NoDataColumn(loading = feedEntriesVM.showLoading.value, search = feedEntriesVM.showSearchBar.value)
                }
            }
        }
    }
}
