
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ConnectionsPage(
    navController: NavHostController,
    sessionsVM: SessionsViewModel = viewModel(),
) {
    val itemsState by sessionsVM.itemsFlow.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val sessions = remember(itemsState) { itemsState.filter { !it.isCustom } }
    val apiTokens = remember(itemsState) { itemsState.filter { it.isCustom } }
    val tabTitles = listOf(stringResource(Res.string.sessions), stringResource(Res.string.api_tokens))

    val refreshState = rememberRefreshLayoutState {
        sessionsVM.fetch()
        setRefreshState(RefreshContentState.Finished)
    }

    LaunchedEffect(Unit) {
        sessionsVM.fetch()
    }

    if (showCreateDialog) {
        CreateApiTokenDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                showCreateDialog = false
                sessionsVM.createCustomToken(name)
            },
        )
    }

    PScaffold(
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = { NavigationBackIcon { navController.navigateUp() } },
                    title = {
                        PScrollableTabRow(selectedTabIndex = pagerState.currentPage) {
                            tabTitles.forEachIndexed { index, title ->
                                PFilterChip(
                                    modifier = Modifier.padding(start = if (index == 0) 0.dp else 8.dp),
                                    selected = pagerState.currentPage == index,
                                    onClick = { scope.launch { pagerState.scrollToPage(index) } },
                                    label = { Text(text = title) },
                                )
                            }
                        }
                    },
                    actions = {
                        if (pagerState.currentPage == 1) {
                            ActionButtonAdd {
                                showCreateDialog = true
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                )
            }
        },
        content = { paddingValues ->
            HorizontalPager(state = pagerState, userScrollEnabled = true) { page ->
                PullToRefresh(refreshLayoutState = refreshState) {
                    val items = if (page == 0) sessions else apiTokens
                    if (items.isNotEmpty()) {
                        LazyColumn(
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .padding(top = paddingValues.calculateTopPadding())
                        ) {
                            item { TopSpace() }
                            items(items) { m ->
                                SessionListItem(
                                    m = m,
                                    onDelete = { sessionsVM.delete(it) },
                                    onRename = { clientId, name -> sessionsVM.rename(clientId, name) },
                                )
                            }
                            item { BottomSpace(paddingValues) }
                        }
                    } else {
                        NoDataColumn()
                    }
                }
            }
        },
    )
}

