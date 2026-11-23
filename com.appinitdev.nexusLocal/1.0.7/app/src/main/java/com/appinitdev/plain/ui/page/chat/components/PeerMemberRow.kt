@Composable
internal fun PeerMemberRow(
    peer: DPeer,
    isMember: Boolean,
    isPending: Boolean,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (isMember) {
                Icon(
                    painter = painterResource(Res.drawable.check),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            } else {
                Icon(
                    painter = painterResource(DeviceType.fromValue(peer.deviceType).getIcon()),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column {
                Text(
                    text = peer.name.ifBlank { peer.getBestIp() },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (isPending) {
                    Text(
                        text = stringResource(Res.string.pending_invite),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (isMember || isPending) {
            OutlinedButton(
                onClick = onRemove,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(
                    text = stringResource(Res.string.remove_member),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        } else {
            Button(onClick = onAdd) {
                Text(
                    text = stringResource(Res.string.add_member),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
