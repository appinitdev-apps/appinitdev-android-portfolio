
@Serializable
data class EditorData(val language: String, val wrapContent: Boolean, val isDarkTheme: Boolean, val readOnly: Boolean, val gotoEnd: Boolean, val content: String)

class EditorWebViewClient(
    val context: Context,
    val data: EditorData,
) : WebViewClient() {
    override fun onPageFinished(view: WebView, url: String?) {
        coMain {
            val json = withIO { jsonEncode(data) }
            view.evaluateJavascript("loadEditor(${json})") {}
        }
    }
}