
@Composable
fun CreateChannelDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    TextFieldDialog(
        title = stringResource(Res.string.new_channel),
        placeholder = stringResource(Res.string.channel_name_hint),
        onDismissRequest = onDismiss,
        onConfirm = { name ->
            onConfirm(name.trim())
        },
        validator = { it.trim().isNotBlank() },
    )
}

@Composable
fun RenameChannelDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    TextFieldDialog(
        title = stringResource(Res.string.rename_channel),
        value = currentName,
        placeholder = stringResource(Res.string.channel_name_hint),
        onDismissRequest = onDismiss,
        onConfirm = { name ->
            onConfirm(name.trim())
        },
        validator = { it.trim().isNotBlank() },
    )
}
