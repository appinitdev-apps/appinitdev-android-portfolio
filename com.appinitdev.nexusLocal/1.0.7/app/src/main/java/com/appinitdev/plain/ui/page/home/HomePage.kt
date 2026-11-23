@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    navController: NavHostController,
    mainVM: MainViewModel,
    updateVM: UpdateViewModel,
    peerVM: PeerViewModel,
    channelVM: ChannelViewModel,
) {
    val webEnabled = LocalWeb.current
    val context = LocalContext.current
    val showOnlineStatus = webEnabled && mainVM.httpServerState == HttpServerState.ON
    var systemAlertWindow by remember { mutableStateOf(Permission.SYSTEM_ALERT_WINDOW.can(context)) }
    val refreshState = rememberRefreshLayoutState {
        PeerStatusManager.reconnectNow("home_pull_refresh")
        peerVM.loadPeers()
        channelVM.refresh()
        setRefreshState(RefreshContentState.Finished)
    }
    LaunchedEffect(Unit) {
        peerVM.loadPeers()
    }
    LaunchedEffect(Unit) {
        Channel.sharedFlow.collect { event ->
            if (updateVM.consumeUpdateDownloadEvent(event)) {
                return@collect
            }
            when (event) {
                is PermissionsResultEvent -> {
                    systemAlertWindow = Permission.SYSTEM_ALERT_WINDOW.can(context)
                }
                is WindowFocusChangedEvent -> {
                    mainVM.isVPNConnected = NetworkHelper.isVPNConnected(context)
                    mainVM.ip4s = NetworkHelper.getDeviceIP4s().filter { it.isNotEmpty() }
                    mainVM.ip4 = NetworkHelper.getDeviceIP4().ifEmpty { "127.0.0.1" }
                    systemAlertWindow = Permission.SYSTEM_ALERT_WINDOW.can(context)
                }
            }
        }
    }
    UpdateDialog(updateVM)
    PScaffold(
        topBar = { TopBarHome(navController, peerVM) },
    ) { paddingValues ->
        PullToRefresh(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            refreshLayoutState = refreshState,
        ) {
            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    TopSpace()
                    if (webEnabled) {
                        if (mainVM.isVPNConnected) {
                            PAlert(
                                description = stringResource(Res.string.vpn_web_conflict_warning),
                                AlertType.WARNING,
                            )
                        }
                    }
                }
                item {
                    HomeWeb(context, navController, mainVM, webEnabled)
                    VerticalSpace(dp = 16.dp)
                }
                item {
                    HomeShortcutGrid(navController = navController, peerVM = peerVM, channelVM = channelVM, showOnlineStatus = showOnlineStatus)
                    VerticalSpace(dp = 16.dp)
                }
                item {
                    BottomSpace(paddingValues)
                }
            }
        }
    }
}
