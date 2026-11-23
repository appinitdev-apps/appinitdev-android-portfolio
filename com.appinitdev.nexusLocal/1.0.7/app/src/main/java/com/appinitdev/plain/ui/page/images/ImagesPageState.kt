@OptIn(ExperimentalMaterial3Api::class)
data class ImagesPageState(
    val pagerState: PagerState,
    val itemsState: List<DImage>,
    val dragSelectState: DragSelectState,
    val scrollBehavior: TopAppBarScrollBehavior,
    val previewerState: MediaPreviewerState,
    val tagsState: List<DTag>,
    val tagsMapState: Map<String, List<DTagRelation>>,
    val bucketsMap: Map<String, DMediaBucket>,
    val cellsPerRow: MutableState<Int>
) {
    companion object {
        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun create(
            imagesVM: ImagesViewModel,
            tagsVM: TagsViewModel,
            imageFoldersVM: MediaFoldersViewModel,
        ): ImagesPageState {
            LaunchedEffect(Unit) {
                tagsVM.dataType.value = imagesVM.dataType
                imageFoldersVM.dataType.value = imagesVM.dataType
            }
            val tagsState by tagsVM.itemsFlow.collectAsState()
            val pagerState = rememberPagerState(pageCount = { 
                tagsState.size + if (AppFeatureType.MEDIA_TRASH.has()) 2 else 1 
            })
            val itemsState by imagesVM.itemsFlow.collectAsState()
            val dragSelectState = rememberDragSelectState({ imagesVM.scrollStateMap[pagerState.currentPage] })
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(canScroll = {
                (imagesVM.scrollStateMap[pagerState.currentPage]?.firstVisibleItemIndex ?: 0) > 0 && !dragSelectState.selectMode
            })
            val previewerState = rememberPreviewerState()
            val tagsMapState by tagsVM.tagsMapFlow.collectAsState()
            val bucketsMap by imageFoldersVM.bucketsMapFlow.collectAsState()
            val cellsPerRow = remember { mutableIntStateOf(ImageGridCellsPerRowPreference.default) }
            return ImagesPageState(
                pagerState = pagerState,
                itemsState = itemsState,
                dragSelectState = dragSelectState,
                scrollBehavior = scrollBehavior,
                previewerState = previewerState,
                tagsState = tagsState,
                tagsMapState = tagsMapState,
                bucketsMap = bucketsMap,
                cellsPerRow = cellsPerRow
            )
        }
    }
}