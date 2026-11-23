object ChannelSystemMessageHandler {
    fun handle(fromId: String, type: String, payload: String) {
        try {
            when (type) {
                ChannelSystemMessages.TYPE_INVITE -> handleInvite(fromId, jsonDecode(payload))
                ChannelSystemMessages.TYPE_INVITE_ACCEPT -> handleInviteAccept(fromId, jsonDecode(payload))
                ChannelSystemMessages.TYPE_INVITE_DECLINE -> handleInviteDecline(fromId, jsonDecode(payload))
                ChannelSystemMessages.TYPE_UPDATE -> handleUpdate(fromId, jsonDecode(payload))
                ChannelSystemMessages.TYPE_KICK -> handleKick(fromId, jsonDecode(payload))
                ChannelSystemMessages.TYPE_LEAVE -> handleLeave(fromId, jsonDecode(payload))
                else -> LogCat.e("Unknown channel system message type: $type")
            }
        } catch (e: Exception) {
            LogCat.e("Error handling channel system message [$type] from $fromId: ${e.message}")
        }
    }
    private fun ensureChannelPeer(
        id: String,
        name: String,
        publicKey: String,
        deviceType: String,
        ip: String = "",
        port: Int = 0,
        logTag: String = "member",
    ): Boolean {
        val peerDao = AppDatabase.instance.peerDao()
        if (peerDao.getById(id) != null) return false
        peerDao.insert(
            DPeer(
                id = id,
                name = name,
                publicKey = publicKey,
                status = "channel",
                deviceType = deviceType,
                ip = ip,
                port = port,
            ),
        )
        LogCat.d("Created channel peer record for $logTag $id")
        return true
    }
    private fun handleInvite(fromId: String, msg: ChannelSystemMessages.ChannelInvite) {
        val dao = AppDatabase.instance.chatChannelDao()
        val peerDao = AppDatabase.instance.peerDao()
        val existingChannel = dao.getById(msg.channelId)
        val isReinvite = existingChannel != null &&
            (existingChannel.status == DChatChannel.STATUS_LEFT || existingChannel.status == DChatChannel.STATUS_KICKED)
        if (msg.owner != fromId) {
            LogCat.e("Invite from $fromId but payload claims owner=${msg.owner} — rejected")
            return
        }
        if (existingChannel != null && !isReinvite) {
            LogCat.d("Channel ${msg.channelId} already exists locally, ignoring invite")
            return
        }
        val peer = peerDao.getById(fromId) ?: run {
            LogCat.e("Invite from unknown peer $fromId — ignored")
            return
        }
        for (memberInfo in msg.memberPeers) {
            ensureChannelPeer(
                id = memberInfo.id,
                name = memberInfo.name,
                publicKey = memberInfo.publicKey,
                deviceType = memberInfo.deviceType,
                ip = memberInfo.ip,
                port = memberInfo.port,
            )
        }
        if (isReinvite) {
            val channel = existingChannel
            channel.name = msg.channelName
            channel.key = msg.key
            channel.owner = fromId
            channel.members = msg.members
            channel.version = msg.version
            channel.status = DChatChannel.STATUS_JOINED
            dao.update(channel)
            LogCat.d("Re-invite for channel ${msg.channelId} (was ${existingChannel.status}), restored to joined")
        } else {
            val channel = DChatChannel()
            channel.id = msg.channelId
            channel.name = msg.channelName
            channel.key = msg.key
            channel.owner = fromId
            channel.members = msg.members
            channel.version = msg.version
            dao.insert(channel)
        }
        runBlocking(Dispatchers.IO) {
            ChatCacheManager.loadKeyCacheAsync()
        }
        val peerName = peer.name.ifEmpty { fromId }
        sendEvent(
            ChannelInviteReceivedEvent(
                channelId = msg.channelId,
                channelName = msg.channelName,
                ownerPeerId = fromId,
                ownerPeerName = peerName,
            )
        )
        sendEvent(ChannelUpdatedEvent())
        LogCat.d("Channel invite received: ${msg.channelName} from $fromId")
    }
    private fun handleInviteAccept(fromId: String, msg: ChannelSystemMessages.ChannelInviteAccept) {
        val dao = AppDatabase.instance.chatChannelDao()
        val peerDao = AppDatabase.instance.peerDao()
        val channel = dao.getById(msg.channelId) ?: run {
            LogCat.e("InviteAccept for unknown channel ${msg.channelId}")
            return
        }
        if (channel.owner != "me") {
            LogCat.e("InviteAccept received but we are not the owner of ${msg.channelId}")
            return
        }
        val existingPeer = peerDao.getById(fromId)
        if (existingPeer == null) {
            ensureChannelPeer(
                id = fromId,
                name = msg.name,
                publicKey = msg.publicKey,
                deviceType = msg.deviceType,
                logTag = "accepting member",
            )
        } else if (existingPeer.publicKey.isEmpty() && msg.publicKey.isNotEmpty()) {
            existingPeer.publicKey = msg.publicKey
            if (msg.name.isNotEmpty() && existingPeer.name.isEmpty()) {
                existingPeer.name = msg.name
            }
            peerDao.update(existingPeer)
        }
        val member = channel.findMember(fromId)
        if (member == null) {
            LogCat.e("InviteAccept from $fromId for channel ${msg.channelId} but peer is not in members list — rejected")
            return
        }
        if (!member.isPending()) {
            LogCat.d("InviteAccept from $fromId but member is not pending (status=${member.status}), ignoring")
            return
        }
        channel.members = channel.members.map {
            if (it.id == fromId) it.copy(status = ChannelMember.STATUS_JOINED) else it
        }
        channel.version++
        channel.updatedAt = TimeHelper.now()
        dao.update(channel)
        runBlocking(Dispatchers.IO) {
            ChannelSystemMessageSender.broadcastUpdate(channel)
            ChatCacheManager.loadKeyCacheAsync()
        }
        sendEvent(ChannelUpdatedEvent())
        LogCat.d("Peer $fromId accepted invite for channel ${msg.channelId}")
    }
    private fun handleInviteDecline(fromId: String, msg: ChannelSystemMessages.ChannelInviteDecline) {
        val dao = AppDatabase.instance.chatChannelDao()
        val channel = dao.getById(msg.channelId) ?: return
        if (channel.owner != "me") return
        if (channel.hasMember(fromId)) {
            channel.members = channel.members.filter { it.id != fromId }
            channel.version++
            channel.updatedAt = TimeHelper.now()
            dao.update(channel)
        }
        sendEvent(ChannelUpdatedEvent())
        LogCat.d("Peer $fromId declined invite for channel ${msg.channelId}")
    }
    private fun handleUpdate(fromId: String, msg: ChannelSystemMessages.ChannelUpdate) {
        val dao = AppDatabase.instance.chatChannelDao()
        val peerDao = AppDatabase.instance.peerDao()
        val channel = dao.getById(msg.channelId)
        if (channel == null) {
            LogCat.e("ChannelUpdate for unknown channel ${msg.channelId}")
            return
        }
        if (channel.owner != fromId) {
            LogCat.e("ChannelUpdate from non-owner $fromId (owner=${channel.owner}) — rejected")
            return
        }
        if (msg.version <= channel.version) {
            LogCat.d("Ignoring stale ChannelUpdate (local=${channel.version}, remote=${msg.version})")
            return
        }
        for (memberInfo in msg.memberPeers) {
            ensureChannelPeer(
                id = memberInfo.id,
                name = memberInfo.name,
                publicKey = memberInfo.publicKey,
                deviceType = memberInfo.deviceType,
                ip = memberInfo.ip,
                port = memberInfo.port,
                logTag = "member via update",
            )
        }
        channel.name = msg.channelName
        channel.members = msg.members
        channel.version = msg.version
        channel.updatedAt = TimeHelper.now()
        dao.update(channel)
        runBlocking(Dispatchers.IO) {
            ChatCacheManager.loadKeyCacheAsync()
        }
        sendEvent(ChannelUpdatedEvent())
        LogCat.d("Channel ${msg.channelId} updated to version ${msg.version}")
    }
    private fun handleKick(fromId: String, msg: ChannelSystemMessages.ChannelKick) {
        val dao = AppDatabase.instance.chatChannelDao()
        val channel = dao.getById(msg.channelId) ?: return
        if (channel.owner != fromId) {
            LogCat.e("ChannelKick from non-owner $fromId (owner=${channel.owner}) — rejected")
            return
        }
        channel.status = DChatChannel.STATUS_KICKED
        channel.members = channel.members.filter { it.id != TempData.clientId }
        dao.update(channel)
        runBlocking(Dispatchers.IO) {
            ChatCacheManager.loadKeyCacheAsync()
        }
        sendEvent(ChannelUpdatedEvent())
        LogCat.d("Kicked from channel ${msg.channelId} by $fromId")
    }
    private fun handleLeave(fromId: String, msg: ChannelSystemMessages.ChannelLeave) {
        val dao = AppDatabase.instance.chatChannelDao()
        val channel = dao.getById(msg.channelId) ?: return
        if (channel.owner != "me") {
            LogCat.e("ChannelLeave received but we are not the owner of ${msg.channelId}")
            return
        }
        channel.members = channel.members.filter { it.id != fromId }
        channel.version++
        channel.updatedAt = TimeHelper.now()
        dao.update(channel)
        runBlocking(Dispatchers.IO) {
            ChannelSystemMessageSender.broadcastUpdate(channel)
        }
        sendEvent(ChannelUpdatedEvent())
        LogCat.d("Peer $fromId left channel ${msg.channelId}")
    }
}
