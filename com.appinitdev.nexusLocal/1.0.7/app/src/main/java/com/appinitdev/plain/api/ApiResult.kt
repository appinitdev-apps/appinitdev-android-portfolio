
data class ApiResult(val response: HttpResponse?, val exception: Throwable? = null) {
    fun isOk(): Boolean {
        return response?.isOk() == true
    }

    fun errorMessage(): String {
        return exception?.toString() ?: response?.toString() ?: LocaleHelper.getString(Res.string.unknown)
    }
}
