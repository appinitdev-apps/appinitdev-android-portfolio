
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppFilesPage(
    navController: NavHostController,
    appFilesVM: AppFilesViewModel = viewModel(),
) {
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val previewerState = rememberPreviewerState()
    val files by appFilesVM.itemsFlow.collectAsState()
    val isLoading = appFilesVM.showLoading.value
    val noMore = appFilesVM.noMore.value

    val refreshState = rememberRefreshLayoutState {
        scope.launch {
            withIO { appFilesVM.loadAsync() }
            setRefreshState(RefreshContentState.Finished)
        }
    }

    LaunchedEffect(Unit) {
        withIO { appFilesVM.loadAsync() }
    }

    PScaffold(
        topBar = {
            PTopAppBar(
                navController = navController,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    NavigationBackIcon { navController.navigateUp() }
                },
                title = stringResource(Res.string.app_files),
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
            ) {
                PullToRefresh(
                    modifier = Modifier.fillMaxSize(),
                    refreshLayoutState = refreshState,
                ) {
                    AppFileListContent(
                        navController = navController,
                        files = files,
                        isLoading = isLoading,
                        noMore = noMore,
                        previewerState = previewerState,
                        onRefresh = {
                            scope.launch {
                                withIO { appFilesVM.loadAsync() }
                            }
                        },
                        onLoadMore = {
                            scope.launch {
                                withIO { appFilesVM.moreAsync() }
                            }
                        },
                    )
                }
            }
        },
    )

    MediaPreviewer(state = previewerState)
}
