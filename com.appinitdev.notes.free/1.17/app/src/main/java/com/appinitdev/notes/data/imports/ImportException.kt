class ImportException(val textResId: Int, cause: Throwable) : RuntimeException(cause)
