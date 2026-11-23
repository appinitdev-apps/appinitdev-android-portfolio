class AppLifecycleTracker(
    private val viewModel: ChatViewModel,
    private val handler: Handler
) : DefaultLifecycleObserver {
    private var statusRunnable: Runnable? = null
    override fun onStart(owner: LifecycleOwner) {
        statusRunnable?.let { handler.removeCallbacks(it) }
    }
    override fun onStop(owner: LifecycleOwner) {
    }
}
