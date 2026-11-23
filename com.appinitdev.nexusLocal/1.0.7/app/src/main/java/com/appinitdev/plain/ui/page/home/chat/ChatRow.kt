data class ChatRow(
    val sortAt: Instant,
    val title: String,
    val desc: String,
    val icon: DrawableResource,
    val online: Boolean?,
    val createdAt: Instant,
    val latestChat: DChat?,
    val route: Routing.Chat,
)