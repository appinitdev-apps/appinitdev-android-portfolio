
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsPageList(
    navController: NavHostController,
    appsVM: AppsViewModel,
    items: List<VPackage>,
    index: Int,
    scrollStateMap: MutableMap<Int, LazyListState>,
    scrollBehavior: TopAppBarScrollBehavior,
    topRefreshLayoutState: RefreshLayoutState,
    paddingValues: PaddingValues,
) {
    val scope = rememberCoroutineScope()
    PullToRefresh(refreshLayoutState = topRefreshLayoutState) {
        AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
            if (items.isNotEmpty()) {
                val scrollState = rememberLazyListState()
                scrollStateMap[index] = scrollState
                LazyColumnScrollbar(state = scrollState) {
                    LazyColumn(
                        Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
                        state = scrollState,
                    ) {
                        item { TopSpace() }
                        items(items, key = { it.id }) { m ->
                            PackageListItem(item = m, modifier = PlainTheme.getCardModifier(), onClick = { navController.navigate(Routing.AppDetails(m.id)) })
                            VerticalSpace(dp = 8.dp)
                        }
                        item {
                            if (items.isNotEmpty() && !appsVM.noMore.value) {
                                LaunchedEffect(Unit) { scope.launch(Dispatchers.IO) { withIO { appsVM.moreAsync() } } }
                            }
                            LoadMoreRefreshContent(appsVM.noMore.value)
                            BottomSpace(paddingValues)
                        }
                    }
                }
            } else {
                NoDataColumn(loading = appsVM.showLoading.value, search = appsVM.showSearchBar.value)
            }
        }
    }
}
