
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FeedsPage(navController: NavHostController, feedsVM: FeedsViewModel = viewModel()) {
    val itemsState by feedsVM.itemsFlow.collectAsState()
    val scope = rememberCoroutineScope()
    val topRefreshLayoutState = rememberRefreshLayoutState {
        scope.launch { withIO { feedsVM.loadAsync(withCount = true) }; setRefreshState(RefreshContentState.Finished) }
    }
    LaunchedEffect(Unit) { scope.launch(Dispatchers.IO) { feedsVM.loadAsync(withCount = true) } }
    FeedsPageEffects(feedsVM)
    BackHandler(enabled = feedsVM.selectMode.value) { feedsVM.exitSelectMode() }
    AddFeedDialog(feedsVM); EditFeedDialog(feedsVM); ViewFeedBottomSheet(feedsVM)
    val pageTitle = if (feedsVM.selectMode.value) LocaleHelper.getStringF(Res.string.x_selected, "count", feedsVM.selectedIds.size)
    else LocaleHelper.getStringF(Res.string.subscriptions_title, "count", itemsState.size)

    PScaffold(
        topBar = {
            PTopAppBar(navController = navController, navigationIcon = {
                if (feedsVM.selectMode.value) NavigationCloseIcon { feedsVM.exitSelectMode() } else NavigationBackIcon { navController.navigateUp() }
            }, title = pageTitle, actions = {
                if (feedsVM.selectMode.value) {
                    PTopRightButton(label = stringResource(if (feedsVM.isAllSelected()) Res.string.unselect_all else Res.string.select_all), click = { feedsVM.toggleSelectAll() })
                    HorizontalSpace(dp = 8.dp)
                } else {
                    ActionButtonMoreWithMenu { dismiss ->
                        PDropdownMenuItem(
                            text = { Text(stringResource(Res.string.import_opml_file)) },
                            leadingIcon = { Icon(painter = painterResource(Res.drawable.upload), contentDescription = stringResource(Res.string.import_opml_file)) },
                            onClick = { dismiss(); sendEvent(PickFileEvent(PickFileTag.FEED, PickFileType.FILE, false)) })
                        PDropdownMenuItem(
                            text = { Text(stringResource(Res.string.export_opml_file)) },
                            leadingIcon = { Icon(painter = painterResource(Res.drawable.download), contentDescription = stringResource(Res.string.export_opml_file)) },
                            onClick = { dismiss(); sendEvent(ExportFileEvent(ExportFileType.OPML, "feeds_" + Date().formatName() + ".opml")) })
                    }
                }
            })
        },
        bottomBar = {
            AnimatedVisibility(visible = feedsVM.showBottomActions(), enter = slideInVertically { it }, exit = slideOutVertically { it }) { FeedsSelectModeBottomActions(feedsVM) }
        },
        floatingActionButton = if (feedsVM.selectMode.value) null else {
            {
                PDraggableElement {
                    FloatingActionButton(onClick = { feedsVM.showAddDialog() }) {
                        Icon(painter = painterResource(Res.drawable.plus), stringResource(Res.string.add))
                    }
                }
            }
        },
    ) { paddingValues ->
        PullToRefresh(modifier = Modifier.padding(top = paddingValues.calculateTopPadding()), refreshLayoutState = topRefreshLayoutState) {
            AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                if (itemsState.isNotEmpty()) {
                    LazyColumn(Modifier.fillMaxSize()) {
                        item { TopSpace() }
                        items(itemsState) { m ->
                            FeedListItem(feedsVM = feedsVM, m, onClick = {
                                if (feedsVM.selectMode.value) feedsVM.select(m.id) else navController.navigate(Routing.FeedEntries(m.id))
                            }, onLongClick = { if (!feedsVM.selectMode.value) feedsVM.selectedItem.value = m })
                            VerticalSpace(dp = 8.dp)
                        }
                        item { VerticalSpace(dp = paddingValues.calculateBottomPadding()) }
                    }
                } else {
                    NoDataColumn(loading = feedsVM.showLoading.value)
                }
            }
        }
    }
}
