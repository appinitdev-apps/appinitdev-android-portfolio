
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotesPageContent(
    notesVM: NotesViewModel, tagsVM: TagsViewModel,
    itemsState: List<DNote>, tagsState: List<DTag>, tagsMapState: Map<String, List<DTagRelation>>,
    scrollStateMap: MutableMap<Int, LazyListState>, index: Int,
    scrollBehavior: TopAppBarScrollBehavior, topRefreshLayoutState: RefreshLayoutState,
    navController: NavHostController, bottomPadding: Dp,
    tabs: List<VTabData>, scope: CoroutineScope, pagerState: PagerState,
) {
    PullToRefresh(refreshLayoutState = topRefreshLayoutState) {
        AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
            if (itemsState.isNotEmpty()) {
                val scrollState = rememberLazyListState()
                scrollStateMap[index] = scrollState
                LazyColumnScrollbar(state = scrollState) {
                    LazyColumn(Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection), state = scrollState) {
                        item { TopSpace() }
                        items(itemsState, key = { it.id }) { m ->
                            val tagIds = tagsMapState[m.id]?.map { it.tagId } ?: emptyList()
                            NoteListItem(notesVM, m, tagsState.filter { tagIds.contains(it.id) },
                                onClick = { if (notesVM.selectMode.value) notesVM.select(m.id) else navController.navigate(Routing.NoteDetail(m.id)) },
                                onLongClick = { if (notesVM.selectMode.value) return@NoteListItem; notesVM.selectedItem.value = m },
                                onClickTag = { tag ->
                                    if (notesVM.selectMode.value) return@NoteListItem
                                    val idx = tabs.indexOfFirst { it.value == tag.id }
                                    if (idx != -1) scope.launch { pagerState.scrollToPage(idx) }
                                },
                            )
                            VerticalSpace(dp = 8.dp)
                        }
                        item {
                            if (itemsState.isNotEmpty() && !notesVM.noMore.value) {
                                LaunchedEffect(Unit) { scope.launch(Dispatchers.IO) { withIO { notesVM.moreAsync(tagsVM) } } }
                            }
                            LoadMoreRefreshContent(notesVM.noMore.value)
                        }
                        item { VerticalSpace(dp = bottomPadding) }
                    }
                }
            } else {
                NoDataColumn(loading = notesVM.showLoading.value, search = notesVM.showSearchBar.value)
            }
        }
    }
}
