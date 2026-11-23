
@Composable
fun ChatPageEffects(
    id: String,
    chatVM: ChatViewModel,
    peerVM: PeerViewModel,
    scrollState: LazyListState,
    focusManager: FocusManager,
    scope: CoroutineScope,
    onInputLoaded: (String) -> Unit,
) {
    val context = LocalContext.current
    val sharedFlow = Channel.sharedFlow

    DisposableEffect(id) {
        ChatCacheManager.activeToId = id
        onDispose {
            ChatCacheManager.activeToId = ""
        }
    }

    LaunchedEffect(Unit) {
        onInputLoaded(ChatInputTextPreference.getAsync())
        scope.launch(Dispatchers.IO) {
            chatVM.initializeChatStateAsync(id)
            chatVM.fetchAsync(chatVM.chatState.value.target.toId)
        }
        peerVM.loadPeers()
    }

    LaunchedEffect(sharedFlow) {
        sharedFlow.collect { event ->
            when (event) {
                is DeleteChatItemViewEvent -> chatVM.remove(event.id)
                is HChatItemsDeletedEvent -> chatVM.removeIds(event.ids)
                is HMessageCreatedEvent -> {
                    if (chatVM.chatState.value.target == event.target) {
                        chatVM.addAll(event.items)
                        scope.launch { scrollState.scrollToItem(0) }
                    }
                }

                is PickFileResultEvent -> {
                    if (event.tag != PickFileTag.SEND_MESSAGE) return@collect
                    handleFileSelection(event, context, chatVM, scrollState, focusManager)
                }
            }
        }
    }
}
