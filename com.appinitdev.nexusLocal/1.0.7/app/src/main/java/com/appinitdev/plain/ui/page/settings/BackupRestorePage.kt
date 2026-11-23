@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestorePage(
    navController: NavHostController,
    backupRestoreVM: BackupRestoreViewModel = viewModel(),
) {
    val context = LocalContext.current
    val sharedFlow = Channel.sharedFlow
    LaunchedEffect(sharedFlow) {
        sharedFlow.collect { event ->
            when (event) {
                is PickFileResultEvent -> {
                    if (event.tag != PickFileTag.RESTORE) return@collect
                    event.uris.firstOrNull()?.let {
                        backupRestoreVM.restore(context, it)
                    }
                }
                is ExportFileResultEvent -> {
                    if (event.type != ExportFileType.BACKUP) return@collect
                    backupRestoreVM.backup(context, event.uri)
                }
            }
        }
    }
    PScaffold(
        topBar = {
            PTopAppBar(navController = navController, title = stringResource(Res.string.backup_restore))
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
                    .padding(horizontal = 24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(Res.string.backup_desc),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 48.dp, bottom = 48.dp)
                    )
                    PCircularButton(
                        text = stringResource(Res.string.backup),
                        icon = Res.drawable.database_backup,
                        description = stringResource(Res.string.backup),
                        onClick = {
                            val fileName = "backup_" + Date().formatName() + ".zip"
                            if (isP()) {
                                backupRestoreVM.backupToFile(context, fileName)
                            } else {
                                sendEvent(ExportFileEvent(ExportFileType.BACKUP, fileName))
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    PCircularButton(
                        text = stringResource(Res.string.restore),
                        icon = Res.drawable.archive_restore,
                        description = stringResource(Res.string.restore),
                        onClick = {
                            sendEvent(PickFileEvent(PickFileTag.RESTORE, PickFileType.FILE, false))
                        }
                    )
                }
            }
        }
    )
}
