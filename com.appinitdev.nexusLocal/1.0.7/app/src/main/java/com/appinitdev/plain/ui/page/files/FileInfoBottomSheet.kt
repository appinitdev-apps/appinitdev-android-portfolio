
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileInfoBottomSheet(filesVM: FilesViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val file = filesVM.selectedFile.value ?: return
    var isFavorite by remember { mutableStateOf(false) }
    val onDismiss = { filesVM.selectedFile.value = null }

    LaunchedEffect(file.path) {
        if (file.isDir) {
            isFavorite = FavoriteFoldersPreference.isFavoriteAsync(file.path)
        }
    }

    if (filesVM.showRenameDialog.value) {
        FileRenameDialog(path = file.path, onDismiss = {
            filesVM.showRenameDialog.value = false
        }, onDoneAsync = {
            file.name = it.getFilenameFromPath()
            file.path = it
            filesVM.selectedFile.value = null
            filesVM.loadAsync(context)
        })
    }

    PModalBottomSheet(onDismissRequest = { onDismiss() }) {
        LazyColumn {
            item { VerticalSpace(32.dp) }
            item {
                FileInfoActionButtons(
                    file = file, filesVM = filesVM, isFavorite = isFavorite,
                    onFavoriteToggle = {
                        scope.launch(Dispatchers.IO) {
                            if (isFavorite) {
                                FavoriteFoldersPreference.removeAsync(file.path)
                                isFavorite = false
                            } else {
                                FavoriteFoldersPreference.addAsync(DFavoriteFolder(rootPath = filesVM.rootPath, fullPath = file.path))
                                isFavorite = true
                            }
                        }
                    },
                    showRenameDialog = filesVM.showRenameDialog,
                    context = context, scope = scope, onDismiss = onDismiss,
                    onShowPasteBar = { filesVM.showPasteBar.value = it },
                )
                VerticalSpace(dp = 24.dp)
                PCard {
                    PListItem(title = file.path, action = {
                        CopyIconButton(text = file.path, clipLabel = stringResource(Res.string.file_path))
                    })
                }
                VerticalSpace(dp = 16.dp)
                PCard {
                    if (!file.isDir) {
                        PListItem(title = stringResource(Res.string.file_size), value = file.size.formatBytes())
                    }
                    PListItem(title = stringResource(Res.string.type), value = if (file.isDir) stringResource(Res.string.folder) else file.path.getMimeType())
                    file.createdAt?.let {
                        PListItem(title = stringResource(Res.string.created_at), value = it.formatDateTime())
                    }
                    PListItem(title = stringResource(Res.string.updated_at), value = file.updatedAt.formatDateTime())
                    if (file.isDir && file.children > 0) {
                        PListItem(title = stringResource(Res.string.items), value = file.children.toString())
                    }
                }
            }
            item { BottomSpace() }
        }
    }
}