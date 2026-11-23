@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherFilePage(
    navController: NavHostController,
    path: String,
    title: String
) {
    val context = LocalContext.current
    val file = File(path)
    var displayTitle by remember(path, title) { mutableStateOf(title.ifEmpty { file.name }) }
    LaunchedEffect(path, title) {
        displayTitle = withIO { AppFileDisplayNameHelper.resolveDisplayNameByPath(path, title) }
    }
    PScaffold(
        topBar = {
            PTopAppBar(
                navController = navController,
                title = displayTitle,
                actions = {
                    PIconButton(
                        icon = Res.drawable.share_2,
                        contentDescription = stringResource(Res.string.share),
                        tint = MaterialTheme.colorScheme.onSurface,
                    ) {
                        ShareHelper.shareFile(context, File(path))
                    }
                },
            )
        },
        content = { paddingValues ->
            LazyColumn(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            modifier =
                                Modifier
                                    .padding(bottom = 32.dp)
                                    .size(56.dp),
                            painter = painterResource(Res.drawable.file_question),
                            contentDescription = "",
                        )
                        SelectionContainer {
                            Text(
                                text = displayTitle,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(horizontal = 32.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        VerticalSpace(dp = 16.dp)
                        SelectionContainer {
                            Text(
                                text = stringResource(Res.string.file_size) + ": " + file.length().formatBytes(),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(horizontal = 32.dp),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        VerticalSpace(dp = 16.dp)
                        Text(
                            text = stringResource(Res.string.unknown_file_description),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = 32.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        VerticalSpace(dp = 64.dp)
                        PFilledButton(text = stringResource(Res.string.open_with_other_app), modifier = Modifier.padding(horizontal = 16.dp), onClick = {
                            ShareHelper.openPathWith(context, path)
                        })
                    }
                }
            }
        },
    )
}
