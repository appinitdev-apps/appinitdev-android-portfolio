@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun <T : IData> MediaFoldersBottomSheet(
    mediaVM: BaseMediaViewModel<T>,
    mediaFoldersVM: MediaFoldersViewModel,
    tagsVM: TagsViewModel
) {
    if (!mediaVM.showFoldersDialog.value) {
        return
    }
    val itemsState by mediaFoldersVM.itemsFlow.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val gridState = rememberLazyGridState()
    val listState = rememberLazyListState()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val showAsList = mediaVM is AudioViewModel
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            mediaFoldersVM.loadAsync(context)
        }
    }
    fun onSelect(id: String) {
        mediaVM.showFoldersDialog.value = false
        mediaVM.bucketId.value = id
        scope.launch(Dispatchers.IO) {
            mediaVM.loadAsync(context, tagsVM)
        }
    }
    PModalBottomSheet(
        onDismissRequest = {
            mediaVM.showFoldersDialog.value = false
        },
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
                            scope.launch {
                                withIO { mediaFoldersVM.loadAsync(context) }
                            }
                        }
                    )
                }
            )
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (itemsState.isNotEmpty()) {
                    if (showAsList) {
                        MediaFolderListContent(
                            listState = listState,
                            totalBucket = mediaFoldersVM.totalBucket.value,
                            items = itemsState,
                            selectedBucketId = mediaVM.bucketId.value,
                            onSelect = ::onSelect,
                        )
                    } else {
                        MediaFolderGridContent(
                            gridState = gridState,
                            totalBucket = mediaFoldersVM.totalBucket.value,
                            items = itemsState,
                            selectedBucketId = mediaVM.bucketId.value,
                            onSelect = ::onSelect,
                        )
                    }
                } else {
                    NoDataColumn(loading = mediaFoldersVM.showLoading.value)
                }
            }
        }
    }
}
