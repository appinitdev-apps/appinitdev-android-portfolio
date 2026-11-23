
fun Locale.getElegantDisplayName(): String {
    return when (language) {
        "zh" if country == "TW" -> "繁體中文"
        "zh" if country == "CN" -> "简体中文"
        else -> getDisplayName(this)
    }
}