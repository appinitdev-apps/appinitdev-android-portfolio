@Keep
data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val nameDev: String="",
    val timestamp: Long = System.currentTimeMillis()
)