@Parcelize
enum class HttpServerState: Parcelable {
    OFF,
    ON,
    STARTING,
    STOPPING,
    ERROR;
    fun isProcessing(): Boolean {
        return setOf(STARTING, STOPPING).contains(this)
    }
}