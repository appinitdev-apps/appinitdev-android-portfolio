
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun <T : IData> SearchableTopBar(
    navController: NavHostController,
    viewModel: ISearchableViewModel<T>,
    scrollBehavior: TopAppBarScrollBehavior,
    title: String,
    subtitle: String = "",
    containerColor: Color? = null,
    subtitleColor: Color? = null,
    scrollToTop: (() -> Unit)? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    onSearchAction: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    if (viewModel.showSearchBar.value) {
        ListSearchBar(
            viewModel = viewModel,
            onSearch = {
                viewModel.searchActive.value = false
                scope.launch(Dispatchers.IO) {
                    onSearchAction(it)
                }
            }
        )
        return
    }
    
    val topBarModifier = if (scrollToTop != null) {
        Modifier.combinedClickable(onClick = {}, onDoubleClick = {
            scope.launch {
                scrollToTop()
            }
        })
    } else {
        Modifier
    }
    
    PTopAppBar(
        modifier = topBarModifier,
        navController = navController,
        title = title,
        subtitle = subtitle,
        containerColor = containerColor,
        subtitleColor = subtitleColor,
        scrollBehavior = scrollBehavior,
        navigationIcon = navigationIcon,
        actions = actions
    )
} 