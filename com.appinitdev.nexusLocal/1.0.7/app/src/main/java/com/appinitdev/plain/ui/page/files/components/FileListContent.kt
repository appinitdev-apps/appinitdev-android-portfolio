
@Composable
fun FileListContent(
    navController: NavHostController,
    filesVM: FilesViewModel,
    files: List<DFile>,
    loadFiles: (List<DFile>, Boolean) -> Unit,
    previewerState: MediaPreviewerState,
    audioPlaylistVM: AudioPlaylistViewModel
) {
    val context = LocalContext.current

    if (filesVM.isLoading.value) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (files.isEmpty()) {
        NoDataView(
            icon = Res.drawable.package_open,
            message = stringResource(Res.string.no_data),
            showRefreshButton = true,
            onRefresh = { loadFiles(emptyList(), true) }
        )
    } else {
        val lazyListState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(files) { file ->
                val itemState = rememberTransformItemState()
                FileListItem(
                    file = file,
                    isSelected = filesVM.selectedIds.contains(file.path),
                    isSelectMode = filesVM.selectMode.value,
                    itemState = itemState,
                    previewerState = previewerState,
                    onClick = {
                        if (filesVM.selectMode.value) {
                            filesVM.select(file.path)
                        } else {
                            if (file.isDir) {
                                filesVM.navigateToDirectory(context, file.path)
                            } else if (!ZipBrowserHelper.isZipPath(file.path) && file.extension.lowercase() == "zip") {
                                
                                filesVM.navigateToDirectory(context, ZipBrowserHelper.joinPath(file.path, ""))
                            } else {
                                openFile(context, files, file, navController, previewerState, itemState, audioPlaylistVM)
                            }
                        }
                    },
                    onLongClick = {
                        
                        if (ZipBrowserHelper.isZipPath(file.path)) return@FileListItem
                        if (!filesVM.selectMode.value) {
                            filesVM.selectedFile.value = file
                        } else {
                            filesVM.select(file.path)
                        }
                    },
                    audioPlaylistVM = audioPlaylistVM
                )
            }
            item {
                BottomSpace()
            }
        }
    }
}