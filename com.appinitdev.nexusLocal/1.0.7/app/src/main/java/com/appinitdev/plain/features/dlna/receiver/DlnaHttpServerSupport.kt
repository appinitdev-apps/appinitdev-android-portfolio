internal fun resolveSenderName(headers: Map<String, String>, senderIp: String): String {
    return headers["c-name"]?.takeIf { it.isNotBlank() } ?: senderIp
}
internal fun BufferedInputStream.readHttpLine(): String? {
    val sb = StringBuilder()
    var prev = -1
    while (true) {
        val b = read()
        if (b == -1) return if (sb.isEmpty()) null else sb.toString()
        if (prev == '\r'.code && b == '\n'.code) {
            sb.deleteCharAt(sb.length - 1) 
            return sb.toString()
        }
        sb.append(b.toChar())
        prev = b
    }
}
internal fun readBodyBytes(bis: BufferedInputStream, contentLength: Int): String {
    if (contentLength <= 0) return ""
    val buf = ByteArray(contentLength)
    var offset = 0
    while (offset < contentLength) {
        val read = bis.read(buf, offset, contentLength - offset)
        if (read == -1) break
        offset += read
    }
    return String(buf, 0, offset, Charsets.UTF_8)
}
internal fun parseDlnaTimeToMs(time: String): Long {
    val parts = time.split(":")
    return if (parts.size >= 3) {
        val h = parts[0].toLongOrNull() ?: return -1L
        val m = parts[1].toLongOrNull() ?: return -1L
        val s = parts[2].split(".")[0].toLongOrNull() ?: return -1L
        (h * 3600 + m * 60 + s) * 1000
    } else -1L
}
internal fun httpOk(body: String, contentType: String = "text/plain"): String {
    val bytes = body.toByteArray(Charsets.UTF_8)
    return "HTTP/1.1 200 OK\r\nContent-Type: $contentType\r\nContent-Length: ${bytes.size}\r\nConnection: close\r\n\r\n$body"
}
internal fun httpOkSubscribe(): String {
    return "HTTP/1.1 200 OK\r\nSID: uuid:dlna-plain-sub\r\nTIMEOUT: Second-3600\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
}
internal fun httpNotFound(): String {
    return "HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
}
internal fun httpInternalError(): String {
    return "HTTP/1.1 500 Internal Server Error\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
}