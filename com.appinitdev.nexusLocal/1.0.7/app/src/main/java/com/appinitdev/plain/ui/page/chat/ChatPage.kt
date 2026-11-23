
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPage(
    navController: NavHostController,
    audioPlaylistVM: AudioPlaylistViewModel,
    chatVM: ChatViewModel,
    peerVM: PeerViewModel,
    channelVM: ChannelViewModel,
    id: String = "",
) {
    val context = LocalContext.current
    val itemsState = chatVM.itemsFlow.collectAsState()
    val chatState = chatVM.chatState.collectAsState()
    val channelsState = channelVM.channels.collectAsState()
    val scope = rememberCoroutineScope()
    var inputValue by remember { mutableStateOf("") }
    var showForwardDialog by remember { mutableStateOf(false) }
    var messageToForward by remember { mutableStateOf<VChat?>(null) }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val imageWidthDp = remember { (configuration.screenWidthDp.dp - 44.dp) / 3 }
    val imageWidthPx = remember(imageWidthDp) { derivedStateOf { density.run { imageWidthDp.toPx().toInt() } } }
    val refreshState = rememberRefreshLayoutState {
        scope.launch(Dispatchers.IO) {
            chatVM.fetchAsync(chatState.value.target.toId)
            setRefreshState(RefreshContentState.Finished)
        }
    }
    val scrollState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val previewerState = rememberPreviewerState()

    ChatPageEffects(id, chatVM, peerVM, scrollState, focusManager, scope, onInputLoaded = { inputValue = it })

    BackHandler(enabled = chatVM.selectMode.value || previewerState.visible) {
        if (previewerState.visible) scope.launch { previewerState.closeTransform() }
        else chatVM.exitSelectMode()
    }

    val pageTitle = if (chatVM.selectMode.value) {
        LocaleHelper.getStringF(Res.string.x_selected, "count", chatVM.selectedIds.size)
    } else {
        val state = chatState.value
        if (state.target.type == ChatTargetType.CHANNEL) {
            val channel = channelsState.value.find { it.id == state.target.toId }
            if (channel != null) "${state.toName} (${channel.joinedMembers().size})" else state.toName
        } else state.toName
    }

    val channel = if (chatState.value.target.type == ChatTargetType.CHANNEL) channelsState.value.find { it.id == chatState.value.target.toId } else null

    PScaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            PTopAppBar(
                modifier = Modifier.combinedClickable(onClick = {}, onDoubleClick = { scope.launch { scrollState.scrollToItem(0) } }),
                navController = navController,
                navigationIcon = {
                    if (chatVM.selectMode.value) NavigationCloseIcon { chatVM.exitSelectMode() }
                    else NavigationBackIcon { navController.navigateUp() }
                },
                title = pageTitle,
                actions = {
                    if (chatVM.selectMode.value) {
                        PTopRightButton(label = stringResource(if (chatVM.isAllSelected()) Res.string.unselect_all else Res.string.select_all), click = { chatVM.toggleSelectAll() })
                        HorizontalSpace(dp = 8.dp)
                    } else {
                        ActionButtonMore(onClick = {
                            navController.navigate(Routing.ChatInfo(id))
                        })
                    }
                },
            )
        },
        bottomBar = { AnimatedBottomAction(visible = chatVM.showBottomActions()) { ChatSelectModeBottomActions(chatVM) } },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            PullToRefresh(modifier = Modifier.weight(1f), refreshLayoutState = refreshState) {
                LazyColumnScrollbar(state = scrollState) {
                    LazyColumn(state = scrollState, reverseLayout = true, verticalArrangement = Arrangement.Top) {
                        item(key = "") { VerticalSpace(dp = paddingValues.calculateBottomPadding()) }
                        itemsIndexed(itemsState.value, key = { _, a -> a.id }) { index, m ->
                            ChatListItem(
                                navController = navController, chatVM = chatVM, audioPlaylistVM,
                                itemsState.value, m = m,
                                peer = (if (chatState.value.target.type == ChatTargetType.PEER) ChatCacheManager.peerMap[chatState.value.target.toId] else null)
                                    ?: ChatCacheManager.peerMap[m.fromId],
                                index = index, imageWidthDp = imageWidthDp, imageWidthPx = imageWidthPx.value,
                                focusManager = focusManager, previewerState = previewerState,
                                onForward = { message ->
                                    messageToForward = message
                                    showForwardDialog = true
                                },
                            )
                        }
                    }
                }
            }
            val peer = if (chatState.value.target.type == ChatTargetType.PEER) ChatCacheManager.peerMap[chatState.value.target.toId] else null
            val notAllowChat = (channel != null && !channel.isJoined()) || peer?.status == "unpaired"
            if (notAllowChat) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp), contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(
                            if (peer?.status == "unpaired")
                                Res.string.unpaired
                            else if (channel?.status == DChatChannel.STATUS_KICKED)
                                Res.string.channel_kicked_notice
                            else
                                Res.string.channel_left_notice
                        ),
                        color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, textAlign = TextAlign.Center,
                    )
                }
                Spacer(modifier = Modifier.navigationBarsPadding())
            } else if (!chatVM.showBottomActions() && (peer == null || peer.status == "paired")) {
                ChatInput(
                    value = inputValue,
                    hint = stringResource(Res.string.chat_input_hint),
                    onValueChange = { inputValue = it; scope.launch(Dispatchers.IO) { ChatInputTextPreference.putAsync(it) } },
                    onSend = {
                        if (inputValue.isEmpty()) return@ChatInput
                        scope.launch {
                            chatVM.sendTextMessage(inputValue, context)
                            inputValue = ""
                            withIO { ChatInputTextPreference.putAsync("") }
                            scrollState.scrollToItem(0)
                        }
                    })
            }
        }
    }

    MediaPreviewer(state = previewerState)

    if (showForwardDialog) {
        ForwardTargetDialog(
            peerVM = peerVM, onDismiss = { showForwardDialog = false; messageToForward = null },
            onTargetSelected = { target ->
                messageToForward?.let { message ->
                    chatVM.forwardMessage(message.id, target) { DialogHelper.showSuccess(Res.string.sent) }
                }
            })
    }
}
