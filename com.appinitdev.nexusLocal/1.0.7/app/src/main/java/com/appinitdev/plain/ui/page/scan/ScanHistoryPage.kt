@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ScanHistoryPage(
    navController: NavHostController,
    scanHistoryVM: ScanHistoryViewModel = viewModel(),
) {
    val context = LocalContext.current
    val itemsState by scanHistoryVM.itemsFlow.collectAsState()
    val refreshState =
        rememberRefreshLayoutState {
            scanHistoryVM.fetch(context)
            setRefreshState(RefreshContentState.Finished)
        }
    LaunchedEffect(Unit) {
        scanHistoryVM.fetch(context)
    }
    PScaffold(
        topBar = {
            PTopAppBar(navController = navController, title = stringResource(Res.string.scan_history))
        },
        content = { paddingValues ->
            PullToRefresh(modifier = Modifier.padding(top = paddingValues.calculateTopPadding()), refreshLayoutState = refreshState) {
                if (itemsState.isNotEmpty()) {
                    LazyColumn(
                        Modifier
                            .fillMaxSize()
                    ) {
                        item {
                            TopSpace()
                        }
                        items(itemsState) { m ->
                            ScanHistoryItem(
                                context = context,
                                text = m,
                                onDelete = {
                                    DialogHelper.confirmToDelete {
                                        scanHistoryVM.delete(context, m)
                                    }
                                }
                            )
                            VerticalSpace(dp = 8.dp)
                        }
                        item {
                            BottomSpace(paddingValues)
                        }
                    }
                } else {
                    NoDataColumn()
                }
            }
        },
    )
}
