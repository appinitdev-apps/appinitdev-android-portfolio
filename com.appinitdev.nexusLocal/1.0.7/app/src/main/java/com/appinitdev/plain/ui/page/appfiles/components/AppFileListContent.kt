@Composable
fun AppFileListContent(
    navController: NavHostController,
    files: List<VAppFile>,
    isLoading: Boolean,
    noMore: Boolean,
    previewerState: MediaPreviewerState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
) {
    val context = LocalContext.current
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    if (files.isEmpty()) {
        NoDataView(
            icon = Res.drawable.package_open,
            message = stringResource(Res.string.no_app_files),
            showRefreshButton = true,
            onRefresh = onRefresh,
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        items(files, key = { it.appFile.id }) { file ->
            val itemState = rememberTransformItemState()
            AppFileListItem(
                file = file,
                itemState = itemState,
                previewerState = previewerState,
                onClick = {
                    openAppFile(context, files, file, navController, previewerState, itemState)
                },
            )
        }
        item {
            if (files.isNotEmpty() && !noMore) {
                LaunchedEffect(Unit) { onLoadMore() }
            }
            LoadMoreRefreshContent(noMore)
            BottomSpace()
        }
    }
}
