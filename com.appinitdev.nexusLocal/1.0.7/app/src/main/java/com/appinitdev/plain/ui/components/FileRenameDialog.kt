@Composable
fun FileRenameDialog(path: String, onDismiss: () -> Unit, onDoneAsync: suspend (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val oldName = remember {
        mutableStateOf(path.substringAfterLast("/"))
    }
    val name = remember {
        mutableStateOf(oldName.value)
    }
    TextFieldDialog(
        title = stringResource(Res.string.rename),
        value = name.value,
        placeholder = oldName.value,
        onValueChange = {
            name.value = it
        },
        onDismissRequest = {
            onDismiss()
        },
        confirmText = stringResource(Res.string.save),
        onConfirm = {
            scope.launch {
                withIO {
                    val newFile = FileHelper.rename(path, name.value)
                    MainApp.instance.scanFileByConnection(path)
                    if (newFile != null) {
                        MainApp.instance.scanFileByConnection(newFile.absolutePath)
                        onDoneAsync(newFile.absolutePath)
                    }
                }
                onDismiss()
            }
        },
    )
}