
@Composable
fun ChatListPage(
    navController: NavHostController, mainVM: MainViewModel, peerVM: PeerViewModel,
    channelVM: ChannelViewModel,
) {
    val context = LocalContext.current
    val pairedPeers = peerVM.pairedPeers
    val unpairedPeers = peerVM.unpairedPeers
    val webEnabled = LocalWeb.current
    val showOnlineStatus = webEnabled && mainVM.httpServerState == HttpServerState.ON
    val isDiscoverable = remember {
        context.dataStore.dataFlow.map {
            NearbyDiscoverablePreference.get(it)
        }
    }
        .collectAsStateValue(initial = NearbyDiscoverablePreference.default)
    val refreshState = rememberRefreshLayoutState {
        PeerStatusManager.reconnectNow("chat_list_pull_refresh")
        peerVM.loadPeers()
        setRefreshState(RefreshContentState.Finished)
    }
    var showCreateChannelDialog by channelVM.showCreateChannelDialog
    var renameChannelId by remember { mutableStateOf<String?>(null) }
    var renameChannelName by remember { mutableStateOf("") }
    val channels = channelVM.channels.collectAsStateValue()

    PScaffold(
        topBar = { TopBarChat(navController, channelVM, onNavigateBack = { navController.popBackStack() }) },
    ) { paddingValues ->
        PullToRefresh(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()), refreshLayoutState = refreshState
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item { TopSpace() }
                item {
                    if (!webEnabled) PAlert(
                        description = stringResource(Res.string.web_service_required_for_chat),
                        AlertType.WARNING
                    ) {
                        PFilledButton(
                            text = stringResource(Res.string.enable_web_service),
                            buttonSize = ButtonSize.SMALL,
                            onClick = {
                                mainVM.enableHttpServer(context, true)
                            })
                    }
                }
                item {
                    PeerListItem(
                        title = stringResource(Res.string.local_chat),
                        desc = stringResource(Res.string.local_chat_desc),
                        icon = Res.drawable.bot,
                        latestChat = peerVM.getLatestChat("local"),
                        modifier = PlainTheme.getCardModifier(),
                        onClick = { navController.navigate(Routing.Chat("peer:local")) })
                }
                if (channels.isNotEmpty()) {
                    item {
                        VerticalSpace(dp = 16.dp)
                        Subtitle(stringResource(Res.string.channels))
                    }
                    itemsIndexed(items = channels.toList(), key = { _, i -> i.id }) { index, channel ->
                        PeerListItem(
                            title = channel.name,
                            desc = "",
                            icon = Res.drawable.hash,
                            latestChat = peerVM.getLatestChat(channel.id),
                            onClick = {
                                navController.navigate(Routing.Chat("channel:${channel.id}"))
                            },
                            modifier = PlainTheme.getCardModifier(index, channels.size)
                        )
                    }
                }
                val allPeers = pairedPeers + unpairedPeers
                if (allPeers.isNotEmpty()) {
                    item {
                        VerticalSpace(dp = 16.dp)
                        Subtitle(stringResource(Res.string.devices))
                    }
                    itemsIndexed(items = allPeers, key = { _, i -> i.id }) { index, peer ->
                        PeerListItem(
                            title = peer.name,
                            desc = if (peer.isPaired()) peer.getBestIp() else peer.ip,
                            icon = DeviceType.fromValue(peer.deviceType).getIcon(),
                            online = if (showOnlineStatus) peerVM.getPeerOnlineStatus(peer.id) else null,
                            latestChat = peerVM.getLatestChat(peer.id),
                            peerId = peer.id,
                            onDelete = { peerVM.removePeer(context, it) },
                            onClick = { navController.navigate(Routing.Chat("peer:${peer.id}")) },
                            modifier = PlainTheme.getCardModifier(index, allPeers.size)
                        )
                    }
                }
                item { BottomSpace(paddingValues) }
            }
        }

        if (showCreateChannelDialog) {
            CreateChannelDialog(onDismiss = { showCreateChannelDialog = false }, onConfirm = { showCreateChannelDialog = false; channelVM.createChannel(it) })
        }
        if (renameChannelId != null) {
            RenameChannelDialog(
                currentName = renameChannelName,
                onDismiss = { renameChannelId = null },
                onConfirm = { val id = renameChannelId!!; renameChannelId = null; channelVM.renameChannel(id, it) })
        }
    }
}
