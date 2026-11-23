class TextFileViewModel : ViewModel() {
    val isDataLoading = mutableStateOf(true)
    val isEditorReady = mutableStateOf(false)
    val wrapContent = mutableStateOf(true)
    val readOnly = mutableStateOf(true)
    val showMoreActions = mutableStateOf(false)
    val file = mutableStateOf<DFile?>(null)
    val webView = mutableStateOf<WebView?>(null)
    val content = mutableStateOf("")
    val oldContent = mutableStateOf<String?>(null)
    val isExternalFile = mutableStateOf(false)
    suspend fun loadConfigAsync(context: Context) {
        wrapContent.value = EditorWrapContentPreference.getAsync()
    }
    fun loadFileAsync(context: Context, path: String, mediaId: String) {
        try {
            if (mediaId.isNotEmpty()) {
                file.value = FileMediaStoreHelper.getByIdAsync(context, mediaId)
            }
            isExternalFile.value = path.startsWith("content://")
            content.value = if (path.startsWith("content://")) {
                val uri = Uri.parse(path)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().readText()
                } ?: ""
            } else {
                File(path).readText()
            }
        } catch (e: Exception) {
            DialogHelper.showErrorDialog(e.toString())
            LogCat.e(e.toString())
            e.printStackTrace()
        }
    }
    fun toggleWrapContent(context: Context) {
        wrapContent.value = !wrapContent.value
        viewModelScope.launch(Dispatchers.IO) {
            EditorWrapContentPreference.putAsync(wrapContent.value)
        }
        webView.value?.evaluateJavascript("editor.session.setUseWrapMode(${wrapContent.value.toJsValue()})") {}
    }
    fun gotoTop() {
        webView.value?.evaluateJavascript("editor.gotoLine(1)") {}
    }
    fun gotoEnd() {
        webView.value?.evaluateJavascript("editor.gotoLine(editor.session.getLength())") {}
    }
    fun enterEditMode() {
        readOnly.value = false
        oldContent.value = content.value
        webView.value?.evaluateJavascript("editor.setReadOnly(false)") {}
    }
    fun exitEditMode() {
        readOnly.value = true
        if (oldContent.value != null) {
            content.value = oldContent.value!!
            oldContent.value = null
            val json = JSONObject()
            json.put("content", content.value)
            webView.value?.evaluateJavascript("updateContent($json)") {}
        }
        webView.value?.evaluateJavascript("editor.setReadOnly(true)") {}
    }
}
