suspend inline fun <T> runHandling(block: () -> T): T? {
    return try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        ToastManager.showErrorToast(e.message ?: "Operation failed")
        null
    }
}
