
/**
 * Handles a chat message just arrived from a remote peer: validate sender +
 * channel, persist, enqueue file downloads, trigger link-preview fetching,
 * broadcast the in-app + WebSocket events, and fire a system notification
 * when the chat is not currently active. Caller is responsible for the
 * transport-level decrypt/verify step.
 */
object ChatMessageReceiver {

    /**
     * Replay protection: tracks every (fromPeerId, signature, timestamp) we have
     * processed within [PeerChatParser.MAX_TIMESTAMP_DIFF_MS]. Without this, an
     * attacker who captured a signed chat packet could replay it within the
     * 5-minute timestamp window and create duplicate rows. The bounded set is
     * pruned lazily; one entry per (signature, timestamp) is enough to dedupe.
     */
    private val seenSignatures: MutableSet<String> =
        Collections.synchronizedSet(HashSet())

    /**
     * @param fromPeerId sender's peer id (from the `c-id` header).
     * @param fromChannelId sender's channel id (from the `c-cid` header),
     *                      or empty for a 1:1 peer chat.
     * @param signature Ed25519 signature base64 of the decrypted packet, used
     *                  for in-memory replay dedupe. Pass empty string to skip.
     * @param timestamp the signed packet's timestamp (ms), used together with
     *                  signature to form the dedupe key. Pass 0 to skip.
     * @throws IllegalStateException when the channel is unknown or left.
     * @throws Exception when the sender is unknown.
     * @throws ReplayedMessageException when [signature] + [timestamp] match a
     *         packet we already processed (i.e. this is a replay).
     */
    suspend fun receive(
        fromPeerId: String,
        content: DMessageContent,
        fromChannelId: String = "",
        signature: String = "",
        timestamp: Long = 0L,
    ): DChat {
        if (signature.isNotEmpty() && timestamp > 0L) {
            val key = ""
            if (!seenSignatures.add(key)) {
                throw ReplayedMessageException(fromPeerId, timestamp)
            }
        }

        val fromPeer = AppDatabase.instance.peerDao().getById(fromPeerId)
            ?: throw Exception("invalid peer")

        val fromChannel: DChatChannel? = if (fromChannelId.isNotEmpty()) {
            val ch = AppDatabase.instance.chatChannelDao().getById(fromChannelId)
                ?: throw IllegalStateException("Unknown channel")
            if (ch.status != DChatChannel.STATUS_JOINED) {
                throw IllegalStateException("Channel not joined")
            }
            ch
        } else null

        val item = ChatDbHelper.insertChatItem(
            message = content,
            fromId = fromPeerId,
            toId = if (fromChannelId.isEmpty()) "me" else "",
            channelId = fromChannelId,
            isRemote = false,
        )

        if (item.content.type == DMessageType.TEXT.value) {
            sendEvent(FetchLinkPreviewsEvent(item))
        }

        if (item.content.type == DMessageType.FILES.value ||
            item.content.type == DMessageType.IMAGES.value
        ) {
            val files = when (item.content.value) {
                is DMessageFiles -> (item.content.value as DMessageFiles).items
                is DMessageImages -> (item.content.value as DMessageImages).items
                else -> emptyList()
            }
            files.forEach { file ->
                DownloadQueue.addDownloadTask(
                    messageFile = file,
                    peer = fromPeer,
                    messageId = item.id,
                )
            }
        }

        sendEvent(
            HMessageCreatedEvent(
                target = if (fromChannelId.isNotEmpty()) {
                    ChatTarget(fromChannelId, ChatTargetType.CHANNEL)
                } else {
                    ChatTarget(fromPeerId, ChatTargetType.PEER)
                },
                items = arrayListOf(item),
            ),
        )
        val model = item.toModel().apply { data = getContentData() }
        sendEvent(
            WebSocketEvent(
                EventType.MESSAGE_CREATED,
                JsonHelper.jsonEncode(listOf(model)),
            ),
        )

        emitNotificationIfNeeded(item, fromPeer, fromChannel)
        return item
    }

    @SuppressLint("MissingPermission")
    private fun emitNotificationIfNeeded(
        item: DChat,
        fromPeer: DPeer,
        fromChannel: DChatChannel?,
    ) {
        if (!Permission.POST_NOTIFICATIONS.can(MainApp.instance)) return
        val preview = item.getMessagePreview()
        val (targetId, targetName, messageText) = if (fromChannel == null) {
            NotificationPayload(
                targetId = "peer:${fromPeer.id}",
                targetName = fromPeer.name.ifEmpty { LocaleHelper.getString(Res.string.peer_chat) },
                messageText = preview,
            )
        } else {
            NotificationPayload(
                targetId = "channel:${fromChannel.id}",
                targetName = fromChannel.name.ifEmpty { LocaleHelper.getString(Res.string.peer_chat) },
                messageText = "${fromPeer.name}: $preview",
            )
        }
        if (ChatCacheManager.activeToId == targetId) return
        NotificationHelper.sendChatMessageNotification(
            context = MainApp.instance,
            targetId = targetId,
            targetName = targetName,
            messageText = messageText,
        )
    }
}

private data class NotificationPayload(
    val targetId: String,
    val targetName: String,
    val messageText: String,
)

class ReplayedMessageException(val fromPeerId: String, val timestamp: Long) :
    Exception("Replayed message from $fromPeerId at $timestamp")
