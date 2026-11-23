
@Composable
internal fun ChannelChatInfoDialogs(
    liveChannel: DChatChannel?,
    isOwner: Boolean,
    showRenameDialog: Boolean,
    onDismissRename: () -> Unit,
    channelVM: ChannelViewModel,
    selectedMemberPeer: MutableState<DPeer?>,
    selectedPendingMemberPeer: MutableState<DPeer?>,
) {
    if (showRenameDialog && liveChannel != null) {
        RenameChannelDialog(
            currentName = liveChannel.name,
            onDismiss = onDismissRename,
            onConfirm = { newName -> onDismissRename(); channelVM.renameChannel(liveChannel.id, newName) },
        )
    }

    selectedMemberPeer.value?.let { sp ->
        val isSelf = sp.id == liveChannel?.owner || sp.id == TempData.clientId
        MemberInfoDialog(
            peer = sp, isOwner = isOwner, isSelf = isSelf, liveChannel = liveChannel,
            channelVM = channelVM, onDismiss = { selectedMemberPeer.value = null },
        )
    }

    selectedPendingMemberPeer.value?.let { sp ->
        if (liveChannel != null) {
            PendingMemberDialog(
                peer = sp, liveChannel = liveChannel, channelVM = channelVM,
                onDismiss = { selectedPendingMemberPeer.value = null },
            )
        }
    }
}

@Composable
private fun MemberInfoDialog(
    peer: DPeer, isOwner: Boolean, isSelf: Boolean,
    liveChannel: DChatChannel?, channelVM: ChannelViewModel, onDismiss: () -> Unit,
) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onDismiss) { Text(stringResource(Res.string.close)) } },
        dismissButton = if (isOwner && !isSelf && liveChannel != null) {
            {
                Button(
                    onClick = { channelVM.removeChannelMember(liveChannel.id, peer.id); onDismiss() },
                    colors = ButtonType.DANGER.getColors(),
                ) { Text(stringResource(Res.string.kick_member)) }
            }
        } else null,
        title = { Text(text = peer.name.ifBlank { peer.getBestIp() }, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                PDialogListItem(title = stringResource(Res.string.peer_id), value = peer.id)
                PDialogListItem(title = stringResource(Res.string.ip_address), value = peer.getBestIp())
                PDialogListItem(title = stringResource(Res.string.port), value = peer.port.toString())
                PDialogListItem(title = stringResource(Res.string.device_type), value = DeviceType.fromValue(peer.deviceType).getText())
                val status = peer.getStatusText()
                if (status.isNotEmpty()) {
                    PDialogListItem(title = stringResource(Res.string.status), value = status)
                }
            }
        },
    )
}

@Composable
private fun PendingMemberDialog(
    peer: DPeer, liveChannel: DChatChannel, channelVM: ChannelViewModel, onDismiss: () -> Unit,
) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onDismiss) { Text(stringResource(Res.string.close)) } },
        title = { Text(text = peer.name.ifBlank { peer.getBestIp() }, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                PDialogListItem(
                    modifier = Modifier.clickable { channelVM.resendInvite(liveChannel.id, peer.id); onDismiss() },
                    title = stringResource(Res.string.resend_invite),
                )
                PDialogListItem(
                    modifier = Modifier.clickable { channelVM.removeChannelMember(liveChannel.id, peer.id); onDismiss() },
                    title = stringResource(Res.string.remove_member),
                )
            }
        },
    )
}
