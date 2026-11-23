class ImageMediaObserver(
    private val onChanged: () -> Unit,
) : ContentObserver(Handler(Looper.getMainLooper())) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var pendingJob: Job? = null
    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        pendingJob?.cancel()
        pendingJob = scope.launch {
            delay(DEBOUNCE_MS)
            onChanged()
        }
    }
    companion object {
        private const val DEBOUNCE_MS = 1500L
    }
}
