
@OptIn(ExperimentalMaterial3Api::class)
data class VideosPageState(
    val pagerState: PagerState,
    val itemsState: List<DVideo>,
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
            videosVM: VideosViewModel,
            tagsVM: TagsViewModel,
            mediaFoldersVM: MediaFoldersViewModel,
        ): VideosPageState {
            LaunchedEffect(Unit) {
                mediaFoldersVM.dataType.value = videosVM.dataType
                tagsVM.dataType.value = videosVM.dataType
            }
            val tagsState by tagsVM.itemsFlow.collectAsState()
            val pagerState = rememberPagerState(pageCount = { 
                tagsState.size + if (AppFeatureType.MEDIA_TRASH.has()) 2 else 1 
            })
            val itemsState by videosVM.itemsFlow.collectAsState()
            val dragSelectState = rememberDragSelectState({ videosVM.scrollStateMap[pagerState.currentPage] })
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(canScroll = {
                (videosVM.scrollStateMap[pagerState.currentPage]?.firstVisibleItemIndex ?: 0) > 0 && !dragSelectState.selectMode
            })
            val previewerState = rememberPreviewerState()
            val tagsMapState by tagsVM.tagsMapFlow.collectAsState()
            val bucketsMap by mediaFoldersVM.bucketsMapFlow.collectAsState()
            val cellsPerRow = remember { mutableIntStateOf(VideoGridCellsPerRowPreference.default) }

            return VideosPageState(
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