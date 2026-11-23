@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppsPage(navController: NavHostController, appsVM: AppsViewModel = viewModel()) {
    val context = LocalContext.current
    val itemsState by appsVM.itemsFlow.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollStateMap = remember { mutableStateMapOf<Int, LazyListState>() }
    val pagerState = rememberPagerState(pageCount = { appsVM.tabs.value.size })
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(canScroll = { (scrollStateMap[pagerState.currentPage]?.firstVisibleItemIndex ?: 0) > 0 })
    var isFirstTime by remember { mutableStateOf(true) }
    val topRefreshLayoutState = rememberRefreshLayoutState {
        scope.launch { withIO { appsVM.loadAsync() }; setRefreshState(RefreshContentState.Finished) }
    }
    val once = rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { if (!once.value) { once.value = true; scope.launch(Dispatchers.IO) { appsVM.loadAsync() } } }
    LaunchedEffect(pagerState.currentPage) {
        if (isFirstTime) { isFirstTime = false; return@LaunchedEffect }
        val tab = appsVM.tabs.value.getOrNull(pagerState.currentPage)
        if (tab != null) {
            appsVM.appType.value = tab.value
            scope.launch { scrollBehavior.reset(); scrollStateMap[pagerState.currentPage]?.scrollToItem(0) }
            scope.launch(Dispatchers.IO) { appsVM.loadAsync() }
        }
    }
    if (appsVM.showSortDialog.value) {
        RadioDialog(title = stringResource(Res.string.sort), options = FileSortBy.entries.map {
            RadioDialogOption(text = stringResource(it.getTextId()), selected = it == appsVM.sortBy.value) {
                scope.launch(Dispatchers.IO) { PackageSortByPreference.putAsync(it); appsVM.sortBy.value = it; appsVM.loadAsync() }
            }
        }) { appsVM.showSortDialog.value = false }
    }
    val onSearch: (String) -> Unit = { appsVM.searchActive.value = false; appsVM.showLoading.value = true; scope.launch(Dispatchers.IO) { appsVM.loadAsync() } }
    BackHandler(enabled = appsVM.showSearchBar.value) {
        if (appsVM.showSearchBar.value && (!appsVM.searchActive.value || appsVM.queryText.value.isEmpty())) { appsVM.exitSearchMode(); onSearch("") }
    }
    PScaffold(topBar = {
        if (appsVM.showSearchBar.value) { ListSearchBar(viewModel = appsVM, onSearch = onSearch); return@PScaffold }
        PTopAppBar(modifier = Modifier.combinedClickable(onClick = {}, onDoubleClick = { scope.launch { scrollStateMap[pagerState.currentPage]?.scrollToItem(0) } }),
            navController = navController,
            title = stringResource(Res.string.apps), scrollBehavior = scrollBehavior, actions = {
                ActionButtonSearch { appsVM.enterSearchMode() }; ActionButtonSort { appsVM.showSortDialog.value = true }
            })
    }) { paddingValues ->
        Column(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
            if (!appsVM.showLoading.value) {
                PScrollableTabRow(selectedTabIndex = pagerState.currentPage, modifier = Modifier.fillMaxWidth()) {
                    appsVM.tabs.value.forEachIndexed { index, s ->
                        PFilterChip(modifier = Modifier.padding(start = if (index == 0) 0.dp else 8.dp), selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.scrollToPage(index) } }, label = { Text(text = s.title + " (" + s.count + ")") })
                    }
                }
            }
            if (pagerState.pageCount == 0) { NoDataColumn(loading = appsVM.showLoading.value, search = appsVM.showSearchBar.value); return@PScaffold }
            HorizontalPager(state = pagerState, userScrollEnabled = false) { index ->
                AppsPageList(navController = navController, appsVM = appsVM, items = itemsState, index = index,
                    scrollStateMap = scrollStateMap, scrollBehavior = scrollBehavior, topRefreshLayoutState = topRefreshLayoutState, paddingValues = paddingValues)
            }
        }
    }
}
