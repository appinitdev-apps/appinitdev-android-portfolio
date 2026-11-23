
@Composable
fun ChatText(
    context: Context,
    chatVM: ChatViewModel,
    focusManager: FocusManager,
    m: VChat,
    onDoubleClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val messageText = m.value as DMessageText
    val text = messageText.text.linkify()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        PClickableText(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            onClick = { position ->
                if (chatVM.selectMode.value) {
                    chatVM.select(m.id)
                } else {
                    focusManager.clearFocus()
                    text.urlAt(context, position)
                }
            },
            onDoubleClick = onDoubleClick,
            onLongClick = onLongClick
        )

        if (messageText.linkPreviews.isNotEmpty()) {
            messageText.linkPreviews.forEach { linkPreview ->
                ChatLinkPreview(
                    linkPreview = linkPreview,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }
}
