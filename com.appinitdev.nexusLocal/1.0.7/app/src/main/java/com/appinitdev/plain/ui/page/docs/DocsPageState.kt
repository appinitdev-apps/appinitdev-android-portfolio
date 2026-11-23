
@OptIn(ExperimentalMaterial3Api::class)
data class DocsPageState(
    val pagerState: PagerState,
    val itemsState: List<DDoc>,
    val dragSelectState: DragSelectState,
    val scrollBehavior: TopAppBarScrollBehavior,
    val tagsState: List<DTag>,
    val tagsMapState: Map<String, List<DTagRelation>>,
    val scrollState: LazyListState,
    val bucketsMap: Map<String, DMediaBucket>,
) {
    companion object {
        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun create(
            docsVM: DocsViewModel,
            tagsVM: TagsViewModel,
            mediaFoldersVM: MediaFoldersViewModel,
        ): DocsPageState {
            LaunchedEffect(Unit) {
                tagsVM.dataType.value = docsVM.dataType
                mediaFoldersVM.dataType.value = docsVM.dataType
            }

            val tagsState by tagsVM.itemsFlow.collectAsState()
            val pagerState = rememberPagerState(pageCount = { docsVM.tabs.value.size })
            val itemsState by docsVM.itemsFlow.collectAsState()
            val scrollState = rememberLazyListState()
            val dragSelectState = rememberListDragSelectState({ docsVM.scrollStateMap[pagerState.currentPage] })
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
                canScroll = {
                    (docsVM.scrollStateMap[pagerState.currentPage]?.firstVisibleItemIndex ?: 0) > 0 && !dragSelectState.selectMode
                }
            )
            val tagsMapState by tagsVM.tagsMapFlow.collectAsState()
            val bucketsMap by mediaFoldersVM.bucketsMapFlow.collectAsState()

            return DocsPageState(
                pagerState = pagerState,
                itemsState = itemsState,
                dragSelectState = dragSelectState,
                scrollBehavior = scrollBehavior,
                tagsState = tagsState,
                tagsMapState = tagsMapState,
                scrollState = scrollState,
                bucketsMap = bucketsMap,
            )
        }
    }
}
