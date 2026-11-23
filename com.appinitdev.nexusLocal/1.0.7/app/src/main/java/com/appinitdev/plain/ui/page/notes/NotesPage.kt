
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotesPage(navController: NavHostController, notesVM: NotesViewModel, tagsVM: TagsViewModel) {
    val itemsState by notesVM.itemsFlow.collectAsState()
    val tagsState by tagsVM.itemsFlow.collectAsState()
    val tagsMapState by tagsVM.tagsMapFlow.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollStateMap = remember { mutableStateMapOf<Int, LazyListState>() }
    val pagerState = rememberPagerState(pageCount = { tagsState.size + 2 })
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(canScroll = { (scrollStateMap[pagerState.currentPage]?.firstVisibleItemIndex ?: 0) > 0 && !notesVM.selectMode.value })
    val isFirstTime = remember { mutableStateOf(true) }
    val tabs = remember(tagsState, notesVM.total.intValue, notesVM.totalTrash.intValue) {
        listOf(VTabData(LocaleHelper.getString(Res.string.all), "all", notesVM.total.intValue), VTabData(LocaleHelper.getString(Res.string.trash), "trash", notesVM.totalTrash.intValue), *tagsState.map { VTabData(it.name, it.id, it.count) }.toTypedArray())
    }
    val topRefreshLayoutState = rememberRefreshLayoutState { scope.launch { withIO { notesVM.loadAsync(tagsVM) } }; setRefreshState(RefreshContentState.Finished) }

    NotesPageEffects(notesVM, tagsVM, scrollBehavior, scrollStateMap, pagerState, scope, isFirstTime)

    val pageTitle = if (notesVM.selectMode.value) LocaleHelper.getStringF(Res.string.x_selected, "count", notesVM.selectedIds.size)
        else if (notesVM.tag.value != null) stringResource(Res.string.notes) + " - " + notesVM.tag.value!!.name
        else if (notesVM.trash.value) stringResource(Res.string.notes) + " - " + stringResource(Res.string.trash)
        else stringResource(Res.string.notes)
    ViewNoteBottomSheet(notesVM, tagsVM, tagsMapState, tagsState)
    if (notesVM.showTagsDialog.value) { TagsBottomSheet(tagsVM) { notesVM.showTagsDialog.value = false } }
    val onSearch: (String) -> Unit = { notesVM.searchActive.value = false; notesVM.showLoading.value = true; scope.launch { scrollStateMap[pagerState.currentPage]?.scrollToItem(0) }; scope.launch(Dispatchers.IO) { notesVM.loadAsync(tagsVM) } }
    BackHandler(enabled = notesVM.selectMode.value || notesVM.showSearchBar.value) {
        if (notesVM.selectMode.value) notesVM.exitSelectMode()
        else if (notesVM.showSearchBar.value) { if (!notesVM.searchActive.value || notesVM.queryText.value.isEmpty()) { notesVM.exitSearchMode(); onSearch("") } }
    }

    PScaffold(
        topBar = {
            if (notesVM.showSearchBar.value) { ListSearchBar(viewModel = notesVM, onSearch = onSearch); return@PScaffold }
            PTopAppBar(modifier = Modifier.combinedClickable(onClick = {}, onDoubleClick = { scope.launch { scrollStateMap[pagerState.currentPage]?.scrollToItem(0) } }),
                navController = navController, navigationIcon = { if (notesVM.selectMode.value) NavigationCloseIcon { notesVM.exitSelectMode() } else NavigationBackIcon { navController.navigateUp() } },
                title = pageTitle, scrollBehavior = scrollBehavior, actions = {
                    if (notesVM.selectMode.value) { PTopRightButton(label = stringResource(if (notesVM.isAllSelected()) Res.string.unselect_all else Res.string.select_all), click = { notesVM.toggleSelectAll() }); HorizontalSpace(dp = 8.dp) }
                    else { ActionButtonSearch { notesVM.enterSearchMode() }; ActionButtonTags { notesVM.showTagsDialog.value = true } }
                })
        },
        bottomBar = { AnimatedVisibility(visible = notesVM.showBottomActions(), enter = slideInVertically { it }, exit = slideOutVertically { it }) { NotesSelectModeBottomActions(notesVM, tagsVM, tagsState) } },
        floatingActionButton = if (notesVM.selectMode.value) null else { { PDraggableElement { FloatingActionButton(onClick = { navController.navigate(Routing.NotesCreate(notesVM.tag.value?.id ?: "")) }) { Icon(painter = painterResource(Res.drawable.plus), stringResource(Res.string.add)) } } } },
    ) { paddingValues ->
        Column(Modifier.padding(top = paddingValues.calculateTopPadding())) {
            if (!notesVM.selectMode.value) {
                PScrollableTabRow(selectedTabIndex = pagerState.currentPage, modifier = Modifier.fillMaxWidth()) {
                    tabs.forEachIndexed { index, s ->
                        PFilterChip(modifier = Modifier.padding(start = if (index == 0) 0.dp else 8.dp), selected = pagerState.currentPage == index, onClick = { scope.launch { pagerState.scrollToPage(index) } },
                            label = { if (index < 2) Text(text = s.title + " (" + s.count + ")") else Text(if (notesVM.queryText.value.isNotEmpty()) s.title else "${s.title} (${s.count})") })
                    }
                }
            }
            HorizontalPager(state = pagerState, userScrollEnabled = false) { index ->
                NotesPageContent(notesVM, tagsVM, itemsState, tagsState, tagsMapState, scrollStateMap, index, scrollBehavior, topRefreshLayoutState, navController, paddingValues.calculateBottomPadding(), tabs, scope, pagerState)
            }
        }
    }
}
