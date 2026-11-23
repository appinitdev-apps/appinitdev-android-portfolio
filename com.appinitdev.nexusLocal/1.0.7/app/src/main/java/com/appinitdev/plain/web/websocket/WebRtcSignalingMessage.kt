@Serializable
data class WebRtcSignalingMessage(
    val type: String,
    val sdp: String? = null,
    val sdpMid: String? = null,
    val sdpMLineIndex: Int? = null,
    val candidate: String? = null,
    val phoneIp: String? = null,
)
