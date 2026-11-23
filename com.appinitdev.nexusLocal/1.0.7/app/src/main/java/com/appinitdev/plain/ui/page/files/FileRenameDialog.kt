
@Composable
internal fun FileRenameDialog(
    file: DFile,
    filesVM: FilesViewModel,
    showDialog: MutableState<Boolean>,
) {
    val scope = rememberCoroutineScope()
    val name = remember { mutableStateOf(file.name) }

    TextFieldDialog(
        title = stringResource(Res.string.rename),
        value = name.value,
        placeholder = file.name,
        onValueChange = { name.value = it },
        onDismissRequest = { showDialog.value = false },
        confirmText = stringResource(Res.string.save),
        onConfirm = { newName ->
            scope.launch {
                DialogHelper.showLoading()
                val oldName = file.name
                val oldPath = file.path
                val dstFile = withIO { FileHelper.rename(file.path, newName) }
                if (dstFile != null) {
                    withIO {
                        MainApp.instance.scanFileByConnection(file.path)
                        MainApp.instance.scanFileByConnection(dstFile)
                    }
                }

                file.name = newName
                file.path = file.path.replace("/$oldName", "/$newName")
                if (file.isDir) {
                    filesVM.breadcrumbs.find { b -> b.path == oldPath }?.let { b ->
                        b.path = file.path
                        b.name = newName
                    }
                }

                DialogHelper.hideLoading()
                filesVM.exitSelectMode()
                showDialog.value = false
            }
        }
    )
}
