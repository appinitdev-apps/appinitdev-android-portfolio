
@Composable
fun AceEditor(
    textFileVM: TextFileViewModel,
    scope: CoroutineScope,
    data: EditorData,
) {
    DisposableEffect(Unit) {
        onDispose {
            textFileVM.webView.value?.destroy()
            textFileVM.webView.value = null
        }
    }

    AndroidView(factory = {
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        val webView = WebView(it)
        webView.apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.TRANSPARENT)
            settings.javaScriptEnabled = true
            webViewClient = EditorWebViewClient(
                context,
                data
            )
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            addJavascriptInterface(
                EditorWebViewInterface(
                    ready = {
                        textFileVM.isEditorReady.value = true
                    },
                    update = { c ->
                        textFileVM.content.value = c
                    }), "AndroidApp"
            )
            settings.domStorageEnabled = true
            loadUrl("file:///android_asset/editor/index.html")
            scope.launch(Dispatchers.IO) {
                Language.initLocaleAsync(context)
            }
        }
        textFileVM.webView.value = webView
        webView
    })
}