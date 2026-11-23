@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MdEditorSettingsDialog(
    mdEditorVM: MdEditorViewModel,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = {
            mdEditorVM.showSettings = false
        },
        confirmButton = {
            Button(
                onClick = {
                    mdEditorVM.showSettings = false
                }
            ) {
                Text(stringResource(Res.string.close))
            }
        },
        title = {
            Text(text = stringResource(Res.string.editor_settings),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                PDialogListItem(
                    title = stringResource(Res.string.show_line_numbers),
                ) {
                    PSwitch(
                        activated = mdEditorVM.showLineNumbers,
                    ) {
                        mdEditorVM.showLineNumbers = it
                        scope.launch(Dispatchers.IO) {
                            EditorShowLineNumbersPreference.putAsync(it)
                        }
                    }
                }
                PDialogListItem(
                    title = stringResource(Res.string.wrap_content),
                ) {
                    PSwitch(
                        activated = mdEditorVM.wrapContent,
                    ) {
                        mdEditorVM.wrapContent = it
                        scope.launch(Dispatchers.IO) {
                            EditorWrapContentPreference.putAsync(it)
                        }
                    }
                }
            }
        })
}