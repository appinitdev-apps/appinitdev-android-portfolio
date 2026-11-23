suspend fun DChatChannel.getPeersAsync(): List<DPeer> {
    val ids = memberIds()
    val dbPeers = AppDatabase.instance.peerDao().getByIds(ids).associateBy { it.id }
    return ids.mapNotNull { peerId ->
        if (peerId == TempData.clientId) {
            DPeer(
                id = peerId,
                name = TempData.deviceName.value,
                ip = NetworkHelper.getDeviceIP4s().joinToString(","),
                port = TempData.httpsPort.value,
                publicKey = SignatureHelper.getRawPublicKeyBase64Async(),
                deviceType = DeviceType.PHONE.value,
            )
        } else {
            dbPeers[peerId]
        }
    }
}
fun DChatChannel.isOwnedByMe(): Boolean = owner == "me"
fun DChatChannel.isJoined(): Boolean = status == DChatChannel.STATUS_JOINED
fun DChatChannel.canDeleteFromThisDevice(): Boolean =
    isOwnedByMe() || status == DChatChannel.STATUS_LEFT || status == DChatChannel.STATUS_KICKED
fun DChatChannel.canLeave(): Boolean = !isOwnedByMe() && isJoined()
