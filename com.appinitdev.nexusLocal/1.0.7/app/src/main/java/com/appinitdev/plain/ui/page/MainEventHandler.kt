@Composable
fun MainEventCollector(
    scope: CoroutineScope,
    context: Context,
    chatVM: ChatViewModel,
    audioPlaylistVM: AudioPlaylistViewModel,
    pomodoroVM: PomodoroViewModel,
    peerVM: PeerViewModel,
    onConfirmDialog: (ConfirmDialogEvent) -> Unit,
    onLoadingDialog: (LoadingDialogEvent) -> Unit,
    onToast: (ToastEvent) -> Unit,
    clearToast: () -> Unit,
) {
    var dismissToastJob: Job? = null
    val sharedFlow = Channel.sharedFlow
    LaunchedEffect(sharedFlow) {
        sharedFlow.collect { event ->
            when (event) {
                is ConfirmDialogEvent -> onConfirmDialog(event)
                is LoadingDialogEvent -> onLoadingDialog(event)
                is ToastEvent -> {
                    onToast(event)
                    dismissToastJob?.cancel()
                    dismissToastJob = coIO { delay(event.duration); clearToast() }
                }
                is AudioActionEvent -> {
                    if (event.action == AudioAction.MEDIA_ITEM_TRANSITION) {
                        scope.launch(Dispatchers.IO) { audioPlaylistVM.loadAsync(context) }
                    }
                }
                is FetchLinkPreviewsEvent -> {
                    scope.launch(Dispatchers.IO) {
                        val data = event.chat.content.value as? DMessageText ?: return@launch
                        val urls = LinkPreviewHelper.extractUrls(data.text)
                        if (urls.isEmpty()) return@launch
                        ChatMessageEditor.updateTextAsync(context, event.chat, data.text)
                        chatVM.update(event.chat)
                    }
                }
                is HPomodoroStartEvent -> {
                    pomodoroVM.timeLeft.intValue = event.timeLeft
                    pomodoroVM.startSession()
                }
                is HPomodoroPauseEvent -> pomodoroVM.pauseSession()
                is HPomodoroStopEvent -> pomodoroVM.resetTimer()
                is HDownloadTaskDoneEvent -> {
                    scope.launch(Dispatchers.IO) {
                        val chat = AppDatabase.instance.chatDao().getById(event.downloadTask.messageId)
                        if (chat != null) {
                            chatVM.update(chat)
                            val m = chat.toModel()
                            m.data = m.getContentData()
                            sendEvent(WebSocketEvent(EventType.MESSAGE_UPDATED, JsonHelper.jsonEncode(listOf(m))))
                        }
                    }
                }
                is HMessageUpdatedEvent -> {
                    scope.launch(Dispatchers.IO) {
                        val chat = AppDatabase.instance.chatDao().getById(event.id)
                        if (chat != null) {
                            chatVM.update(chat)
                            val m = chat.toModel()
                            m.data = m.getContentData()
                            sendEvent(WebSocketEvent(EventType.MESSAGE_UPDATED, JsonHelper.jsonEncode(listOf(m))))
                        }
                    }
                }
                is ChannelUpdatedEvent -> peerVM.loadPeers()
                else -> {}
            }
        }
    }
}
