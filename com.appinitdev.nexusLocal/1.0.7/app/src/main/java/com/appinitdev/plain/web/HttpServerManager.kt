object HttpServerManager {
    private const val SSL_KEY_ALIAS = Constants.SSL_NAME
    val tokenCache = mutableMapOf<String, ByteArray>() 
    val clientIpCache = mutableMapOf<String, String>() 
    val wsSessions = Collections.synchronizedSet<WebSocketSession>(LinkedHashSet())
    val wsSessionCount = kotlinx.coroutines.flow.MutableStateFlow(0)
    val clientRequestTs = mutableMapOf<String, Long>()
    var httpServerError: String = ""
    val portsInUse = mutableSetOf<Int>()
    val httpsPorts = setOf(8043, 8143, 8243, 8343, 8443, 8543, 8643, 8743, 8843, 8943)
    val httpPorts = setOf(8080, 8180, 8280, 8380, 8480, 8580, 8680, 8780, 8880, 8980)
    @Volatile
    var server: EmbeddedServer<*, *>? = null
    private const val LOGIN_RATE_LIMIT_WINDOW_MS = 60_000L
    private const val LOGIN_RATE_LIMIT_MAX_ATTEMPTS = 5
    private data class RateLimitWindow(var startMs: Long, var count: Int)
    private val loginAttemptsByKey = ConcurrentHashMap<String, RateLimitWindow>()
    private var clientTsJob: Job? = null
    fun tryAcquireLoginAttempt(key: String, nowMs: Long = System.currentTimeMillis()): Boolean {
        if (key.isBlank()) return true
        val window = loginAttemptsByKey.compute(key) { _, existing ->
            if (existing == null || nowMs - existing.startMs >= LOGIN_RATE_LIMIT_WINDOW_MS) {
                RateLimitWindow(nowMs, 1)
            } else {
                existing.count += 1
                existing
            }
        } ?: return true
        if (loginAttemptsByKey.size > 5_000) {
            val threshold = nowMs - (LOGIN_RATE_LIMIT_WINDOW_MS * 2)
            loginAttemptsByKey.entries.removeIf { it.value.startMs < threshold }
        }
        return window.count <= LOGIN_RATE_LIMIT_MAX_ATTEMPTS
    }
    fun getClientIpForLogin(clientId: String, remoteAddress: String): String {
        val cached = clientIpCache[clientId]
        if (!cached.isNullOrEmpty()) return cached
        if (remoteAddress.isNotEmpty()) {
            clientIpCache[clientId] = remoteAddress
        }
        return remoteAddress
    }
    val notificationId: Int by lazy {
        NotificationHelper.generateId()
    }
    suspend fun resetPasswordAsync(): String {
        val password = CryptoHelper.randomPassword(6)
        PasswordPreference.putAsync(password)
        return password
    }
    fun getNotificationContent(): String {
        val ip = TempData.mdnsHostname
        return "http://$ip:${TempData.httpPort.value}\nhttps://$ip:${TempData.httpsPort.value}"
    }
    fun handleWakeAppRequest(actionFromPc: String?) {
        val context: Context = MainApp.instance
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("EXTRA_PC_ACTION", actionFromPc ?: "OPEN_APP")
            }
            context.startActivity(intent)
            LogCat.d("MainActivity despertada con éxito desde PC. Acción: $actionFromPc")
        } catch (e: Exception) {
            LogCat.e("Error al intentar despertar la app desde el servidor local: ${e.message}")
        }
    }
    suspend fun stopServiceAsync(context: Context) {
        sendEvent(HttpServerStateChangedEvent(HttpServerState.STOPPING))
        try {
            val client = HttpClientManager.httpClient()
            client.get(UrlHelper.getShutdownUrl())
        } catch (_: Exception) {}
        try { server?.stop(0, 1000) } catch (_: Exception) {}
        context.stopService(Intent(context, HttpServerService::class.java))
        PNotificationListenerService.toggle(context, false)
        httpServerError = ""
        portsInUse.clear()
        server = null
        sendEvent(HttpServerStateChangedEvent(HttpServerState.OFF))
    }
    fun stopPreviousServer() {
        try {
            server?.stop(0, 500)
            LogCat.d("Previous server instance stopped")
        } catch (e: Exception) {
            LogCat.e("Error stopping previous server: ${e.message}")
        } finally {
            server = null
        }
    }
    suspend fun waitForPortsAvailable(httpPort: Int, httpsPort: Int, maxWaitMs: Long = 3000): Boolean {
        val interval = 200L
        var elapsed = 0L
        while (elapsed < maxWaitMs) {
            val httpFree = !PortHelper.isPortInUse(httpPort)
            val httpsFree = !PortHelper.isPortInUse(httpsPort)
            if (httpFree && httpsFree) {
                LogCat.d("Ports $httpPort and $httpsPort are free after ${elapsed}ms")
                return true
            }
            delay(interval)
            elapsed += interval
        }
        LogCat.e("Ports still in use after ${maxWaitMs}ms - http:${PortHelper.isPortInUse(httpPort)}, https:${PortHelper.isPortInUse(httpsPort)}")
        return false
    }
    fun warmUp() {
        coIO {
            try {
                val s = embeddedServer(Netty, port = 0) {}
                s.start(wait = false)
                s.stop(0, 0)
                LogCat.d("Netty warm-up complete")
            } catch (_: Exception) {}
        }
    }
    suspend fun checkServerAsync(): Boolean {
        return withTimeoutOrNull(9000) {
            val client = HttpClientManager.httpClient()
            val deadline = System.currentTimeMillis() + 8500L
            var healthy = false
            while (!healthy && System.currentTimeMillis() < deadline) {
                try {
                    val response = client.get(UrlHelper.getHealthCheckUrl())
                    if (response.status == HttpStatusCode.OK) {
                        healthy = true
                    }
                } catch (ex: Exception) {
                    delay(300)
                    LogCat.e("HTTP server check failed: ${ex.message}")
                }
            }
            LogCat.d("HTTP server check healthy: $healthy")
            healthy
        } ?: false
    }
    private suspend fun passwordToToken(): ByteArray {
        return hashToToken(CryptoHelper.sha512(PasswordPreference.getAsync().toByteArray()))
    }
    fun hashToToken(hash: String): ByteArray {
        return hash.substring(0, 32).toByteArray()
    }
    suspend fun loadTokenCache() {
        tokenCache.clear()
        SessionList.getItemsAsync().forEach {
            if (it.token.isNotEmpty()) {
                tokenCache[it.clientId] = Base64.decode(it.token, Base64.NO_WRAP)
            }
        }
    }
    fun generateSSLKeyStore(file: File, password: String) {
        val keyStore = JksHelper.genJksFile(SSL_KEY_ALIAS, password, Constants.SSL_NAME)
        val tmp = File(file.parent, "${file.name}.tmp")
        try {
            FileOutputStream(tmp).use {
                keyStore.store(it, password.toCharArray())
            }
            tmp.renameTo(file)
        } catch (ex: Exception) {
            tmp.delete()
            throw ex
        }
    }
    private fun getSSLKeyStore(context: Context, password: String): KeyStore {
        val filesDir = context.filesDir ?: throw IllegalStateException("context.filesDir is null")
        val file = File(filesDir, Constants.KEY_STORE_FILE_NAME)
        if (!file.exists()) {
            generateSSLKeyStore(file, password)
        }
        return KeyStore.getInstance("BKS", BouncyCastleProvider()).apply {
            try {
                file.inputStream().use {
                    load(it, password.toCharArray())
                }
            } catch (ex: Exception) {
                LogCat.e("Failed to load keystore: ${ex.message}, regenerating...")
                ex.printStackTrace()
                if (file.exists()) {
                    file.delete()
                }
                try {
                    generateSSLKeyStore(file, password)
                    file.inputStream().use {
                        load(it, password.toCharArray())
                    }
                } catch (ex2: Exception) {
                    LogCat.e("Failed to regenerate keystore: ${ex2.message}")
                    ex2.printStackTrace()
                    throw ex2
                }
            }
        }
    }
    suspend fun createHttpServerAsync(context: Context): EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration> {
        val password = KeyStorePasswordPreference.getAsync()
        val passwordArray = password.toCharArray()
        val httpPort = TempData.httpPort.value
        val httpsPort = TempData.httpsPort.value
        val environment = applicationEnvironment {
            log = LoggerFactory.getLogger("ktor.application")
        }
        return embeddedServer(Netty, environment, configure = {
            runningLimit = 1000
            tcpKeepAlive = true
            enableHttp2 = false
            connector {
                port = httpPort
            }
            sslConnector(
                keyStore = getSSLKeyStore(context, password),
                keyAlias = SSL_KEY_ALIAS,
                keyStorePassword = { passwordArray },
                privateKeyPassword = { passwordArray },
            ) {
                port = httpsPort
            }
        }, HttpModule.module)
    }
    fun getSSLSignature(context: Context, password: String): ByteArray {
        val keystore = getSSLKeyStore(context, password)
        val cert = keystore.getCertificate(SSL_KEY_ALIAS) as X509Certificate
        return cert.signature
    }
    fun clientTsInterval() {
        if (clientTsJob?.isActive == true) return
        val activeIntervalMs = 5_000L
        val idleIntervalMs = 60_000L
        var lastSyncTs = 0L
        clientTsJob = coIO {
            while (kotlinx.coroutines.currentCoroutineContext().isActive) {
                val updates =
                    clientRequestTs
                        .filter { it.value > lastSyncTs }
                        .map { SessionClientTsUpdate(it.key, Instant.fromEpochMilliseconds(it.value)) }
                if (updates.isNotEmpty()) {
                    val maxTsInThisBatch = updates.maxOf { it.lastActiveAt.toEpochMilliseconds() }
                    runCatching {
                        AppDatabase.instance.sessionDao().updateTs(updates)
                        lastSyncTs = maxTsInThisBatch
                    }.onFailure {
                        LogCat.e("Failed to update client session timestamps: ${it.message}")
                    }
                    delay(activeIntervalMs)
                } else {
                    delay(idleIntervalMs)
                }
            }
        }
    }
    suspend fun respondTokenAsync(
        event: ConfirmToAcceptLoginEvent,
        clientIp: String,
    ) {
        val token = CryptoHelper.generateChaCha20Key()
        val r = event.request
        SessionList.addOrUpdateAsync(event.clientId) {
            it.clientIP = clientIp
            it.osName = r.osName
            it.osVersion = r.osVersion
            it.browserName = r.browserName
            it.browserVersion = r.browserVersion
            it.token = token
        }
        HttpServerManager.loadTokenCache()
        NotificationHelper.sendWebLoginNotification(MainApp.instance, r.browserName, r.browserVersion, r.osName, r.osVersion, clientIp)
        event.session.send(
            CryptoHelper.chaCha20Encrypt(
                HttpServerManager.passwordToToken(),
                JsonHelper.jsonEncode(
                    AuthResponse(
                        TempData.clientId,
                        AuthStatus.COMPLETED,
                        token,
                    ),
                ),
            ),
        )
    }
}
