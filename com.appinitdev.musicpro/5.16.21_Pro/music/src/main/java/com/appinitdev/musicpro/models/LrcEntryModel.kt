data class LrcEntryModel(
        var timestampMs: Long,
        var text: String,
        val id: Long = ID_COUNTER.getAndIncrement()
) {
    companion object {
        private val ID_COUNTER = AtomicLong(System.currentTimeMillis())
    }
}
