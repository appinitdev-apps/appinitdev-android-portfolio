
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPage(
    navController: NavHostController,
    uri: Uri,
    fileName: String = "",
) {
    val context = LocalContext.current
    val title = fileName.ifEmpty { uri.getFileName(context) }

    PScaffold(
        topBar = {
            PTopAppBar(
                navController = navController,
                title = title,
                actions = {
                    PIconButton(
                        icon = Res.drawable.share_2,
                        contentDescription = stringResource(Res.string.share),
                        tint = MaterialTheme.colorScheme.onSurface,
                    ) {
                        if (uri.scheme == "content") {
                            ShareHelper.shareUri(context, uri)
                        } else {
                            ShareHelper.shareFile(context, uri.toFile(), displayName = fileName)
                        }
                    }
                },
            )
        },
        content = { paddingValues ->
            PdfView(
                uri = uri, modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
            )
        },
    )
}
