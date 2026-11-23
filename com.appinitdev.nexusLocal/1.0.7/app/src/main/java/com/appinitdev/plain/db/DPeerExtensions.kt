
fun DPeer.getBestIp(): String {
    val ips = getIpList()
    if (ips.isEmpty()) return ip
    return NetworkHelper.getBestIp(ips)
}

fun DPeer.getBaseUrl(): String = "https://${getBestIp()}:$port"

fun DPeer.getApiUrl(): String = "${getBaseUrl()}/peer_graphql"

fun DPeer.getStatusWsUrl(): String = "wss://${getBestIp()}:$port/status"

fun DPeer.getFileUrl(fileId: String): String = "${getBaseUrl()}/fs?id=${fileId.urlEncode()}"
