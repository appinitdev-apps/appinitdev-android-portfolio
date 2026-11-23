
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PModalBottomSheet(
    modifier: Modifier = Modifier.defaultMinSize(minHeight = 320.dp),
    onDismissRequest: () -> Unit = {},
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
    content: @Composable ColumnScope.() -> Unit = {},
) {
    ModalBottomSheet(
        modifier = modifier.statusBarsPadding(),
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        onDismissRequest = onDismissRequest,
        dragHandle = null,
        content = content
    )
}
