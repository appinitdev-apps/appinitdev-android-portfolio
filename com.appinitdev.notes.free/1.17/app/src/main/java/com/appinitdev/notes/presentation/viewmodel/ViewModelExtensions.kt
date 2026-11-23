fun ViewModel.executeAsyncWithCallback(
    function: suspend () -> Unit,
    callback: (success: Boolean) -> Unit,
) {
    viewModelScope.launch {
        val success =
            try {
                withContext(Dispatchers.IO) { function() }
                true
            } catch (exception: SQLiteConstraintException) {
                false
            }
        callback(success)
    }
}
