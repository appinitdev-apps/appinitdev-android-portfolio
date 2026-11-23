
@Composable
fun FileSortDialog(sortBy: MutableState<FileSortBy>, entries: List<FileSortBy> = FileSortBy.entries, onSelected: (FileSortBy) -> Unit, onDismiss: () -> Unit = {}) {
    RadioDialog(
        title = stringResource(Res.string.sort),
        options =
        entries.map {
            RadioDialogOption(
                text = stringResource(it.getTextId()),
                selected = it == sortBy.value,
            ) {
                onSelected(it)
            }
        },
        onDismissRequest = onDismiss
    )
}