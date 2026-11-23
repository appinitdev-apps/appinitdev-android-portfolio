
class AppLifecycleObserver(
    private val viewModel: ChatViewModel
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        Firebase.auth.currentUser?.uid?.let { uid ->
            viewModel.updateStatus(true, uid)
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        Firebase.auth.currentUser?.uid?.let { uid ->
            viewModel.updateStatus(false, uid)
        }
    }
}
