
@Composable
internal fun PeerIconWithStatus(
    icon: DrawableResource,
    title: String,
    online: Boolean?,
) {
    Box(
        modifier = Modifier
            .size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        if (online == true) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-10).dp, y = (-10).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(1.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.green
                    )
            )
        }
    }
}

@Composable
internal fun PeerContextMenu(
    peerId: String,
    showContextMenu: MutableState<Boolean>,
    deleteDeviceText: String,
    deleteText: String,
    deleteWarningText: String,
    cancelText: String,
    onDelete: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp)
            .wrapContentSize(Alignment.Center),
    ) {
        PDropdownMenu(
            expanded = showContextMenu.value,
            onDismissRequest = { showContextMenu.value = false },
        ) {
            PDropdownMenuItem(
                text = { Text(deleteDeviceText) },
                onClick = {
                    showContextMenu.value = false
                    DialogHelper.showConfirmDialog(
                        title = deleteDeviceText,
                        message = deleteWarningText,
                        confirmButton = Pair(deleteText) { onDelete(peerId) },
                        dismissButton = Pair(cancelText) {}
                    )
                },
            )
        }
    }
}
