
@Serializable
data class DPairingRequest(
    val fromId: String,
    val fromName: String,
    val port: Int,
    val deviceType: DeviceType,
    val ecdhPublicKey: String, 
    val signaturePublicKey: String, 
    val timestamp: Long, 
    val ips: List<String> = emptyList(), 
    var signature: String = "", 
    var fromIp: String = ""
) {
    fun toSignatureData(): String {
        return "$fromId|$fromName|$port|${deviceType.value}|$ecdhPublicKey|$signaturePublicKey|$timestamp|${ips.joinToString(",")}"
    }
}

@Serializable
data class DPairingResult(
    val deviceId: String,
    val error: String = "",
)

@Serializable
data class DPairingResponse(
    val fromId: String,
    val toId: String,
    val port: Int,
    val deviceType: DeviceType,
    val ecdhPublicKey: String, 
    val signaturePublicKey: String, 
    val accepted: Boolean,
    val timestamp: Long, 
    val ips: List<String> = emptyList(), 
    var signature: String = ""
) {
    fun toSignatureData(): String {
        return "$fromId|$toId|$port|${deviceType.value}|$ecdhPublicKey|$signaturePublicKey|$accepted|$timestamp|${ips.joinToString(",")}"
    }
}

@Serializable
data class DPairingCancel(
    val fromId: String,
    val toId: String,
)

data class DPairingSession(
    val deviceId: String,
    val deviceName: String,
    val deviceIp: String,
    val keyPair: java.security.KeyPair,
    val timestamp: Instant = TimeHelper.now()
)