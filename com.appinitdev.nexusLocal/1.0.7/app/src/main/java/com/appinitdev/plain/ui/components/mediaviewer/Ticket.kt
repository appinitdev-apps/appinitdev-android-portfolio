class Ticket {
    private var ticketKey by mutableStateOf("")
    private val ticketMap = mutableMapOf<String, Continuation<Unit>>()
    suspend fun awaitNextTicket() = suspendCoroutine { c ->
        ticketKey = UUID.randomUUID().toString()
        ticketMap[ticketKey] = c
    }
    private fun clearTicket() {
        ticketMap.forEach {
            it.value.resume(Unit)
        }
        ticketMap.clear()
    }
    @Composable
    fun Next() {
        LaunchedEffect(ticketKey) {
            clearTicket()
        }
    }
}