@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioDialog(
    modifier: Modifier = Modifier,
    title: String = "",
    options: List<RadioDialogOption> = emptyList(),
    onDismissRequest: () -> Unit = {},
) {
    AlertDialog(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            LazyColumn {
                items(options) { option ->
                    PDialogRadioRow(selected = option.selected, onClick = {
                        option.onClick()
                        onDismissRequest()
                    }, text = option.text)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismissRequest) {
                Text(text = stringResource(Res.string.close))
            }
        },
        dismissButton = {},
    )
}
data class RadioDialogOption(
    val text: String = "",
    val selected: Boolean = false,
    val onClick: () -> Unit = {},
)
