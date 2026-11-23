@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilesSelectModeBottomActions(
    filesVM: FilesViewModel,
    onShowPasteBar: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val selectedFiles = filesVM.itemsFlow.value.filter { file -> filesVM.selectedIds.contains(file.path) }
    val showRenameDialog = remember { mutableStateOf(false) }
    if (showRenameDialog.value && filesVM.selectedIds.size == 1) {
        FileRenameDialog(
            file = selectedFiles[0],
            filesVM = filesVM,
            showDialog = showRenameDialog,
        )
    }
    PBottomAppBar {
        BottomActionButtons {
            IconTextSmallButtonCut {
                performCutFiles(filesVM, selectedFiles, onShowPasteBar) { filesVM.exitSelectMode() }
            }
            IconTextSmallButtonCopy {
                performCopyFiles(filesVM, selectedFiles, onShowPasteBar) { filesVM.exitSelectMode() }
            }
            IconTextSmallButtonShare {
                ShareHelper.sharePaths(context, filesVM.selectedIds.toSet())
            }
            IconTextSmallButtonDelete {
                DialogHelper.confirmToDelete {
                    scope.launch {
                        val paths = filesVM.selectedIds.toSet()
                        DialogHelper.showLoading()
                        withIO {
                            FilePathValidator.requireAllSafe(paths.toList())
                            paths.forEach { File(it).deleteRecursively() }
                            MainApp.instance.scanFileByConnection(paths.toTypedArray())
                            filesVM.loadAsync(context)
                        }
                        DialogHelper.hideLoading()
                        filesVM.exitSelectMode()
                    }
                }
            }
            IconTextSmallButtonZip {
                performZipFiles(scope, context, filesVM, selectedFiles) { filesVM.exitSelectMode() }
            }
            if (selectedFiles.size == 1 && selectedFiles[0].path.endsWith(".zip")) {
                IconTextSmallButtonUnzip {
                    scope.launch {
                        DialogHelper.showLoading()
                        val file = selectedFiles[0]
                        val destFile = File(file.path.removeSuffix(".zip"))
                        var destPath = destFile.path
                        if (destFile.exists()) {
                            destPath = destFile.newPath()
                        }
                        val success = withIO {
                            ZipHelper.unzip(File(file.path), File(destPath))
                        }
                        if (success) {
                            withIO {
                                MainApp.instance.scanFileByConnection(destPath)
                                filesVM.loadAsync(context)
                            }
                        } else {
                            DialogHelper.showMessage(Res.string.error)
                        }
                        DialogHelper.hideLoading()
                        filesVM.exitSelectMode()
                    }
                }
            }
            if (filesVM.selectedIds.size == 1) {
                IconTextSmallButtonRename {
                    showRenameDialog.value = true
                }
            }
        }
    }
} 