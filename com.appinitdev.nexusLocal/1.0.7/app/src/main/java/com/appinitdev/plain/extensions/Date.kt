fun Date.formatDateTime(): String {
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, LocaleHelper.currentLocale()).format(this)
}
fun Date.formatTime(): String {
    val c = Calendar.getInstance()
    c.timeInMillis = time
    return android.text.format.DateFormat.getTimeFormat(MainApp.instance).format(c.time)
}
fun Date.formatName(): String {
    return SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.ENGLISH).format(this)
}
