data class Peer(
    val id: String,
    val name: String,
    val ip: String,
    val status: String,
    val port: Int,
    val deviceType: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val online: Boolean,
)
fun DPeer.toModel(): Peer {
    return Peer(id, name, getBestIp(), status, port, deviceType, createdAt, updatedAt, PeerStatusManager.isOnline(id))
}
