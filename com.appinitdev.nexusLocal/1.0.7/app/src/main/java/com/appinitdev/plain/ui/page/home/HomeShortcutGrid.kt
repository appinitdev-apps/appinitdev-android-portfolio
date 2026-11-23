@Composable
fun HomeShortcutGrid(
    navController: NavHostController,
    peerVM: PeerViewModel,
    channelVM: ChannelViewModel,
    showOnlineStatus: Boolean,
) {
    val context = LocalContext.current
    val featuresStr = remember {
        context.dataStore.dataFlow.map { HomeFeaturesPreference.get(it) }
    }.collectAsStateValue(initial = HomeFeaturesPreference.default)
    val enabledIds = remember(featuresStr) {
        HomeFeaturesPreference.parseList(featuresStr.ifEmpty { HomeFeaturesPreference.default })
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (AppFeatureType.CHAT.name in enabledIds) {
            HomeChatWidget(navController, peerVM, channelVM, showOnlineStatus)
        }
        HomeFeatureItemsGrid(navController)
    }
}
