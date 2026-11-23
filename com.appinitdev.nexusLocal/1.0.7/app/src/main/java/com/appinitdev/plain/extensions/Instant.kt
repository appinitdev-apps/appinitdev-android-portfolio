

fun Instant.formatTime(): String {
    val c = Calendar.getInstance()
    c.timeInMillis = epochSeconds * 1000
    return android.text.format.DateFormat.getTimeFormat(MainApp.instance)
        .format(c.time)
}

fun Instant.formatDateTime(): String {
    val c = Calendar.getInstance()
    c.timeInMillis = epochSeconds * 1000
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, LocaleHelper.currentLocale())
        .format(c.time)
}

fun Instant.timeAgo(): String {
    return TimeAgoHelper.getString(toEpochMilliseconds())
}

fun Instant.formatDate(): String {
    val c = Calendar.getInstance()
    c.timeInMillis = epochSeconds * 1000
    return DateFormat.getDateInstance(DateFormat.MEDIUM, LocaleHelper.currentLocale())
        .format(c.time)
}