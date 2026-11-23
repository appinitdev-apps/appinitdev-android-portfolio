
@Composable
fun ForwardTargetDialog(
    peerVM: PeerViewModel,
    onDismiss: () -> Unit,
    onTargetSelected: (ChatTarget) -> Unit
) {
    val pairedPeers = peerVM.pairedPeers

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.forward),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            LazyColumn(modifier = Modifier.defaultMinSize(minHeight = 100.dp)) {
                item {
                    PDialogListItem(
                        modifier = Modifier.clickable {
                            onTargetSelected(ChatTarget("local", ChatTargetType.PEER))
                            onDismiss()
                        },
                        title = stringResource(Res.string.local_chat),
                        subtitle = stringResource(Res.string.local_chat_desc),
                        showMore = true
                    )
                }

                if (pairedPeers.isNotEmpty()) {
                    items(pairedPeers, key = { it.id }) { peer ->
                        PDialogListItem(
                            modifier = Modifier.clickable {
                                onTargetSelected(ChatTarget(peer.id, ChatTargetType.PEER))
                                onDismiss()
                            },
                            title = peer.name,
                            subtitle = peer.ip,
                            showMore = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(Res.string.close))
            }
        }
    )
}

