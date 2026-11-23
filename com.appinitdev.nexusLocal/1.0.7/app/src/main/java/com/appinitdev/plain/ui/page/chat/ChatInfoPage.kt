
/**
 * Dispatcher that routes to [PeerChatInfoPage] or [ChannelChatInfoPage]
 * depending on the current chat type held in [chatVM].
 */
@Composable
fun ChatInfoPage(
    navController: NavHostController,
    chatVM: ChatViewModel,
    peerVM: PeerViewModel,
    channelVM: ChannelViewModel,
) {
    val chatState = chatVM.chatState.collectAsState()
    if (chatState.value.target.type == ChatTargetType.PEER) {
        PeerChatInfoPage(navController, chatVM, peerVM)
    } else {
        ChannelChatInfoPage(navController, chatVM, peerVM, channelVM)
    }
}
