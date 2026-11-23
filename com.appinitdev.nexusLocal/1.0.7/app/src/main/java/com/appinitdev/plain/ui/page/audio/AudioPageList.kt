
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun ColumnScope.AudioPageList(
    pagerState: PagerState, scrollBehavior: TopAppBarScrollBehavior,
    dragSelectState: DragSelectState, itemsState: List<DAudio>,
    audioVM: AudioViewModel, audioPlaylistVM: AudioPlaylistViewModel,
    tagsVM: TagsViewModel, castVM: CastViewModel,
    audioTagsMap: Map<String, List<DTag>>, isAudioPlaying: Boolean,
    topRefreshLayoutState: RefreshLayoutState, paddingValues: PaddingValues,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    if (pagerState.pageCount == 0) {
        NoDataColumn(loading = audioVM.showLoading.value, search = audioVM.showSearchBar.value)
        return
    }
    HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
        PullToRefresh(refreshLayoutState = topRefreshLayoutState, userEnable = !dragSelectState.selectMode) {
            AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                if (itemsState.isNotEmpty()) {
                    val scrollState = rememberLazyListState()
                    audioVM.scrollStateMap[index] = scrollState
                    LazyColumnScrollbar(state = scrollState) {
                        LazyColumn(Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)
                            .listDragSelect(items = itemsState, state = dragSelectState), state = scrollState) {
                            item { TopSpace() }
                            items(items = itemsState, key = { it.id }) { item ->
                                val tags = audioTagsMap[item.id] ?: emptyList()
                                AudioListItem(item = item, audioVM = audioVM, audioPlaylistVM, tagsVM = tagsVM,
                                    castVM = castVM, tags = tags, pagerState = pagerState, dragSelectState = dragSelectState,
                                    isCurrentlyPlaying = isAudioPlaying && audioPlaylistVM.selectedPath.value == item.path,
                                    isInPlaylist = audioPlaylistVM.isInPlaylist(item.path))
                                VerticalSpace(dp = 8.dp)
                            }
                            item(key = "") {
                                if (itemsState.isNotEmpty() && !audioVM.noMore.value) {
                                    LaunchedEffect(Unit) {
                                        scope.launch(Dispatchers.IO) { withIO { audioVM.moreAsync(context, tagsVM) } }
                                    }
                                }
                                LoadMoreRefreshContent(audioVM.noMore.value)
                            }
                            item(key = "") { BottomSpace(paddingValues) }
                        }
                    }
                } else {
                    NoDataColumn(loading = audioVM.showLoading.value, search = audioVM.showSearchBar.value)
                }
            }
        }
    }
}
