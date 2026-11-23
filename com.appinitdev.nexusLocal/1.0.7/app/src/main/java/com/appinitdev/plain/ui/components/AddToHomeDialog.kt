
@Composable
fun AddToHomeDialog(
    path: String,
    iconRes: Int,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val defaultLabel = remember(path) { File(path).nameWithoutExtension }
    TextFieldDialog(
        title = stringResource(Res.string.add_to_home),
        value = defaultLabel,
        description = stringResource(Res.string.shortcut_label_hint),
        onDismissRequest = onDismiss,
        onConfirm = { label ->
            MediaShortcutHelper.addToDesktop(context, path, label, iconRes)
            onDismiss()
        },
    )
}
