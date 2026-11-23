@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class, FlowPreview::class)
@Composable
fun MdEditor(
    modifier: Modifier,
    mdEditorVM: MdEditorViewModel,
    scrollState: ScrollState,
    focusRequester: FocusRequester,
    shouldRequestFocus: Boolean = false,
    onFocusRequested: () -> Unit = {},
) {
    val lineNumberState = rememberScrollState()
    var lineCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        snapshotFlow { scrollState.value }
            .debounce(10)
            .collectLatest { value ->
                lineNumberState.scrollTo(value)
            }
    }
    LaunchedEffect(shouldRequestFocus) {
        if (shouldRequestFocus) {
            focusRequester.requestFocus()
            onFocusRequested()
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        val lineNumberWidth = if (mdEditorVM.showLineNumbers) measureTextWidth(" ${lineCount + 1} ", MaterialTheme.typography.bodyLarge) else 0.dp
        if (mdEditorVM.showLineNumbers) {
            Column(
                modifier = Modifier
                    .width(lineNumberWidth)
                    .fillMaxHeight()
                    .verticalScroll(lineNumberState, enabled = false),
            ) {
                Text(
                    text = mdEditorVM.linesText,
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier
                        .fillMaxHeight(),
                    textAlign = TextAlign.End,
                )
            }
        }
        Box(
            modifier = if (mdEditorVM.wrapContent) {
                Modifier
                    .padding(start = if (mdEditorVM.showLineNumbers) lineNumberWidth + 8.dp else PlainTheme.PAGE_HORIZONTAL_MARGIN, end = PlainTheme.PAGE_HORIZONTAL_MARGIN)
                    .fillMaxSize()
            } else {
                Modifier
                    .padding(if (mdEditorVM.showLineNumbers) lineNumberWidth + 8.dp else PlainTheme.PAGE_HORIZONTAL_MARGIN, end = PlainTheme.PAGE_HORIZONTAL_MARGIN)
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState())
            }
        ) {
            BasicTextField(
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester),
                state = mdEditorVM.textFieldState,
                scrollState = scrollState,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                onTextLayout = { result ->
                    val r = result()
                    if (r != null) {
                        if (lineCount != r.lineCount) {
                            lineCount = r.lineCount
                            mdEditorVM.linesText = MdEditorLineHelper.getLinesText(
                                lineCount,
                                r,
                                mdEditorVM.textFieldState.text.toString(),
                            )
                        }
                    }
                })
        }
    }
}
