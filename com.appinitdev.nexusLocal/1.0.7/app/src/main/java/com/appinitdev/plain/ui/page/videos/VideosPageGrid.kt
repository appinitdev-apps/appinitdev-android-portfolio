@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VideosPageGrid(
    context: Context, videosVM: VideosViewModel, tagsVM: TagsViewModel, castVM: CastViewModel,
    itemsState: List<DVideo>, pagerState: PagerState, scrollBehavior: TopAppBarScrollBehavior,
    topRefreshLayoutState: RefreshLayoutState, dragSelectState: DragSelectState,
    previewerState: MediaPreviewerState, cellsPerRow: MutableState<Int>,
    imageWidthPx: Int, scope: CoroutineScope, paddingValues: PaddingValues,
) {
    val hapticFeedback = LocalHapticFeedback.current
    if (pagerState.pageCount == 0) { NoDataColumn(loading = videosVM.showLoading.value, search = videosVM.showSearchBar.value); return }
    HorizontalPager(state = pagerState) { index ->
        PullToRefresh(refreshLayoutState = topRefreshLayoutState) {
            AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                if (itemsState.isNotEmpty()) {
                    val scrollState = rememberLazyGridState()
                    videosVM.scrollStateMap[index] = scrollState
                    val flingBehavior = rememberBoostFlingBehavior(cellsPerRow.value / 3f)
                    LazyVerticalGridScrollbar(state = scrollState) {
                        LazyVerticalGrid(columns = GridCells.Fixed(cellsPerRow.value), state = scrollState, flingBehavior = flingBehavior,
                            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)
                                .gridDragSelect(items = itemsState, state = dragSelectState)
                                .pinchZoomGrid(cellsPerRow = cellsPerRow, hapticFeedback = hapticFeedback, scope = scope) { VideoGridCellsPerRowPreference.putAsync(it) },
                            horizontalArrangement = Arrangement.spacedBy(2.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            val isGroupMode = videosVM.sortBy.value == FileSortBy.TAKEN_AT_DESC
                                && videosVM.queryText.value.isEmpty()
                            if (isGroupMode) {
                                val groupedItems = groupMediaByDate(itemsState) { it.takenAt ?: it.createdAt }
                                groupedItems.forEach { group ->
                                    item(span = { GridItemSpan(maxLineSpan) }, key = "", contentType = "header") {
                                        Text(text = group.dateLabel, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.titleSmall)
                                    }
                                    items(group.items, key = { it.id }, contentType = { "video" }, span = { GridItemSpan(1) }) { m ->
                                        VideoGridItem(modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null), videosVM, castVM, m, showSize = cellsPerRow.value < 6, previewerState, dragSelectState, imageWidthPx, sort = videosVM.sortBy.value)
                                    }
                                }
                            } else {
                                items(itemsState, key = { it.id }, contentType = { "video" }, span = { GridItemSpan(1) }) { m ->
                                    VideoGridItem(modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null), videosVM, castVM, m, showSize = cellsPerRow.value < 6, previewerState, dragSelectState, imageWidthPx, sort = videosVM.sortBy.value)
                                }
                            }
                            item(span = { GridItemSpan(maxLineSpan) }, key = "") {
                                if (itemsState.isNotEmpty() && !videosVM.noMore.value) { LaunchedEffect(Unit) { scope.launch(Dispatchers.IO) { withIO { videosVM.moreAsync(context, tagsVM) } } } }
                                LoadMoreRefreshContent(videosVM.noMore.value)
                            }
                            item(span = { GridItemSpan(maxLineSpan) }, key = "") { BottomSpace(paddingValues) }
                        }
                    }
                } else {
                    NoDataColumn(loading = videosVM.showLoading.value, search = videosVM.showSearchBar.value)
                }
            }
        }
    }
}
