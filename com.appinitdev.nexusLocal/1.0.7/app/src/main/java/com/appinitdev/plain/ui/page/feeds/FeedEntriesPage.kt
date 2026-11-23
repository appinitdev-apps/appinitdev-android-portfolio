@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FeedEntriesPage(
    navController: NavHostController, feedId: String, tagsVM: TagsViewModel,
    feedEntriesVM: FeedEntriesViewModel = viewModel(), feedsVM: FeedsViewModel = viewModel(),
) {
    val feedsState by feedsVM.itemsFlow.collectAsState()
    val feedsMap = remember(feedsState) { derivedStateOf { feedsState.associateBy { it.id } } }
    val tagsState by tagsVM.itemsFlow.collectAsState()
    val tagsMapState by tagsVM.tagsMapFlow.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollStateMap = remember { mutableStateMapOf<Int, LazyListState>() }
    val pagerState = rememberPagerState(pageCount = { tagsState.size + 2 })
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(canScroll = {
        (scrollStateMap[pagerState.currentPage]?.firstVisibleItemIndex ?: 0) > 0 && !feedEntriesVM.selectMode.value
    })
    val isFirstTime = remember { mutableStateOf(true) }
    val tabs = remember(tagsState, feedEntriesVM.total.intValue) {
        listOf(VTabData(LocaleHelper.getString(Res.string.all), "all", feedEntriesVM.total.intValue),
            VTabData(LocaleHelper.getString(Res.string.today), "today", feedEntriesVM.totalToday.value),
            *tagsState.map { VTabData(it.name, it.id, it.count) }.toTypedArray())
    }
    val topRefreshLayoutState = rememberRefreshLayoutState { scope.launch { feedEntriesVM.sync() } }
    val onSearch: (String) -> Unit = {
        feedEntriesVM.searchActive.value = false; feedEntriesVM.showLoading.value = true
        scope.launch { scrollStateMap[pagerState.currentPage]?.scrollToItem(0) }
        scope.launch(Dispatchers.IO) { feedEntriesVM.loadAsync(tagsVM) }
    }
    FeedEntriesPageEffects(feedEntriesVM, feedsVM, tagsVM, feedId, scope, tabs, pagerState, scrollBehavior, scrollStateMap, topRefreshLayoutState, isFirstTime, onSearch)
    val feed = if (feedEntriesVM.feedId.value.isEmpty()) null else feedsMap.value[feedEntriesVM.feedId.value]
    val feedName = feed?.name ?: stringResource(Res.string.feeds)
    val pageTitle = if (feedEntriesVM.selectMode.value) LocaleHelper.getStringF(Res.string.x_selected, "count", feedEntriesVM.selectedIds.size)
        else if (feedEntriesVM.tag.value != null) listOf(feedName, feedEntriesVM.tag.value!!.name).joinToString(" - ")
        else if (feedEntriesVM.filterType == FeedEntryFilterType.TODAY) feedName + " - " + stringResource(Res.string.today) else feedName
    ViewFeedEntryBottomSheet(feedEntriesVM, tagsVM, tagsMapState, tagsState)
    if (feedEntriesVM.showTagsDialog.value) { TagsBottomSheet(tagsVM) { feedEntriesVM.showTagsDialog.value = false } }
    PScaffold(topBar = {
        if (feedEntriesVM.showSearchBar.value) { ListSearchBar(viewModel = feedEntriesVM, onSearch = onSearch); return@PScaffold }
        PTopAppBar(modifier = Modifier.combinedClickable(onClick = {}, onDoubleClick = { scope.launch { scrollStateMap[pagerState.currentPage]?.scrollToItem(0) } }),
            navController = navController, navigationIcon = {
                if (feedEntriesVM.selectMode.value) NavigationCloseIcon { feedEntriesVM.exitSelectMode() } else NavigationBackIcon { navController.navigateUp() }
            }, title = pageTitle, scrollBehavior = scrollBehavior, actions = {
                if (feedEntriesVM.selectMode.value) { PTopRightButton(label = stringResource(if (feedEntriesVM.isAllSelected()) Res.string.unselect_all else Res.string.select_all), click = { feedEntriesVM.toggleSelectAll() }); HorizontalSpace(dp = 8.dp) }
                else {
                    ActionButtonSearch { feedEntriesVM.enterSearchMode() }
                    if (feedEntriesVM.feedId.value.isEmpty()) { PIconButton(icon = Res.drawable.rss, contentDescription = stringResource(Res.string.subscriptions), tint = MaterialTheme.colorScheme.onSurface) { navController.navigate(Routing.Feeds) } }
                    ActionButtonMoreWithMenu { dismiss -> PDropdownMenuItemTags(onClick = { dismiss(); feedEntriesVM.showTagsDialog.value = true })
                        if (feedEntriesVM.feedId.value.isEmpty()) PDropdownMenuItemSettings(onClick = { dismiss(); navController.navigate(Routing.FeedSettings) }) }
                }
            })
    }, bottomBar = {
        AnimatedVisibility(visible = feedEntriesVM.showBottomActions(), enter = slideInVertically { it }, exit = slideOutVertically { it }) {
            FeedEntriesSelectModeBottomActions(feedEntriesVM, tagsVM, tagsState)
        }
    }) { paddingValues ->
        Column(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
            FeedEntriesPageContent(feedEntriesVM, tagsVM, navController, scope, tabs, pagerState, scrollBehavior, scrollStateMap,
                topRefreshLayoutState, feedsMap, tagsState, tagsMapState, paddingValues.calculateBottomPadding().value)
        }
    }
}
