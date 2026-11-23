@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : IData> ListSearchBar(
    viewModel: ISearchableViewModel<T>,
    onSearch: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val windowInfo = LocalWindowInfo.current
    LaunchedEffect(windowInfo) {
        snapshotFlow { windowInfo.isWindowFocused }.collect { isWindowFocused ->
            if (isWindowFocused && viewModel.searchActive.value) {
                focusRequester.requestFocus()
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SearchBar(
            modifier = Modifier
                .focusRequester(focusRequester),
            query = viewModel.queryText.value,
            onQueryChange = {
                viewModel.queryText.value = it
            },
            onSearch = onSearch,
            active = viewModel.searchActive.value,
            onActiveChange = {
                if (viewModel.searchActive.value != it) {
                    viewModel.searchActive.value = it
                    if (!viewModel.searchActive.value && viewModel.queryText.value.isEmpty()) {
                        viewModel.exitSearchMode()
                        onSearch("")
                    }
                }
            },
            placeholder = { Text(stringResource(Res.string.search)) },
            leadingIcon = {
                PIconButton(
                    icon = Res.drawable.arrow_left,
                    contentDescription = stringResource(Res.string.back),
                    tint = MaterialTheme.colorScheme.onSurface,
                ) {
                    if (!viewModel.searchActive.value || viewModel.queryText.value.isEmpty()) {
                        viewModel.exitSearchMode()
                        onSearch("")
                    } else {
                        viewModel.searchActive.value = false
                    }
                }
            },
            colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.cardBackgroundNormal),
        ) {
        }
    }
}