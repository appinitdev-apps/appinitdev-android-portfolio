
fun Long.getFormattedDuration(): String {
    return (this / 1000L).toInt().getFormattedDuration()
}
