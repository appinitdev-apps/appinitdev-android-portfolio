object HttpClientManager {
    fun browserClient() =
        HttpClient(CIO) {
            BrowserUserAgent()
            install(Logging) {
                logger =
                    object : Logger {
                        override fun log(message: String) {
                            LogCat.v(message)
                        }
                    }
                level = LogLevel.HEADERS
            }
            install(HttpCookies)
            install(HttpTimeout) {
                requestTimeoutMillis = HttpApiTimeout.BROWSER_SECONDS * 1000L
            }
            headers {
                set("accept", "*/*")
            }
        }
    fun downloadClient(): OkHttpClient =
        OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_1_1))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()
    fun httpClient() =
        HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = HttpApiTimeout.DEFAULT_SECONDS * 1000L
                connectTimeoutMillis = HttpApiTimeout.DEFAULT_SECONDS * 1000L
            }
            install(WebSockets)
        }
    private fun OkHttpClient.Builder.ignoreAllSSLErrors(): OkHttpClient.Builder {
        val naiveTrustManager =
            object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                override fun checkClientTrusted(
                    certs: Array<X509Certificate>,
                    authType: String,
                ) = Unit
                override fun checkServerTrusted(
                    certs: Array<X509Certificate>,
                    authType: String,
                ) = Unit
            }
        val insecureSocketFactory =
            SSLContext.getInstance("TLSv1.2").apply {
                val trustAllCerts = arrayOf<TrustManager>(naiveTrustManager)
                init(arrayOf<KeyManager>(), trustAllCerts, SecureRandom())
            }.socketFactory
        sslSocketFactory(insecureSocketFactory, naiveTrustManager)
        hostnameVerifier { _, _ -> true }
        return this
    }
    fun createCryptoHttpClient(
        token: String,
        timeout: Int,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                val requestBody = request.body!!
                val requestBodyStr = bodyToString(requestBody)
                LogCat.d("[Request] $requestBodyStr")
                val response =
                    chain.proceed(
                        request.newBuilder()
                            .addHeader("c-id", TempData.clientId)
                            .addHeader("c-platform", "android")
                            .addHeader(
                                "c-name",
                                Base64.encodeToString(PhoneHelper.getDeviceName(MainApp.instance).toByteArray(), Base64.NO_WRAP),
                            )
                            .addHeader("c-version", MainApp.getAppVersion())
                            .post(CryptoHelper.chaCha20Encrypt(token, requestBodyStr).toRequestBody(requestBody.contentType()))
                            .build(),
                    )
                val responseBody = response.body
                val decryptedBytes = CryptoHelper.chaCha20Decrypt(token, responseBody.bytes())
                if (decryptedBytes != null) {
                    val json = decryptedBytes.decodeToString()
                    LogCat.d("[Response] $json")
                    return@addInterceptor response.newBuilder().body(json.toResponseBody(responseBody.contentType())).build()
                }
                response.newBuilder().build()
            }
            .connectTimeout(500, TimeUnit.MILLISECONDS)
            .writeTimeout(timeout.toLong(), TimeUnit.SECONDS)
            .readTimeout(timeout.toLong(), TimeUnit.SECONDS)
            .ignoreAllSSLErrors()
            .build()
    }
    fun createUnsafeOkHttpClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(arrayOf<KeyManager>(), trustAllCerts, SecureRandom())
        val sslSocketFactory = sslContext.socketFactory
        return OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { hostname, _ ->
                NetworkHelper.isLocalNetworkAddress(hostname)
            }
            .build()
    }
    private fun bodyToString(request: RequestBody): String {
        val buffer = okio.Buffer()
        request.writeTo(buffer)
        return buffer.readUtf8()
    }
}
