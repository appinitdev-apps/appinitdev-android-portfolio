
object ChatSender {
    suspend fun createChatItem(target: ChatTarget, content: DMessageContent): DChat {
        val item = ChatDbHelper.insertChatItem(
            message = content,
            fromId = "me",
            toId = if (target.type == ChatTargetType.PEER) target.toId else "",
            channelId = if (target.type == ChatTargetType.CHANNEL) target.toId else "",
            isRemote = !target.isLocal(),
        )
        if (item.content.type == DMessageType.TEXT.value) {
            sendEvent(FetchLinkPreviewsEvent(item))
        }
        return item
    }

    suspend fun send(
        item: DChat,
        target: ChatTarget,
        onlinePeerIds: Set<String>,
    ) {
        if (target.isLocal()) {
            return
        }

        when (target.type) {
            ChatTargetType.PEER -> {
                val peer = AppDatabase.instance.peerDao().getById(target.toId) ?: return
                sendToPeer(item, peer)
            }

            ChatTargetType.CHANNEL -> {
                val channel = AppDatabase.instance.chatChannelDao().getById(target.toId) ?: return
                sendToChannel(item, channel, onlinePeerIds)
            }
        }
    }

    suspend fun resend(item: DChat) {
        send(item, item.target(), currentOnlinePeers())
        sendEvent(HMessageUpdatedEvent(item.id))
    }

    private fun currentOnlinePeers(): Set<String> {
        return com.appinitdev.nexusLocal.chat.peer.PeerStatusManager.onlinePeers()
    }

    private fun DChat.target(): ChatTarget = when {
        channelId.isNotEmpty() -> ChatTarget(channelId, ChatTargetType.CHANNEL)
        toId.isEmpty() || toId == "local" -> ChatTarget("local", ChatTargetType.PEER)
        else -> ChatTarget(toId, ChatTargetType.PEER)
    }

    fun triggerPeerRediscovery(peerId: String) {
        val key = ChatCacheManager.peerKeyCache[peerId]
        if (key != null) {
            NearbyDiscoverManager.discoverSpecificDevice(peerId, key)
        }
    }

    suspend fun sendToPeer(item: DChat, peer: DPeer) {
        val error = PeerChatSender.send(peer, item.content)
        if (error != null) {
            triggerPeerRediscovery(peer.id)
        }
        ChatDbHelper.updateChatItemStatus(item, peer, error)
    }

    suspend fun sendToChannel(item: DChat, channel: DChatChannel, onlinePeerIds: Set<String> = emptySet()) {
        when (val result = ChannelChatSender.send(channel, item.content)) {
            is ChannelChatSender.Result.Status -> {
                ChatDbHelper.updateChannelChatItemStatus(item, result.data)
            }

            ChannelChatSender.Result.NoLeader -> {
                
                
                channel.getRecipientIds().forEach { triggerPeerRediscovery(it) }
                ChatDbHelper.updateChannelChatItemStatus(item, null)
            }

            is ChannelChatSender.Result.LeaderPeerMissing -> {
                
                
                
                triggerPeerRediscovery(result.leaderId)
                channel.getRecipientIds()
                    .filter { it != result.leaderId }
                    .forEach { triggerPeerRediscovery(it) }
                ChatDbHelper.updateChannelChatItemStatus(item, null)
            }
        }
    }

    suspend fun sendToChannelMembers(item: DChat, channel: DChatChannel, peerIds: List<String>) {
        val newResults = ChannelChatSender.sendToRecipients(channel, peerIds, item.content)
        val existing = item.parseStatusData()?.results ?: emptyList()
        val retriedIds = peerIds.toSet()
        val merged = existing.filter { it.peerId !in retriedIds } + newResults.results
        val mergedStatusData = DMessageStatusData(merged)

        ChatDbHelper.updateChannelChatItemStatus(item, mergedStatusData)
    }
}