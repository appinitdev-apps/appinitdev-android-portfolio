@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosPage(
    navController: NavHostController,
    videosVM: VideosViewModel = viewModel(key = ""),
    tagsVM: TagsViewModel = viewModel(key = ""),
    mediaFoldersVM: MediaFoldersViewModel = viewModel(key = ""),
    castVM: CastViewModel = viewModel(key = ""),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val videosState = VideosPageState.create(videosVM, tagsVM, mediaFoldersVM)
    val itemsState = videosState.itemsState
    val configuration = LocalConfiguration.current
    val pagerState = videosState.pagerState
    val scrollBehavior = videosState.scrollBehavior
    val tagsState = videosState.tagsState
    val previewerState = videosState.previewerState
    val tagsMapState = videosState.tagsMapState
    val dragSelectState = videosState.dragSelectState
    val cellsPerRow = videosState.cellsPerRow
    val isFirstTime = remember { mutableStateOf(true) }
    val density = LocalDensity.current
    val imageWidthPx = remember(cellsPerRow.value) { density.run { ((configuration.screenWidthDp.dp - ((cellsPerRow.value - 1) * 2).dp) / cellsPerRow.value).toPx().toInt() } }
    val tabs = remember(tagsState, videosVM.total.intValue, videosVM.totalTrash.intValue) {
        val baseTabs = mutableListOf(VTabData(LocaleHelper.getString(Res.string.all), "all", videosVM.total.intValue))
        if (AppFeatureType.MEDIA_TRASH.has()) baseTabs.add(VTabData(LocaleHelper.getString(Res.string.trash), "trash", videosVM.totalTrash.intValue))
        baseTabs.addAll(tagsState.map { VTabData(it.name, it.id, it.count) }); baseTabs
    }
    val topRefreshLayoutState = rememberRefreshLayoutState { scope.launch { withIO { videosVM.loadAsync(context, tagsVM); mediaFoldersVM.loadAsync(context) }; setRefreshState(RefreshContentState.Finished) } }
    BackHandler(enabled = previewerState.visible || dragSelectState.selectMode || castVM.castMode.value || videosVM.showSearchBar.value) {
        when {
            previewerState.visible -> scope.launch { previewerState.closeTransform() }
            dragSelectState.selectMode -> dragSelectState.exitSelectMode()
            castVM.castMode.value -> castVM.exitCastMode()
            videosVM.showSearchBar.value && (!videosVM.searchActive.value || videosVM.queryText.value.isEmpty()) -> {
                videosVM.exitSearchMode(); videosVM.showLoading.value = true
                scope.launch(Dispatchers.IO) { videosVM.loadAsync(context, tagsVM) }
            }
        }
    }
    VideosPageEffects(context, videosVM, tagsVM, mediaFoldersVM, scrollBehavior, pagerState, scope, dragSelectState, previewerState, cellsPerRow, isFirstTime, tabs, tagsState)
    ViewVideoBottomSheet(videosVM, tagsVM, tagsMapState, tagsState, dragSelectState)
    MediaFoldersBottomSheet(videosVM, mediaFoldersVM, tagsVM)
    if (videosVM.showTagsDialog.value) { TagsBottomSheet(tagsVM) { videosVM.showTagsDialog.value = false } }
    CastDialog(castVM)
    PScaffold(
        topBar = {
            MediaTopBar(
                navController = navController,
                mediaVM = videosVM,
                tagsVM = tagsVM,
                castVM = castVM,
                dragSelectState = dragSelectState,
                scrollBehavior = scrollBehavior,
                bucketsMap = videosState.bucketsMap,
                itemsState = itemsState,
                scrollToTop = { scope.launch { videosVM.scrollStateMap[pagerState.currentPage]?.scrollToItem(0) } },
                defaultNavigationIcon = { NavigationBackIcon { navController.popBackStack() } },
                onSortSelected = { _, sortBy ->
                    scope.launch(Dispatchers.IO) { VideoSortByPreference.putAsync(sortBy); videosVM.sortBy.value = sortBy; videosVM.loadAsync(context, tagsVM) }
                },
                onSearchAction = { ctx, tv -> scope.launch(Dispatchers.IO) { videosVM.loadAsync(ctx, tv) } },
            )
        },
        bottomBar = {
            AnimatedBottomAction(visible = dragSelectState.showBottomActions()) {
                VideoFilesSelectModeBottomActions(videosVM, tagsVM, tagsState, dragSelectState)
            }
        },
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())) {
            if (!videosVM.hasPermission.value) { NeedPermissionColumn(Res.drawable.video, AppFeatureType.FILES.getPermission()!!); return@PScaffold }
            if (!dragSelectState.selectMode) {
                PScrollableTabRow(selectedTabIndex = pagerState.currentPage, modifier = Modifier.fillMaxWidth()) {
                    tabs.forEachIndexed { index, s ->
                        PFilterChip(modifier = Modifier.padding(start = if (index == 0) 0.dp else 8.dp), selected = pagerState.currentPage == index, onClick = { scope.launch { pagerState.scrollToPage(index) } },
                            label = { if (index == 0) Text(text = s.title + " (" + s.count + ")") else Text(if (videosVM.bucketId.value.isNotEmpty() || videosVM.queryText.value.isNotEmpty()) s.title else "${s.title} (${s.count})") })
                    }
                }
            }
            VideosPageGrid(context, videosVM, tagsVM, castVM, itemsState, pagerState, scrollBehavior, topRefreshLayoutState, dragSelectState, previewerState, cellsPerRow, imageWidthPx, scope, paddingValues)
        }
    }
    MediaPreviewer(
        state = previewerState,
        tagsVM = tagsVM,
        tagsMap = tagsMapState,
        tagsState = tagsState,
        onRenamed = { scope.launch(Dispatchers.IO) { videosVM.loadAsync(context, tagsVM) } },
        deleteAction = { item -> scope.launch(Dispatchers.IO) { videosVM.delete(context, tagsVM, setOf(item.mediaId)); previewerState.closeTransform() } },
        onTagsChanged = {},
    )
}
