
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImagesPageGrid(
    context: Context, imagesVM: ImagesViewModel, tagsVM: TagsViewModel, castVM: CastViewModel,
    itemsState: List<DImage>, pagerState: PagerState, scrollBehavior: TopAppBarScrollBehavior,
    topRefreshLayoutState: RefreshLayoutState, dragSelectState: DragSelectState,
    previewerState: MediaPreviewerState, cellsPerRow: MutableState<Int>,
    imageWidthPx: Int, scope: CoroutineScope, paddingValues: PaddingValues,
) {
    val hapticFeedback = LocalHapticFeedback.current
    if (pagerState.pageCount == 0) { NoDataColumn(loading = imagesVM.showLoading.value, search = imagesVM.showSearchBar.value); return }
    HorizontalPager(state = pagerState) { index ->
        PullToRefresh(refreshLayoutState = topRefreshLayoutState) {
            AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                if (itemsState.isNotEmpty()) {
                    val scrollState = rememberLazyGridState()
                    imagesVM.scrollStateMap[index] = scrollState
                    val flingBehavior = rememberBoostFlingBehavior(cellsPerRow.value / 3f)
                    LazyVerticalGridScrollbar(state = scrollState) {
                        LazyVerticalGrid(columns = GridCells.Fixed(cellsPerRow.value), state = scrollState, flingBehavior = flingBehavior,
                            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)
                                .gridDragSelect(items = itemsState, state = dragSelectState)
                                .pinchZoomGrid(cellsPerRow = cellsPerRow, hapticFeedback = hapticFeedback, scope = scope) { ImageGridCellsPerRowPreference.putAsync(it) },
                            horizontalArrangement = Arrangement.spacedBy(2.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            val isGroupMode = imagesVM.sortBy.value == FileSortBy.TAKEN_AT_DESC
                                && imagesVM.queryText.value.isEmpty()
                                && !imagesVM.useAiSearch.value
                            if (isGroupMode) {
                                val groupedItems = groupMediaByDate(itemsState) { it.takenAt ?: it.createdAt }
                                groupedItems.forEach { group ->
                                    item(span = { GridItemSpan(maxLineSpan) }, key = "", contentType = "header") {
                                        Text(text = group.dateLabel, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.titleSmall)
                                    }
                                    items(group.items, key = { it.id }, contentType = { "image" }, span = { GridItemSpan(1) }) { m ->
                                        ImageGridItem(modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null), imagesVM, castVM, m, showSize = cellsPerRow.value < 6, previewerState, dragSelectState, imageWidthPx)
                                    }
                                }
                            } else {
                                items(itemsState, key = { it.id }, contentType = { "image" }, span = { GridItemSpan(1) }) { m ->
                                    ImageGridItem(modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null), imagesVM, castVM, m, showSize = cellsPerRow.value < 6, previewerState, dragSelectState, imageWidthPx)
                                }
                            }
                            item(span = { GridItemSpan(maxLineSpan) }, key = "") {
                                if (itemsState.isNotEmpty() && !imagesVM.noMore.value) { LaunchedEffect(Unit) { scope.launch(Dispatchers.IO) { withIO { imagesVM.moreAsync(context, tagsVM) } } } }
                                LoadMoreRefreshContent(imagesVM.noMore.value)
                            }
                            item(span = { GridItemSpan(maxLineSpan) }, key = "") { BottomSpace(paddingValues) }
                        }
                    }
                } else {
                    NoDataColumn(loading = imagesVM.showLoading.value, search = imagesVM.showSearchBar.value)
                }
            }
        }
    }
}
