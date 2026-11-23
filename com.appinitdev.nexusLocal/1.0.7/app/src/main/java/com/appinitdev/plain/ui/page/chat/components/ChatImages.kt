
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatImages(
    context: Context,
    items: List<VChat>,
    m: VChat,
    peer: DPeer?,
    imageWidthDp: Dp,
    imageWidthPx: Int,
    previewerState: MediaPreviewerState,
    chatViewModel: ChatViewModel,
) {
    val imageItems = (m.value as DMessageImages).items
    val downloadProgressMap by DownloadQueue.downloadProgress.collectAsState(mapOf())

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        maxItemsInEachRow = 3,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
        content = {
            imageItems.forEach { item ->
                ChatImageItem(
                    context = context,
                    item = item,
                    items = items,
                    messageId = m.id,
                    peer = peer,
                    imageWidthDp = imageWidthDp,
                    imageWidthPx = imageWidthPx,
                    previewerState = previewerState,
                    downloadTask = downloadProgressMap[item.id],
                )
            }
        },
    )
}
