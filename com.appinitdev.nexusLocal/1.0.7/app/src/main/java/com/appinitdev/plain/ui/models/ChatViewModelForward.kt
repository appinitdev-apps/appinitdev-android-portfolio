
fun ChatViewModel.resendMessage(messageId: String) {
    viewModelScope.launch(Dispatchers.IO) {
        val item = ChatDbHelper.getChatItem(messageId) ?: return@launch
        val state = chatState.value
        ChatDbHelper.updateChatItemStatus(item, "pending")
        update(item)
        ChatSender.resend(item)
    }
}

fun ChatViewModel.resendToMembers(messageId: String, peerIds: List<String>) {
    viewModelScope.launch(Dispatchers.IO) {
        val state = chatState.value
        val channel = AppDatabase.instance.chatChannelDao().getById(state.target.toId) ?: return@launch
        val item = ChatDbHelper.getChatItem(messageId) ?: return@launch
        ChatDbHelper.updateChatItemStatus(item, "pending")
        update(item)
        ChatSender.sendToChannelMembers(item, channel, peerIds)
        update(item)
    }
}

fun ChatViewModel.forwardMessage(messageId: String, target: ChatTarget, onResult: (Boolean) -> Unit = {}) {
    viewModelScope.launch(Dispatchers.IO) {
        val item = ChatDbHelper.getChatItem(messageId) ?: return@launch
        val newItem = ChatSender.createChatItem(target, item.content)
        val state = chatState.value
        ChatSender.send(newItem, target, onlinePeerIds.value)
        update(newItem)
        sendEvent(WebSocketEvent(EventType.MESSAGE_CREATED, JsonHelper.jsonEncode(listOf(newItem.toModel()))))
        onResult(newItem.status == "sent")
    }
}

fun ChatViewModel.delete(context: Context, ids: Set<String>) {
    viewModelScope.launch(Dispatchers.IO) {
        val items = itemsFlow.value.filter { ids.contains(it.id) }
        for (m in items) {
            ChatDbHelper.deleteAsync(context, m.id)
        }
        _itemsFlow.update {
            val mutableList = it.toMutableStateList()
            mutableList.removeIf { m -> ids.contains(m.id) }
            mutableList
        }
        sendEvent(WebSocketEvent(EventType.MESSAGE_DELETED, JsonHelper.jsonEncode("ids=${ids.joinToString(",")}")))
    }
}

fun ChatViewModel.clearAllMessages(context: Context) {
    viewModelScope.launch(Dispatchers.IO) {
        val state = chatState.value
        if (state.target.type == ChatTargetType.CHANNEL) {
            ChatDbHelper.deleteAllChannelChatsAsync(context, state.target.toId)
        } else {
            ChatDbHelper.deleteAllChatsAsync(context, state.target.toId)
        }
        _itemsFlow.value = mutableStateListOf()
        sendEvent(WebSocketEvent(EventType.MESSAGE_DELETED, JsonHelper.jsonEncode(state.target.encodedToId)))
    }
}
