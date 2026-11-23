
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotesPageEffects(
    notesVM: NotesViewModel, tagsVM: TagsViewModel,
    scrollBehavior: TopAppBarScrollBehavior,
    scrollStateMap: Map<Int, LazyListState>, pagerState: PagerState,
    scope: CoroutineScope, isFirstTime: MutableState<Boolean>,
) {
    LaunchedEffect(Unit) {
        tagsVM.dataType.value = notesVM.dataType
        scope.launch(Dispatchers.IO) { notesVM.loadAsync(tagsVM) }
    }

    LaunchedEffect(notesVM.selectMode.value) {
        if (notesVM.selectMode.value) scrollBehavior.reset()
    }

    LaunchedEffect(pagerState.currentPage) {
        if (isFirstTime.value) { isFirstTime.value = false; return@LaunchedEffect }
        val tagsState = tagsVM.itemsFlow.value
        when (val index = pagerState.currentPage) {
            0 -> { notesVM.trash.value = false; notesVM.tag.value = null }
            1 -> { notesVM.trash.value = true; notesVM.tag.value = null }
            else -> { notesVM.trash.value = false; notesVM.tag.value = tagsState.getOrNull(index - 2) }
        }
        scope.launch { scrollBehavior.reset(); scrollStateMap[pagerState.currentPage]?.scrollToItem(0) }
        scope.launch(Dispatchers.IO) { notesVM.loadAsync(tagsVM) }
    }
}
