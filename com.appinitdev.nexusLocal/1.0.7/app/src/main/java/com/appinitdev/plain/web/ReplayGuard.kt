
 




object ReplayGuard {
    private const val MAX_TIMESTAMP_DIFF_MS = 30_000L 
    private const val CLEANUP_INTERVAL_MS = 60_000L

    
    private val nonceCache = ConcurrentHashMap<String, MutableSet<String>>()
    private var lastCleanupMs = System.currentTimeMillis()

    data class ParsedRequest(
        val timestamp: Long,
        val nonce: String,
        val body: String,
    )

     


    fun parse(decrypted: String): ParsedRequest? {
        val parts = decrypted.split("|", limit = 3)
        if (parts.size != 3) return null
        val ts = parts[0].toLongOrNull() ?: return null
        return ParsedRequest(ts, parts[1], parts[2])
    }

     


    fun validate(clientId: String, req: ParsedRequest): String? {
        val now = System.currentTimeMillis()
        val diff = kotlin.math.abs(now - req.timestamp)
        if (diff > MAX_TIMESTAMP_DIFF_MS) {
            LogCat.e("Replay guard: timestamp too far off (${diff}ms) from client $clientId")
            return "timestamp_expired"
        }
        val nonceKey = ""
        val nonces = nonceCache.getOrPut(clientId) {
            ConcurrentHashMap.newKeySet()
        }
        if (!nonces.add(nonceKey)) {
            LogCat.e("Replay guard: duplicate nonce from client $clientId")
            return "duplicate_nonce"
        }
        cleanupIfNeeded(now)
        return null
    }

    fun removeClient(clientId: String) {
        nonceCache.remove(clientId)
    }

    private fun cleanupIfNeeded(now: Long) {
        if (now - lastCleanupMs < CLEANUP_INTERVAL_MS) return
        lastCleanupMs = now
        
        
        nonceCache.forEach { (_, nonces) ->
            nonces.removeIf { key ->
                val ts = key.substringBefore(':').toLongOrNull() ?: 0L
                now - ts > MAX_TIMESTAMP_DIFF_MS * 2
            }
        }
        nonceCache.entries.removeIf { it.value.isEmpty() }
    }
}
