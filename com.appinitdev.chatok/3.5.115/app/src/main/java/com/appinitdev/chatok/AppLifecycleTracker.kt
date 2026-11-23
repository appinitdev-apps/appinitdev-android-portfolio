
class AppLifecycleTracker(
    private val viewModel: ChatViewModel,
    private val handler: Handler
) : DefaultLifecycleObserver {

    private var statusRunnable: Runnable? = null

    override fun onStart(owner: LifecycleOwner) {
        // Si la app regresa a primer plano, se cancela el cambio a offline
        statusRunnable?.let { handler.removeCallbacks(it) }
      //  viewModel.actualizarEstadoYStatusUsuario(true)
    }

    override fun onStop(owner: LifecycleOwner) {
        // Espera 10 segundos antes de actualizar a offline
        //statusRunnable = Runnable { viewModel.actualizarEstadoYStatusUsuario(false) }
      //  handler.postDelayed(statusRunnable!!, 10_000) // 10 segundos
    }
}
