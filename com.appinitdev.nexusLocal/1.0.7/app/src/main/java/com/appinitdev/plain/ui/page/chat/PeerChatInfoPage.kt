
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeerChatInfoPage(
    navController: NavHostController,
    chatVM: ChatViewModel,
    peerVM: PeerViewModel,
) {
    val context = LocalContext.current
    val chatState = chatVM.chatState.collectAsState()
    val peer = ChatCacheManager.peerMap[chatState.value.target.toId]

    PScaffold(
        topBar = {
            PTopAppBar(
                navController = navController,
                navigationIcon = {
                    NavigationBackIcon { navController.navigateUp() }
                },
                title = stringResource(Res.string.peer_info),
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
        ) {
            if (peer != null) {
                item {
                    PCard {
                        PListItem(title = stringResource(Res.string.peer_id), value = peer.id)
                        PListItem(title = stringResource(Res.string.ip_address), value = peer.getBestIp())
                        PListItem(title = stringResource(Res.string.port), value = peer.port.toString())
                        PListItem(title = stringResource(Res.string.device_type), value = DeviceType.fromValue(peer.deviceType).getText())
                        val status = peer.getStatusText()
                        if (status.isNotEmpty()) {
                            PListItem(title = stringResource(Res.string.status), value = status)
                        }
                    }
                }
            }

            item { VerticalSpace(dp = 24.dp) }

            item {
                val clearMessagesText = stringResource(Res.string.clear_messages)
                val clearMessagesConfirmText = stringResource(Res.string.clear_messages_confirm)
                val cancelText = stringResource(Res.string.cancel)
                POutlinedButton(
                    text = clearMessagesText,
                    type = ButtonType.DANGER,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    block = true,
                    onClick = {
                        DialogHelper.showConfirmDialog(
                            title = clearMessagesText,
                            message = clearMessagesConfirmText,
                            confirmButton = Pair(clearMessagesText) {
                                chatVM.clearAllMessages(context)
                                navController.navigateUp()
                                DialogHelper.showSuccess(Res.string.messages_cleared)
                            },
                            dismissButton = Pair(cancelText) {},
                        )
                    },
                )
            }

            if (peer != null) {
                item {
                    val deleteDeviceText = stringResource(Res.string.delete_device)
                    val deleteText = stringResource(Res.string.delete)
                    val deleteDeviceWarningText = stringResource(Res.string.delete_peer_warning)
                    val cancelText = stringResource(Res.string.cancel)
                    VerticalSpace(dp = 16.dp)
                    POutlinedButton(
                        text = deleteDeviceText,
                        type = ButtonType.DANGER,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        block = true,
                        onClick = {
                            DialogHelper.showConfirmDialog(
                                title = deleteDeviceText,
                                message = deleteDeviceWarningText,
                                confirmButton = Pair(deleteText) {
                                    peerVM.removePeer(context, peer.id)
                                    navController.popBackStack(navController.graph.startDestinationId, false)
                                },
                                dismissButton = Pair(cancelText) {},
                            )
                        },
                    )
                }
            }

            item { BottomSpace(paddingValues) }
        }
    }
}
