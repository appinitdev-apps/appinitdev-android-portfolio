@Composable
fun TagNameDialog(tagsVM: TagsViewModel, onChangedAsync: suspend () -> Unit = {}) {
    val tag = tagsVM.editItem.value
    val scope = rememberCoroutineScope()
    if (tagsVM.tagNameDialogVisible.value) {
        TextFieldDialog(
            title = stringResource(if (tag != null) Res.string.edit_tag else Res.string.add_tag),
            value = tagsVM.editTagName.value,
            placeholder = tag?.name ?: stringResource(Res.string.name),
            onValueChange = {
                tagsVM.editTagName.value = it
            },
            onDismissRequest = {
                tagsVM.tagNameDialogVisible.value = false
            },
            confirmText = stringResource(Res.string.save),
            onConfirm = {
                scope.launch(Dispatchers.IO) {
                    if (tag != null) {
                        tagsVM.editTagAsync(tagsVM.editTagName.value)
                    } else {
                        tagsVM.addTagAsync(tagsVM.editTagName.value)
                    }
                    onChangedAsync()
                }
            },
        )
    }
}