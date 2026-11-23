
 

















object MdnsHostResponder {
    private const val MDNS_GROUP = "224.0.0.251"
    private const val MDNS_PORT = 5353

    @Volatile private var hostname = "plainapp.local"

    private val stateLock = Any()
    private var socket: MulticastSocket? = null
    private var worker: Thread? = null
    private var multicastLock: WifiManager.MulticastLock? = null

    fun start(context: Context, mdnsHostname: String): Boolean {
        val normalized = normalizeHostname(mdnsHostname)
        if (normalized.isEmpty()) {
            LogCat.e("mDNS start skipped: empty hostname")
            return false
        }
        stop()
        hostname = normalized

        val candidates = candidateInterfaces()
        if (candidates.isEmpty()) {
            LogCat.e("mDNS: no candidate interfaces found")
            return false
        }

        val multicastGroup = InetAddress.getByName(MDNS_GROUP)
        val groupSockAddr = InetSocketAddress(multicastGroup, MDNS_PORT)
        synchronized(stateLock) {
            val lock = acquireMulticastLock(context)
            val s = runCatching {
                MulticastSocket(null).apply {
                    reuseAddress = true
                    soTimeout = 1000
                    
                    
                    
                    
                    
                    bind(InetSocketAddress(InetAddress.getByName("0.0.0.0"), MDNS_PORT))
                    var joinCount = 0
                    for ((iface, ip) in candidates) {
                        runCatching { joinGroup(groupSockAddr, iface) }
                            .onSuccess { joinCount++; LogCat.d("mDNS joined ${iface.name} (${ip.hostAddress})") }
                            .onFailure { LogCat.e("mDNS joinGroup ${iface.name}: ${it.message}") }
                    }
                    
                    
                    if (joinCount == 0) {
                        runCatching { joinGroup(multicastGroup) }
                            .onSuccess { joinCount++; LogCat.d("mDNS joined multicast group (default interface)") }
                            .onFailure { LogCat.e("mDNS joinGroup default fallback failed: ${it.message}") }
                    }
                    if (joinCount == 0) {
                        LogCat.e("mDNS: failed to join multicast group on any interface")
                    }
                }
            }.getOrElse {
                lock?.let { l -> runCatching { l.release() } }
                LogCat.e("mDNS socket create failed: ${it.message}")
                return false
            }
            socket = s
            multicastLock = lock
            worker = Thread { runLoop(s) }.apply {
                name = "plain-mdns-responder"
                isDaemon = true
                start()
            }
        }
        LogCat.d("mDNS responder started for $hostname on ${candidates.size} interface(s)")
        return true
    }

    fun stop() {
        synchronized(stateLock) {
            val t = worker; worker = null
            val s = socket; socket = null
            runCatching { s?.close() }
            runCatching { t?.join(300) }
            multicastLock?.let { ml -> runCatching { if (ml.isHeld) ml.release() } }
            multicastLock = null
        }
    }

    private fun runLoop(s: MulticastSocket) {
        val buf = ByteArray(1500)
        while (!s.isClosed) {
            val packet = DatagramPacket(buf, buf.size)
            try {
                s.receive(packet)
                
                
                val senderIp = extractInet4Address(packet.address) ?: continue
                val fresh = candidateInterfaces()
                if (fresh.isEmpty()) continue
                val (responseIface, localIp) = findResponseIface(senderIp, fresh)
                val response = MdnsPacketCodec.buildResponseIfMatchDetails(
                    query = packet.data.copyOf(packet.length),
                    hostname = hostname,
                    ips = listOf(localIp),
                ) ?: continue
                val useUnicast = response.unicastResponseRequested || packet.port != MDNS_PORT
                val destAddress = if (useUnicast) senderIp else InetAddress.getByName(MDNS_GROUP)
                val destPort = if (useUnicast) packet.port else MDNS_PORT
                val dest = "${destAddress.hostAddress}:$destPort"
                val queryInfo = formatQuestions(response.matchedQuestions)

                
                
                
                runCatching {
                    if (!useUnicast) s.networkInterface = responseIface
                    s.send(DatagramPacket(response.bytes, response.bytes.size, destAddress, destPort))
                    LogCat.d(
                        "mDNS reply $hostname → ${localIp.hostAddress} from " +
                            "${senderIp.hostAddress}:${packet.port} $queryInfo dest=$dest",
                    )
                }.onFailure { LogCat.e("mDNS send to ${senderIp.hostAddress}: ${it.message}") }
            } catch (_: SocketTimeoutException) {
                
            } catch (_: Exception) {
                if (s.isClosed) break
            }
        }
    }

     



    internal fun extractInet4Address(addr: InetAddress): Inet4Address? {
        if (addr is Inet4Address) return addr
        if (addr is Inet6Address) {
            val b = addr.address
            
            if (b.size == 16 && b[0] == 0.toByte() && b[10] == 0xFF.toByte() && b[11] == 0xFF.toByte() &&
                b.take(10).all { it == 0.toByte() }
            ) {
                return runCatching { InetAddress.getByAddress(b.copyOfRange(12, 16)) as? Inet4Address }.getOrNull()
            }
        }
        return null
    }

    private fun formatQuestions(questions: List<MdnsQuestion>): String {
        return questions.joinToString(separator = ",", prefix = "[", postfix = "]") {
            "qtype=${it.qtype} qclass=${it.qclass} QU=${it.unicastResponseRequested}"
        }
    }

    internal fun normalizeHostname(value: String): String {
        val trimmed = value.trim().trim('.').lowercase()
        if (trimmed.isEmpty()) return ""
        return if (trimmed.endsWith(".local")) trimmed else "$trimmed.local"
    }

    private fun acquireMulticastLock(context: Context): WifiManager.MulticastLock? {
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            ?: return null
        return runCatching {
            wifi.createMulticastLock("plain-mdns-lock").apply {
                setReferenceCounted(false)
                acquire()
            }
        }.getOrNull()
    }
}
