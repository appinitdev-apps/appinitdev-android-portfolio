@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VideosPageEffects(
    context: Context, videosVM: VideosViewModel, tagsVM: TagsViewModel,
    mediaFoldersVM: MediaFoldersViewModel, scrollBehavior: TopAppBarScrollBehavior,
    pagerState: PagerState, scope: CoroutineScope,
    dragSelectState: DragSelectState, previewerState: MediaPreviewerState,
    cellsPerRow: MutableState<Int>, isFirstTime: MutableState<Boolean>,
    tabs: List<VTabData>, tagsState: List<DTag>,
) {
    LaunchedEffect(Unit) {
        videosVM.hasPermission.value = AppFeatureType.FILES.hasPermission(context)
        if (videosVM.hasPermission.value) {
            scope.launch(Dispatchers.IO) {
                cellsPerRow.value = VideoGridCellsPerRowPreference.getAsync()
                videosVM.sortBy.value = VideoSortByPreference.getValueAsync()
                videosVM.loadAsync(context, tagsVM); mediaFoldersVM.loadAsync(context)
            }
        }
    }
    LaunchedEffect(Channel.sharedFlow) {
        Channel.sharedFlow.collect { event ->
            when (event) {
                is PermissionsResultEvent -> {
                    videosVM.hasPermission.value = AppFeatureType.FILES.hasPermission(context)
                    scope.launch(Dispatchers.IO) { videosVM.sortBy.value = VideoSortByPreference.getValueAsync(); videosVM.loadAsync(context, tagsVM) }
                }
            }
        }
    }
    LaunchedEffect(dragSelectState.selectMode, (previewerState.visible && !context.isGestureInteractionMode())) {
        if (dragSelectState.selectMode || (previewerState.visible && !context.isGestureInteractionMode())) scrollBehavior.reset()
    }
    LaunchedEffect(pagerState.currentPage) {
        if (isFirstTime.value) { isFirstTime.value = false; return@LaunchedEffect }
        val tab = tabs.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        when (tab.value) {
            "all" -> { videosVM.trash.value = false; videosVM.tag.value = null }
            "trash" -> { videosVM.trash.value = true; videosVM.tag.value = null }
            else -> { videosVM.trash.value = false; videosVM.tag.value = tagsState.find { it.id == tab.value } }
        }
        scope.launch { scrollBehavior.reset(); videosVM.scrollStateMap[pagerState.currentPage]?.scrollToItem(0) }
        scope.launch(Dispatchers.IO) { videosVM.loadAsync(context, tagsVM) }
    }
}
