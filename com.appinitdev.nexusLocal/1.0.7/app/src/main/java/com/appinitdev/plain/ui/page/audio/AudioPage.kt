@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AudioPage(
    navController: NavHostController,
    audioPlaylistVM: AudioPlaylistViewModel,
    audioVM: AudioViewModel = viewModel(key = ""),
    tagsVM: TagsViewModel = viewModel(key = ""),
    mediaFoldersVM: MediaFoldersViewModel = viewModel(key = ""),
    castVM: CastViewModel = viewModel(key = ""),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val audioState = AudioPageState.create(audioVM, tagsVM, mediaFoldersVM)
    val pagerState = audioState.pagerState
    val scrollBehavior = audioState.scrollBehavior
    val tagsState = audioState.tagsState
    val tagsMapState = audioState.tagsMapState
    val dragSelectState = audioState.dragSelectState
    val itemsState = audioState.itemsState
    val scrollState = audioState.scrollState
    val isAudioPlaying by AudioPlayer.isPlayingFlow.collectAsState()
    val tabs = remember(tagsState, audioVM.total.intValue, audioVM.totalTrash.intValue) {
        val baseTabs = mutableListOf(VTabData(LocaleHelper.getString(Res.string.all), "all", audioVM.total.intValue))
        if (AppFeatureType.MEDIA_TRASH.has()) baseTabs.add(VTabData(LocaleHelper.getString(Res.string.trash), "trash", audioVM.totalTrash.intValue))
        baseTabs.addAll(tagsState.map { VTabData(it.name, it.id, it.count) })
        baseTabs
    }
    val topRefreshLayoutState = rememberRefreshLayoutState {
        scope.launch {
            withIO { audioVM.loadAsync(context, tagsVM); audioPlaylistVM.loadAsync(context); mediaFoldersVM.loadAsync(context) }
            setRefreshState(RefreshContentState.Finished)
        }
    }
    BackHandler(enabled = dragSelectState.selectMode || castVM.castMode.value || audioVM.showSearchBar.value) {
        when {
            dragSelectState.selectMode -> dragSelectState.exitSelectMode()
            castVM.castMode.value -> castVM.exitCastMode()
            audioVM.showSearchBar.value && (!audioVM.searchActive.value || audioVM.queryText.value.isEmpty()) -> {
                audioVM.exitSearchMode()
                audioVM.showLoading.value = true
                scope.launch(Dispatchers.IO) { audioVM.loadAsync(context, tagsVM) }
            }
        }
    }
    AudioPageEffects(audioState, audioVM, audioPlaylistVM, tagsVM, mediaFoldersVM)
    LaunchedEffect(pagerState.currentPage) {
        val tab = tabs.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        when (tab.value) {
            "all" -> {
                audioVM.trash.value = false; audioVM.tag.value = null
            }
            "trash" -> {
                audioVM.trash.value = true; audioVM.tag.value = null
            }
            else -> {
                audioVM.trash.value = false; audioVM.tag.value = tagsState.find { it.id == tab.value }
            }
        }
        scope.launch { scrollBehavior.reset(); audioVM.scrollStateMap[pagerState.currentPage]?.scrollToItem(0) ?: scrollState.scrollToItem(0) }
        scope.launch(Dispatchers.IO) { audioVM.loadAsync(context, tagsVM) }
    }
    val audioTagsMap = remember(tagsMapState, tagsState) {
        tagsMapState.mapValues { entry -> entry.value.mapNotNull { relation -> tagsState.find { it.id == relation.tagId } } }
    }
    ViewAudioBottomSheet(audioVM = audioVM, tagsVM = tagsVM, tagsMapState = tagsMapState, tagsState = tagsState, dragSelectState = dragSelectState, castVM = castVM)
    MediaFoldersBottomSheet(audioVM, mediaFoldersVM, tagsVM)
    if (audioVM.showTagsDialog.value) {
        TagsBottomSheet(tagsVM) { audioVM.showTagsDialog.value = false }
    }
    CastDialog(castVM)
    PScaffold(
        topBar = {
            MediaTopBar(
                navController = navController,
                mediaVM = audioVM,
                tagsVM = tagsVM,
                castVM = castVM,
                dragSelectState = dragSelectState,
                scrollBehavior = scrollBehavior,
                bucketsMap = audioState.bucketsMap,
                itemsState = itemsState,
                scrollToTop = { scope.launch { audioVM.scrollStateMap[pagerState.currentPage]?.scrollToItem(0) } },
                defaultNavigationIcon = { NavigationBackIcon { navController.popBackStack() } },
                onSortSelected = { _, sortBy ->
                    scope.launch(Dispatchers.IO) {
                        AudioSortByPreference.putAsync(sortBy)
                        audioVM.sortBy.value = sortBy
                        audioVM.loadAsync(context, tagsVM)
                    }
                },
                onSearchAction = { ctx, tv ->
                    scope.launch(Dispatchers.IO) {
                        audioVM.loadAsync(ctx, tv)
                    }
                },
            )
        },
        bottomBar = {
            AnimatedBottomAction(visible = dragSelectState.showBottomActions()) {
                AudioFilesSelectModeBottomActions(audioVM, audioPlaylistVM, tagsVM, tagsState, dragSelectState)
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (!audioVM.hasPermission.value) {
                    NeedPermissionColumn(Res.drawable.music, AppFeatureType.FILES.getPermission()!!); return@Column
                }
                if (!dragSelectState.selectMode) {
                    PScrollableTabRow(selectedTabIndex = pagerState.currentPage, modifier = Modifier.fillMaxWidth()) {
                        tabs.forEachIndexed { index, s ->
                            PFilterChip(
                                modifier = Modifier.padding(start = if (index == 0) 0.dp else 8.dp),
                                selected = pagerState.currentPage == index,
                                onClick = { scope.launch { pagerState.scrollToPage(index) } },
                                label = { Text(if (index == 0) s.title + " (" + s.count + ")" else if (audioVM.bucketId.value.isNotEmpty() || audioVM.queryText.value.isNotEmpty()) s.title else "${s.title} (${s.count})") })
                        }
                    }
                }
                AudioPageList(
                    pagerState, scrollBehavior, dragSelectState, itemsState, audioVM, audioPlaylistVM,
                    tagsVM, castVM, audioTagsMap, isAudioPlaying, topRefreshLayoutState, paddingValues
                )
            }
            AudioPlayerBar(audioPlaylistVM, castVM, modifier = Modifier.align(Alignment.BottomCenter), dragSelectState = audioState.dragSelectState)
            AudioCastPlayerBar(castVM = castVM, modifier = Modifier.align(Alignment.BottomCenter), dragSelectState = audioState.dragSelectState)
        }
    }
}
