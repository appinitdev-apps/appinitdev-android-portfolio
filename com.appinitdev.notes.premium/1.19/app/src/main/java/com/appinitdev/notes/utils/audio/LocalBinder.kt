class LocalBinder<T : Service>(private val service: T) : Binder() {
    fun getService(): T = service
}
