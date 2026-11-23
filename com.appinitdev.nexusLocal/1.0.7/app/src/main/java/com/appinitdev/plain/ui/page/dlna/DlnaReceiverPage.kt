@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DlnaReceiverPage(
    navController: NavHostController,
    vm: DlnaReceiverViewModel = viewModel(),
) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        vm.startReceiver(context)
        onDispose { vm.stopReceiver(context) }
    }
    val mediaUri by DlnaRendererState.mediaUri.collectAsState()
    val mediaType by DlnaRendererState.mediaType.collectAsState()
    val playbackState by DlnaRendererState.playbackState.collectAsState()
    val pendingCastRequest by DlnaRendererState.pendingCastRequest.collectAsState()
    val startError by DlnaRendererState.startError.collectAsState()
    val isRetrying by vm.isRetrying.collectAsState()
    val hasMedia = mediaUri.isNotEmpty() && playbackState != DlnaPlaybackState.NO_MEDIA_PRESENT
    pendingCastRequest?.let { pending ->
        var rememberChoice by remember { mutableStateOf(false) }
        val displayName = pending.senderName.ifEmpty { pending.senderIp.ifEmpty { stringResource(Res.string.unknown) } }
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(Res.string.dlna_cast_request_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(Res.string.dlna_cast_request_desc, displayName))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Checkbox(
                            checked = rememberChoice,
                            onCheckedChange = { rememberChoice = it },
                        )
                        Text(
                            text = stringResource(Res.string.dlna_cast_remember_choice),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { vm.acceptCastRequest(context, rememberChoice) }) {
                    Text(stringResource(Res.string.dlna_cast_accept))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { vm.rejectCastRequest(context, rememberChoice) }) {
                    Text(stringResource(Res.string.dlna_cast_reject))
                }
            },
        )
    }
    if (hasMedia) {
        val exitAction = {
            DlnaRendererState.mediaUri.value = ""
            DlnaRendererState.playbackState.value = DlnaPlaybackState.NO_MEDIA_PRESENT
        }
        BackHandler { exitAction() }
        when (mediaType) {
            DlnaMediaType.AUDIO -> DlnaReceiverAudioPlayer(vm = vm, onExit = exitAction)
            DlnaMediaType.IMAGE -> DlnaReceiverImageViewer(onExit = exitAction)
            else -> DlnaReceiverVideoPlayer(vm = vm, onExit = exitAction)
        }
        return
    }
    PScaffold(
        topBar = {
            PTopAppBar(
                navController = navController,
                title = stringResource(Res.string.dlna_receiver),
                actions = {
                    PIconButton(
                        icon = Res.drawable.history,
                        contentDescription = stringResource(Res.string.dlna_cast_history),
                        tint = MaterialTheme.colorScheme.onSurface
                    ) { navController.navigate(Routing.DlnaCastHistory) }
                },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (startError.isNotEmpty()) {
                item {
                    PAlert(
                        description = startError,
                        type = AlertType.ERROR,
                        actions = {
                            PFilledButton(
                                stringResource(Res.string.retry),
                                buttonSize = ButtonSize.SMALL,
                                isLoading = isRetrying,
                                onClick = { vm.retryReceiver(context) }
                            )
                        },
                    )
                }
            }
            item { DlnaReceiverWaitingScreen() }
            item { BottomSpace() }
        }
    }
}
