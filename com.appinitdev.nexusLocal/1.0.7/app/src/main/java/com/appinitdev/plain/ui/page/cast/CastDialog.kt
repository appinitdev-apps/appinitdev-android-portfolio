
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CastDialog(castVM: CastViewModel) {
    if (!castVM.showCastDialog.value) return
    val itemsState by castVM.itemsFlow.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val hasDevices = itemsState.isNotEmpty()
    val heroHeight by animateDpAsState(if (hasDevices) 104.dp else 156.dp, label = "castHeroHeight")
    var job by remember { mutableStateOf<Job?>(null) }
    val onDismiss = { castVM.showCastDialog.value = false }

    LaunchedEffect(Unit) {
        if (job?.isActive != true) {
            job = coIO { castVM.searchAsync(context) }
        }
    }
    LaunchedEffect(hasDevices) {
        if (hasDevices) sheetState.expand()
    }
    DisposableEffect(Unit) {
        onDispose {
            job?.cancel()
            job = null
        }
    }

    PModalBottomSheet(
        modifier = Modifier.defaultMinSize(minHeight = 360.dp),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().animateContentSize().padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PBottomSheetTopAppBar(
                title = stringResource(if (hasDevices) Res.string.cast_select_screen else Res.string.cast_searching_for_screen),
                subtitle = stringResource(if (hasDevices)  Res.string.cast_dialog_hint else Res.string.cast_looking_for_devices),
            )
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SearchingScreenAnimation(Modifier.fillMaxWidth().height(heroHeight))
            }
            if (hasDevices) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(itemsState, key = { it.hostAddress }) { device ->
                        CastDeviceCard(
                            title = device.description?.device?.friendlyName ?: device.hostAddress,
                            subtitle = device.hostAddress,
                            onClick = {
                                castVM.selectDevice(device)
                                castVM.enterCastMode()
                                AudioPlayer.pause()
                                scope.launch(Dispatchers.IO) {
                                    if (!WebPreference.getAsync()) {
                                        WebPreference.putAsync(true)
                                        sendEvent(StartHttpServerEvent())
                                    }
                                }
                                onDismiss()
                            },
                        )
                    }
                }
            }
            Tips(stringResource(Res.string.cast_dialog_wireless_cast_tip))
            BottomSpace()
        }
    }
}

