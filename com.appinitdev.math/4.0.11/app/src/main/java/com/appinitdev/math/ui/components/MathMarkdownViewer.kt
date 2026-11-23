@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MathMarkdownViewer(
    markdownContent: String,
    modifier: Modifier = Modifier,
    filterTab: DetailTab = DetailTab.ALL
) {
    val isDarkTheme = isSystemInDarkTheme()
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isPageLoaded by remember { mutableStateOf(false) }
    fun sendData(webView: WebView?) {
        if (webView == null || markdownContent.isBlank()) return
        val mode = when (filterTab) {
            DetailTab.ALL -> "all"
            DetailTab.THEORY -> "theory"
            DetailTab.EXAMPLES -> "examples"
            DetailTab.FORMULAS -> "formulas"
            DetailTab.CALCULATOR -> "all"
        }
        val jsonContent = JSONObject.quote(markdownContent)
        val jsCode = "loadContent($jsonContent, '$mode', $isDarkTheme);"
        webView.post {
            webView.evaluateJavascript(jsCode, null)
        }
    }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(Color.TRANSPARENT)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        isPageLoaded = true
                        sendData(view)
                    }
                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        val url = request?.url?.toString() ?: ""
                        val urlLower = url.lowercase()
                        if (urlLower.endsWith(".svg") || urlLower.endsWith(".png") || urlLower.endsWith(".jpg") || urlLower.contains("svg/")) {
                            try {
                                val fileName = url.substringAfterLast("/")
                                val stream = openImageAsset(context, fileName)
                                if (stream != null) {
                                    val mimeType = when {
                                        urlLower.endsWith(".svg") -> "image/svg+xml"
                                        urlLower.endsWith(".png") -> "image/png"
                                        else -> "image/jpeg"
                                    }
                                    return WebResourceResponse(mimeType, "UTF-8", stream)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        return super.shouldInterceptRequest(view, request)
                    }
                }
                loadUrl("file:///android_asset/katex/template.html")
                webViewRef = this
            }
        },
        update = { webView ->
            webViewRef = webView
            if (isPageLoaded) {
                sendData(webView)
            }
        }
    )
    LaunchedEffect(markdownContent, filterTab, isDarkTheme) {
        if (isPageLoaded && webViewRef != null) {
            sendData(webViewRef)
        }
    }
}
private fun openImageAsset(context: android.content.Context, rawFileName: String): InputStream? {
    val fileName = rawFileName.substringAfterLast("/")
    return try {
        context.assets.open("Data/shared_svg/$fileName")
    } catch (_: Exception) {
        null
    }
}
