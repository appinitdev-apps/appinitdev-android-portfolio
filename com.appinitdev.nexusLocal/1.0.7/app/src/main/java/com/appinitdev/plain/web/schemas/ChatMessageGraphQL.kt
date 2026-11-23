fun SchemaBuilder.addChatMessageSchema() {
    query("chatItems") {
        resolver { id: String ->
            val dao = AppDatabase.instance.chatDao()
            val target = ChatTarget.parseId(id)
            val items = if (target.type == ChatTargetType.CHANNEL) {
                dao.getByChannelId(target.toId)
            } else {
                dao.getByPeerId(target.toId)
            }
            items.map { it.toModel() }
        }
    }
    query("latestChatItems") {
        resolver { ->
            AppDatabase.instance.chatDao().getAllLatestChats().map { it.toModel() }
        }
    }
    type<ChatItem> {
        property("data") {
            resolver { c: ChatItem ->
                c.getContentData()
            }
        }
    }
    mutation("sendChatItem") {
        resolver { toId: String, content: String ->
            val target = ChatTarget.parseId(toId)
            val item = ChatSender.createChatItem(target, DChat.parseContent(content))
            ChatSender.send(item, target, emptySet())
            val model = item.toModel()
            sendEvent(WebSocketEvent(EventType.MESSAGE_CREATED, JsonHelper.jsonEncode(listOf(model))))
            sendEvent(HMessageCreatedEvent(target, arrayListOf(item)))
            arrayListOf(model)
        }
    }
    mutation("deleteChatItem") {
        resolver { id: ID ->
            val item = ChatDbHelper.getChatItem(id.value)
            if (item != null) {
                ChatDbHelper.deleteAsync(MainApp.instance, item.id)
                sendEvent(DeleteChatItemViewEvent(item.id))
            }
            true
        }
    }
    mutation("deleteChatItems") {
        resolver { query: String ->
            val context = MainApp.instance
            val ids = ChatDbHelper.getIdsAsync(query)
            ChatDbHelper.deleteByIdsAsync(context, ids)
            sendEvent(HChatItemsDeletedEvent(ids))
            sendEvent(WebSocketEvent(EventType.MESSAGE_DELETED, JsonHelper.jsonEncode(query)))
            true
        }
    }
    mutation("retryChatItem") {
        resolver { id: ID ->
            val item = ChatDbHelper.getChatItem(id.value) ?: return@resolver null
            ChatDbHelper.updateChatItemStatus(item, "pending")
            sendEvent(HRetryChatItemEvent(item))
            item.toModel()
        }
    }
}
