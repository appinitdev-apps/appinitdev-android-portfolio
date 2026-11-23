class ErrorViewModel(application: Application, private val error: String) : AndroidViewModel(application) {
    private val message: Pattern = Pattern.compile("\\s[\\w\\s]+\\n", Pattern.MULTILINE)
    private val tags = Pattern.compile(""   +
                                               "\\(\\w+\\.+\\S+\\)",
                                       Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)
    private val spanned: MutableLiveData<Spanned> by lazy {
        MutableLiveData<Spanned>().also {
            formatSpanned()
        }
    }
    fun getSpanned(): LiveData<Spanned> {
        return spanned
    }
    private fun formatSpanned() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val content = SpannableString(error)
                val matcher: Matcher = tags.matcher(error)
                while (matcher.find()) {
                    content.setSpan(ForegroundColorSpan(com.appinitdev.musicpro.theme.managers.ThemeManager.accent.primaryAccentColor), matcher.start(),
                                    matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                matcher.usePattern(message)
                while (matcher.find()) {
                    content.setSpan(ForegroundColorSpan(Color.parseColor("#2980B9")),
                                    matcher.start(), matcher.end(),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                spanned.postValue(content)
            }.onFailure {
                spanned.postValue(error.toSpanned())
            }
        }
    }
}
