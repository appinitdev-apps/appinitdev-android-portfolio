@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImagesPageEffects(
    context: Context, imagesVM: ImagesViewModel, tagsVM: TagsViewModel,
    mediaFoldersVM: MediaFoldersViewModel, scrollBehavior: TopAppBarScrollBehavior,
    pagerState: PagerState, scope: CoroutineScope,
    dragSelectState: DragSelectState, previewerState: MediaPreviewerState,
    cellsPerRow: MutableState<Int>, isFirstTime: MutableState<Boolean>,
    tabs: List<VTabData>, tagsState: List<DTag>,
) {
    LaunchedEffect(Unit) {
        imagesVM.hasPermission.value = AppFeatureType.FILES.hasPermission(context)
        if (imagesVM.hasPermission.value) {
            scope.launch(Dispatchers.IO) {
                cellsPerRow.value = ImageGridCellsPerRowPreference.getAsync()
                imagesVM.sortBy.value = ImageSortByPreference.getValueAsync()
                imagesVM.loadAsync(context, tagsVM); mediaFoldersVM.loadAsync(context)
            }
        }
    }
    LaunchedEffect(Channel.sharedFlow) {
        Channel.sharedFlow.collect { event ->
            when (event) {
                is PermissionsResultEvent -> {
                    imagesVM.hasPermission.value = AppFeatureType.FILES.hasPermission(context)
                    scope.launch(Dispatchers.IO) { imagesVM.sortBy.value = ImageSortByPreference.getValueAsync(); imagesVM.loadAsync(context, tagsVM) }
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
            "all" -> { imagesVM.trash.value = false; imagesVM.tag.value = null }
            "trash" -> { imagesVM.trash.value = true; imagesVM.tag.value = null }
            else -> { imagesVM.trash.value = false; imagesVM.tag.value = tagsState.find { it.id == tab.value } }
        }
        scope.launch { scrollBehavior.reset(); imagesVM.scrollStateMap[pagerState.currentPage]?.scrollToItem(0) }
        scope.launch(Dispatchers.IO) { imagesVM.loadAsync(context, tagsVM) }
    }
}
