data class WeatherDestination(
    val id: String,
    val cityEs: String,
    val cityEn: String,
    var temp: String,
    var icon: String,
    var statusEs: String,
    var statusEn: String,
    val tipEs: String,
    val tipEn: String,
    val lat: Double,
    val lon: Double
) {
    fun getCityName(): String {
        val lang = Locale.getDefault().language
        return if (lang.equals("en", ignoreCase = true)) cityEn else cityEs
    }
    fun getStatus(): String {
        val lang = Locale.getDefault().language
        return if (lang.equals("en", ignoreCase = true)) statusEn else statusEs
    }
    fun getTip(): String {
        val lang = Locale.getDefault().language
        return if (lang.equals("en", ignoreCase = true)) tipEn else tipEs
    }
}
