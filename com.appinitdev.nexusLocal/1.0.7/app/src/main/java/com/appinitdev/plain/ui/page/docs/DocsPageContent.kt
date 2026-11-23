@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ColumnScope.DocsPageContent(
    navController: NavHostController, docsVM: DocsViewModel,
    tagsVM: TagsViewModel,
    itemsState: List<DDoc>,
    dragSelectState: DragSelectState,
    docsTagsMap: Map<String, List<DTag>>,
    pagerState: PagerState, scrollBehavior: TopAppBarScrollBehavior,
    topRefreshLayoutState: RefreshLayoutState, paddingValues: PaddingValues,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    if (pagerState.pageCount == 0) {
        NoDataColumn(loading = docsVM.showLoading.value, search = docsVM.showSearchBar.value)
        return
    }
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.weight(1f),
        userScrollEnabled = !dragSelectState.selectMode,
    ) { index ->
        PullToRefresh(refreshLayoutState = topRefreshLayoutState, userEnable = !dragSelectState.selectMode) {
            AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                if (itemsState.isNotEmpty()) {
                    val scrollState = rememberLazyListState()
                    docsVM.scrollStateMap[index] = scrollState
                    LazyColumnScrollbar(state = scrollState) {
                        LazyColumn(
                            Modifier
                                .fillMaxSize()
                                .nestedScroll(scrollBehavior.nestedScrollConnection)
                                .listDragSelect(items = itemsState, state = dragSelectState),
                            state = scrollState
                        ) {
                            item { TopSpace() }
                            items(itemsState, key = { it.id }) { m ->
                                val tags = docsTagsMap[m.id] ?: emptyList()
                                DocItem(
                                    navController = navController,
                                    docsVM = docsVM,
                                    dragSelectState = dragSelectState,
                                    m = m,
                                    tags = tags,
                                    onTagClick = { tag ->
                                        if (!docsVM.tabsShowTags.value) {
                                            return@DocItem
                                        }
                                        val idx = tagsVM.itemsFlow.value.indexOfFirst { it.id == tag.id }
                                        if (idx != -1) {
                                            scope.launch { pagerState.scrollToPage(idx + if (AppFeatureType.MEDIA_TRASH.has()) 2 else 1) }
                                        }
                                    }
                                )
                                VerticalSpace(dp = 8.dp)
                            }
                            item(key = "") {
                                if (itemsState.isNotEmpty() && !docsVM.noMore.value) {
                                    LaunchedEffect(Unit) {
                                        scope.launch(Dispatchers.IO) { withIO { docsVM.moreAsync(context, tagsVM) } }
                                    }
                                }
                                LoadMoreRefreshContent(docsVM.noMore.value)
                            }
                            item(key = "") { BottomSpace(paddingValues) }
                        }
                    }
                } else {
                    NoDataColumn(loading = docsVM.showLoading.value, search = docsVM.showSearchBar.value)
                }
            }
        }
    }
}
