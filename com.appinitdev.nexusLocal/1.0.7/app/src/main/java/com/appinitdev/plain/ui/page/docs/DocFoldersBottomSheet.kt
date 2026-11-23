@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DocFoldersBottomSheet(
    docsVM: DocsViewModel,
    mediaFoldersVM: MediaFoldersViewModel,
    tagsVM: TagsViewModel,
) {
    if (!docsVM.showFoldersDialog.value) {
        return
    }
    val itemsState by mediaFoldersVM.itemsFlow.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            mediaFoldersVM.loadAsync(context)
        }
    }
    fun onSelect(id: String) {
        docsVM.showFoldersDialog.value = false
        docsVM.bucketId.value = id
        scope.launch(Dispatchers.IO) {
            docsVM.loadAsync(context, tagsVM)
        }
    }
    PModalBottomSheet(
        onDismissRequest = { docsVM.showFoldersDialog.value = false },
        sheetState = sheetState,
    ) {
        Column {
            PBottomSheetTopAppBar(
                title = stringResource(Res.string.folders),
                actions = {
                    ActionButtonRefresh(
                        loading = mediaFoldersVM.showLoading.value,
                        onClick = {
                            mediaFoldersVM.showLoading.value = true
                            scope.launch { withIO { mediaFoldersVM.loadAsync(context) } }
                        }
                    )
                }
            )
            AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                if (itemsState.isNotEmpty()) {
                    MediaFolderListContent(
                        listState = listState,
                        totalBucket = mediaFoldersVM.totalBucket.value,
                        items = itemsState,
                        selectedBucketId = docsVM.bucketId.value,
                        onSelect = ::onSelect,
                    )
                } else {
                    NoDataColumn(loading = mediaFoldersVM.showLoading.value)
                }
            }
        }
    }
}
