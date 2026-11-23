
object ChannelSystemMessageSender {

     




    private suspend fun buildMemberPeers(channel: DChatChannel): List<ChannelSystemMessages.MemberPeerInfo> {
        return channel.getPeersAsync().map { peer ->
            ChannelSystemMessages.MemberPeerInfo(
                id = peer.id,
                name = peer.name,
                publicKey = peer.publicKey,
                deviceType = peer.deviceType,
                ip = peer.ip,
                port = peer.port,
            )
        }
    }

     








    suspend fun sendInvite(channel: DChatChannel, peer: DPeer): Boolean {
        val payload = jsonEncode(
            ChannelSystemMessages.ChannelInvite(
                channelId = channel.id,
                channelName = channel.name,
                owner = TempData.clientId,
                key = channel.key,
                members = channel.members,
                memberPeers = buildMemberPeers(channel),
                version = channel.version,
            )
        )
        return sendToPeer(peer, ChannelSystemMessages.TYPE_INVITE, payload)
    }

     


    suspend fun sendInviteAccept(channelId: String, ownerPeer: DPeer): Boolean {
        val context = MainApp.instance
        val publicKey = SignatureHelper.getRawPublicKeyBase64Async()
        val deviceType = PhoneHelper.getDeviceType(context).value
        val payload = jsonEncode(
            ChannelSystemMessages.ChannelInviteAccept(
                channelId = channelId,
                publicKey = publicKey,
                name = TempData.deviceName.value,
                deviceType = deviceType,
            )
        )
        return sendToPeer(ownerPeer, ChannelSystemMessages.TYPE_INVITE_ACCEPT, payload)
    }

     
    suspend fun sendInviteDecline(channelId: String, ownerPeer: DPeer): Boolean {
        val payload = jsonEncode(ChannelSystemMessages.ChannelInviteDecline(channelId))
        return sendToPeer(ownerPeer, ChannelSystemMessages.TYPE_INVITE_DECLINE, payload)
    }

     

    suspend fun broadcastUpdate(channel: DChatChannel) {
        val payload = jsonEncode(
            ChannelSystemMessages.ChannelUpdate(
                channelId = channel.id,
                channelName = channel.name,
                members = channel.members,
                memberPeers = buildMemberPeers(channel),
                version = channel.version,
            )
        )
        sendToMultiplePeers(channel.memberIdsNotMe(TempData.clientId), ChannelSystemMessages.TYPE_UPDATE, payload, channel.id, channel.key)
    }

     
    suspend fun sendKick(channelId: String, peer: DPeer, channelKey: String = ""): Boolean {
        val payload = jsonEncode(ChannelSystemMessages.ChannelKick(channelId))
        return sendToPeer(peer, ChannelSystemMessages.TYPE_KICK, payload, channelId, channelKey)
    }

     
    suspend fun broadcastKick(channel: DChatChannel) {
        val payload = jsonEncode(ChannelSystemMessages.ChannelKick(channel.id))
        sendToMultiplePeers(channel.memberIdsNotMe(TempData.clientId), ChannelSystemMessages.TYPE_KICK, payload, channel.id, channel.key)
    }

     
    suspend fun sendLeave(channelId: String, ownerPeer: DPeer, channelKey: String = ""): Boolean {
        val payload = jsonEncode(ChannelSystemMessages.ChannelLeave(channelId))
        return sendToPeer(ownerPeer, ChannelSystemMessages.TYPE_LEAVE, payload, channelId, channelKey)
    }

    private suspend fun sendToPeer(peer: DPeer, type: String, payload: String, channelId: String = "", channelKey: String = ""): Boolean {
        return try {
            val response = PeerGraphQLClient.sendChannelSystemMessage(
                peer = peer,
                clientId = TempData.clientId,
                type = type,
                payload = payload,
                channelId = channelId,
                channelKey = channelKey,
            )
            if (response != null && response.isSuccess) {
                LogCat.d("Channel system message [$type] sent to ${peer.id}")
                true
            } else {
                val errors = response?.errors?.joinToString { it.message } ?: "no response"
                LogCat.e("Failed to send [$type] to ${peer.id}: $errors")
                false
            }
        } catch (e: Exception) {
            LogCat.e("Error sending [$type] to ${peer.id}: ${e.message}")
            false
        }
    }

    private suspend fun sendToMultiplePeers(peerIds: List<String>, type: String, payload: String, channelId: String = "", channelKey: String = "") {
        val peerDao = AppDatabase.instance.peerDao()
        for (peerId in peerIds) {
            val peer = peerDao.getById(peerId) ?: continue
            sendToPeer(peer, type, payload, channelId, channelKey)
        }
    }
}
