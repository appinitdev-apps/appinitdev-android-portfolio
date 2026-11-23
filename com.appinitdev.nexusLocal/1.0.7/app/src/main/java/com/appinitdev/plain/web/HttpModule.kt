object HttpModule {
    @SuppressLint("SuspiciousIndentation")
    val module: Application.() -> Unit = {
        install(CachingHeaders) {
            options { _, outgoingContent ->
                when (outgoingContent.contentType?.withoutParameters()) {
                    ContentType.Text.CSS, ContentType.Application.JavaScript ->
                        CachingOptions(
                            CacheControl.MaxAge(maxAgeSeconds = 3600 * 24 * 30),
                        )
                    else -> null
                }
            }
        }
        install(CORS) {
            if (BuildConfig.DEBUG) {
                allowHost("*")
            } else {
                allowHost("localhost:3000")
                allowHost("127.0.0.1:3000")
            }
            allowHeadersPrefixed("c-")
        }
        install(ConditionalHeaders)
        install(WebSockets)
        install(ForwardedHeaders)
        install(PartialContent)
        install(AutoHeadResponse)
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                },
            )
        }
        intercept(ApplicationCallPipeline.Plugins) {
            if (call.request.path() == "/health") {
                return@intercept
            }
            if (!TempData.webEnabled.value) {
                call.respond(HttpStatusCode.NotFound)
                return@intercept finish()
            }
            call.response.headers.append("X-Server-Time", System.currentTimeMillis().toString())
        }
        routing {
            staticResources("/", "web", index = null) {
                cacheControl {
                    arrayListOf(
                        CacheControl.NoCache(CacheControl.Visibility.Public),
                        CacheControl.NoStore(CacheControl.Visibility.Public),
                    )
                }
                fallback { requestedPath, call ->
                    if (requestedPath.contains('.')) {
                        call.respond(HttpStatusCode.NotFound)
                    } else {
                        val classLoader = call.application.environment.classLoader
                        val html = classLoader.getResourceAsStream("web/index.html")
                            ?.bufferedReader()?.readText() ?: ""
                        val injected = html.replace(
                            "<head>",
                            "<head><script>window.__SERVER_TIME__=${System.currentTimeMillis()}</script>"
                        )
                        call.respondText(injected, ContentType.Text.Html)
                    }
                }
            }
            post("/api/wake-app") {
                val action = call.request.queryParameters["action"]
                HttpServerManager.handleWakeAppRequest(action)
                call.respond(HttpStatusCode.OK, "Dispositivo despertado")
            }
            get("/health") {
                call.respond(HttpStatusCode.OK, BuildConfig.APPLICATION_ID)
            }
            get("/shutdown") {
                val ip = call.request.origin.remoteHost
                LogCat.d("$ip is shutting down the server")
                if (ip != "localhost") {
                    call.respond(HttpStatusCode.Forbidden)
                    return@get
                }
                HttpServerManager.wsSessions.forEach {
                    it.session.close()
                }
                HttpServerManager.wsSessions.clear()
                HttpServerManager.wsSessionCount.value = 0
                val latch = CompletableDeferred<Nothing>()
                val application = call.application
                val environment = application.environment
                application.launch {
                    latch.join()
                    application.monitor.raise(ApplicationStopPreparing, environment)
                    application.dispose()
                }
                try {
                    call.respond(HttpStatusCode.Gone)
                } finally {
                    latch.cancel()
                }
            }
            addDLNA()
            addZip()
            addFiles()
            addUploads()
            post("/init") {
                val clientId = call.request.headers["c-id"] ?: ""
                if (clientId.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "`c-id` is missing in the headers")
                    return@post
                }
                if (!TempData.webEnabled.value) {
                    call.respond(HttpStatusCode.Forbidden, "web_access_disabled")
                    return@post
                }
                HttpServerManager.clientIpCache[clientId] = call.request.origin.remoteAddress
                val bodyBytes = runCatching { call.receive<ByteArray>() }.getOrNull()
                if (bodyBytes != null && bodyBytes.isNotEmpty()) {
                    val token = HttpServerManager.tokenCache[clientId]
                    if (token != null) {
                        val decryptedBytes = CryptoHelper.chaCha20Decrypt(token, bodyBytes)
                        if (decryptedBytes != null) {
                            call.respond(HttpStatusCode.OK)
                            return@post
                        }
                    }
                }
                if (PasswordTypePreference.getValueAsync() == PasswordType.NONE) {
                    call.respondText(HttpServerManager.resetPasswordAsync())
                } else {
                    call.respond(HttpStatusCode.NoContent)
                }
            }
            addWebSocket()
        }
        install(MainGraphQL) {
            init()
        }
        install(PeerGraphQL) {
            init()
        }
    }
}
