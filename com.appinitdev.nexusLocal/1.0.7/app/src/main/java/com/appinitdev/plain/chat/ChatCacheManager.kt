object ChatCacheManager {
    val peerKeyCache = mutableMapOf<String, ByteArray>()
    val peerPublicKeyCache = mutableMapOf<String, ByteArray>()
    val channelKeyCache = mutableMapOf<String, ByteArray>()
    val peerMap = mutableMapOf<String, DPeer>()
    var activeToId = ""
    suspend fun loadKeyCacheAsync() {
        peerKeyCache.clear()
        peerPublicKeyCache.clear()
        channelKeyCache.clear()
        val peers = AppDatabase.instance.peerDao().getAllWithPublicKey()
        peers.forEach { peer ->
            if (peer.key.isNotEmpty()) {
                peerKeyCache[peer.id] = Base64.decode(peer.key, Base64.NO_WRAP)
            }
            if (peer.publicKey.isNotEmpty()) {
                peerPublicKeyCache[peer.id] = Base64.decode(peer.publicKey, Base64.NO_WRAP)
            }
        }
        val channels = AppDatabase.instance.chatChannelDao().getAll()
        channels.forEach { channel ->
            if (channel.status == "joined") {
                channelKeyCache[channel.id] = Base64.decode(channel.key, Base64.NO_WRAP)
            }
        }
    }
    fun getKey(type: String, id: String): ByteArray? {
        return when (type) {
            "peer" -> peerKeyCache[id]
            "channel" -> channelKeyCache[id]
            else -> null
        }
    }
    fun refreshPeerMap(peers: List<DPeer>) {
        val cache = mutableMapOf<String, DPeer>()
        peers.forEach { peer ->
            cache[peer.id] = peer
        }
        peerMap.clear()
        peerMap.putAll(cache)
    }
    fun updatePeer(peer: DPeer) {
        peerMap[peer.id] = peer
    }
}