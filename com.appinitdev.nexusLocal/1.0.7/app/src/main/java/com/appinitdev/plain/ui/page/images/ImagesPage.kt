
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagesPage(
    navController: NavHostController,
    imagesVM: ImagesViewModel = viewModel(key = ""),
    tagsVM: TagsViewModel = viewModel(key = ""),
    mediaFoldersVM: MediaFoldersViewModel = viewModel(key = ""),
    castVM: CastViewModel = viewModel(key = ""),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val imagesState = ImagesPageState.create(imagesVM, tagsVM, mediaFoldersVM)
    val itemsState = imagesState.itemsState
    val configuration = LocalConfiguration.current
    val pagerState = imagesState.pagerState
    val scrollBehavior = imagesState.scrollBehavior
    val tagsState = imagesState.tagsState
    val previewerState = imagesState.previewerState
    val tagsMapState = imagesState.tagsMapState
    val dragSelectState = imagesState.dragSelectState
    val cellsPerRow = imagesState.cellsPerRow
    val isFirstTime = remember { mutableStateOf(true) }
    val density = LocalDensity.current
    val imageWidthPx = remember(cellsPerRow.value) { density.run { ((configuration.screenWidthDp.dp - ((cellsPerRow.value - 1) * 2).dp) / cellsPerRow.value).toPx().toInt() } }
    val tabs = remember(tagsState, imagesVM.total.intValue, imagesVM.totalTrash.intValue) {
        val baseTabs = mutableListOf(VTabData(LocaleHelper.getString(Res.string.all), "all", imagesVM.total.intValue))
        if (AppFeatureType.MEDIA_TRASH.has()) baseTabs.add(VTabData(LocaleHelper.getString(Res.string.trash), "trash", imagesVM.totalTrash.intValue))
        baseTabs.addAll(tagsState.map { VTabData(it.name, it.id, it.count) }); baseTabs
    }
    val topRefreshLayoutState = rememberRefreshLayoutState { scope.launch { withIO { imagesVM.loadWithAiSearchAsync(context, tagsVM); mediaFoldersVM.loadAsync(context) }; setRefreshState(RefreshContentState.Finished) } }

    BackHandler(enabled = previewerState.visible || dragSelectState.selectMode || castVM.castMode.value || imagesVM.showSearchBar.value) {
        when {
            previewerState.visible -> scope.launch { previewerState.closeTransform() }
            dragSelectState.selectMode -> dragSelectState.exitSelectMode()
            castVM.castMode.value -> castVM.exitCastMode()
            imagesVM.showSearchBar.value && (!imagesVM.searchActive.value || imagesVM.queryText.value.isEmpty()) -> {
                imagesVM.exitSearchMode(); imagesVM.showLoading.value = true
                scope.launch(Dispatchers.IO) { imagesVM.loadAsync(context, tagsVM) }
            }
        }
    }

    ImagesPageEffects(context, imagesVM, tagsVM, mediaFoldersVM, scrollBehavior, pagerState, scope, dragSelectState, previewerState, cellsPerRow, isFirstTime, tabs, tagsState)

    ViewImageBottomSheet(imagesVM, tagsVM, tagsMapState, tagsState, dragSelectState)
    MediaFoldersBottomSheet(imagesVM, mediaFoldersVM, tagsVM)
    if (imagesVM.showTagsDialog.value) { TagsBottomSheet(tagsVM) { imagesVM.showTagsDialog.value = false } }
    CastDialog(castVM)

    PScaffold(
        topBar = {
            MediaTopBar(
                navController = navController,
                mediaVM = imagesVM,
                tagsVM = tagsVM,
                castVM = castVM,
                dragSelectState = dragSelectState,
                scrollBehavior = scrollBehavior,
                bucketsMap = imagesState.bucketsMap,
                itemsState = itemsState,
                scrollToTop = { scope.launch { imagesVM.scrollStateMap[pagerState.currentPage]?.scrollToItem(0) } },
                defaultNavigationIcon = { NavigationBackIcon { navController.popBackStack() } },
                onSortSelected = { _, sortBy ->
                    scope.launch(Dispatchers.IO) { ImageSortByPreference.putAsync(sortBy); imagesVM.sortBy.value = sortBy; imagesVM.loadAsync(context, tagsVM) }
                },
                onSearchAction = { ctx, tv -> scope.launch(Dispatchers.IO) { imagesVM.loadWithAiSearchAsync(ctx, tv) } },
            )
        },
        bottomBar = {
            AnimatedBottomAction(visible = dragSelectState.showBottomActions()) {
                ImageFilesSelectModeBottomActions(imagesVM, tagsVM, tagsState, dragSelectState)
            }
        },
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())) {
            if (!imagesVM.hasPermission.value) { NeedPermissionColumn(Res.drawable.image, AppFeatureType.FILES.getPermission()!!); return@PScaffold }
            if (!dragSelectState.selectMode) {
                PScrollableTabRow(selectedTabIndex = pagerState.currentPage, modifier = Modifier.fillMaxWidth()) {
                    tabs.forEachIndexed { index, s ->
                        PFilterChip(modifier = Modifier.padding(start = if (index == 0) 0.dp else 8.dp), selected = pagerState.currentPage == index, onClick = { scope.launch { pagerState.scrollToPage(index) } },
                            label = { if (index == 0) Text(text = s.title + " (" + s.count + ")") else Text(if (imagesVM.bucketId.value.isNotEmpty() || imagesVM.queryText.value.isNotEmpty()) s.title else "${s.title} (${s.count})") })
                    }
                }
            }
            ImagesPageGrid(context, imagesVM, tagsVM, castVM, itemsState, pagerState, scrollBehavior, topRefreshLayoutState, dragSelectState, previewerState, cellsPerRow, imageWidthPx, scope, paddingValues)
        }
    }

    MediaPreviewer(
        state = previewerState,
        tagsVM = tagsVM,
        tagsMap = tagsMapState,
        tagsState = tagsState,
        onRenamed = { scope.launch(Dispatchers.IO) { imagesVM.loadAsync(context, tagsVM) } },
        deleteAction = { item -> scope.launch(Dispatchers.IO) { imagesVM.delete(context, tagsVM, setOf(item.mediaId)); previewerState.closeTransform() } },
        onTagsChanged = {},
    )
}
