data class DNearbyDevice(
    val id: String,
    val name: String,
    val ips: List<String> = emptyList(), 
    val port: Int,
    val deviceType: DeviceType,
    val version: String,
    val platform: String,
    val lastSeen: Instant
) {
    fun getBestIp(): String {
        return NetworkHelper.getBestIp(ips)
    }
}
