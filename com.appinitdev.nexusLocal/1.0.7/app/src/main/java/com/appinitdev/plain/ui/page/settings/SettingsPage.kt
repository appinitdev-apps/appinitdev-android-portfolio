
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SettingsPage(navController: NavHostController, updateViewModel: UpdateViewModel) {
    val autoCheckUpdate = LocalAutoCheckUpdate.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var developerMode by remember { mutableStateOf(false) }
    var cacheSize by remember { mutableLongStateOf(0L) }
    var fileSize by remember { mutableLongStateOf(AppLogHelper.getFileSize(context)) }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            cacheSize = AppHelper.getCacheSize(context)
            developerMode = DeveloperModePreference.getAsync()
        }
    }

    LaunchedEffect(Unit) {
        Channel.sharedFlow.collect { event ->
            if (event is DownloadUpdateEvent) {
                listState.animateScrollToItem(0)
            }
            updateViewModel.consumeUpdateDownloadEvent(event)
        }
    }

    UpdateDialog(updateViewModel)

    PScaffold(
        topBar = { PTopAppBar(navController = navController, title = stringResource(Res.string.settings)) },
        content = { paddingValues ->
            LazyColumn(
                state = listState,
                modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
            ) {
                item { TopSpace() }

















                item { SettingsCardItems(navController) }

                item {
                    VerticalSpace(dp = 16.dp)
                    PCard {
                        PListItem(
                            modifier = Modifier.combinedClickable(onClick = {}, onDoubleClick = {
                                developerMode = true
                                scope.launch(Dispatchers.IO) { DeveloperModePreference.putAsync(true) }
                            }),
                            title = stringResource(Res.string.android_version),
                            value = MainApp.getAndroidVersion(),
                        )






















                    }
                }
                item {
                    VerticalSpace(dp = 16.dp)
                    AboutLogsAndCacheCard(
                        navController = navController, context = context, scope = scope,
                        fileSize = fileSize, onFileSizeCleared = { fileSize = 0 },
                        cacheSize = cacheSize, onCacheCleared = { cacheSize = it },
                        developerMode = developerMode, onDeveloperModeChanged = { developerMode = it },
                    )
                }



















                item { BottomSpace(paddingValues) }
            }
        },
    )
}
