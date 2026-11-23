@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PeerListItem(
    modifier: Modifier = Modifier,
    title: String,
    desc: String,
    icon: DrawableResource,
    online: Boolean? = null,
    latestChat: DChat? = null,
    peerId: String? = null,
    onDelete: ((String) -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    val showContextMenu = remember { mutableStateOf(false) }
    val deleteDeviceText = stringResource(Res.string.delete_device)
    val deleteText = stringResource(Res.string.delete)
    val deleteWarningText = stringResource(Res.string.delete_peer_warning)
    val cancelText = stringResource(Res.string.cancel)
    Box {
        PListItem(
            modifier = modifier.combinedClickable(
                onClick = onClick,
                onLongClick = {
                    if (peerId != null && onDelete != null) {
                        showContextMenu.value = true
                    }
                },
            ),
            title = title,
            subtitle = latestChat?.getMessagePreview() ?: desc,
            start = { PeerIconWithStatus(icon = icon, title = title, online = online) },
            titleTrailing = latestChat?.let { chat ->
                {
                    Text(
                        modifier = Modifier.padding(end = 8.dp),
                        text = chat.createdAt.timeAgo(),
                        style = MaterialTheme.typography.listItemSubtitle()
                    )
                }
            },
        )
        if (peerId != null && onDelete != null) {
            PeerContextMenu(
                peerId = peerId,
                showContextMenu = showContextMenu,
                deleteDeviceText = deleteDeviceText,
                deleteText = deleteText,
                deleteWarningText = deleteWarningText,
                cancelText = cancelText,
                onDelete = onDelete,
            )
        }
    }
}
