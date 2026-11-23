
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PScaffold(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.background,
    topBar: @Composable () -> Unit = {},
    bottomBar: (@Composable () -> Unit)? = null,
    floatingActionButton: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = containerColor,
        topBar = topBar,
        content = content,
        bottomBar = { bottomBar?.invoke() },
        floatingActionButton = { floatingActionButton?.invoke() },
    )
}
