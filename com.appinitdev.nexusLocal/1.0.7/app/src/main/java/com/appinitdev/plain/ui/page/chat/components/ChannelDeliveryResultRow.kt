@Composable
internal fun ChannelDeliveryResultRow(
    result: DMessageDeliveryResult,
    isSelected: Boolean,
    onToggle: (() -> Unit)?,
) {
    val isSuccess = result.error == null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        if (isSuccess) {
            Icon(
                painter = painterResource(Res.drawable.check),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(18.dp)
                    .padding(top = 1.dp),
            )
        } else {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle?.invoke() },
                modifier = Modifier.size(18.dp),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        ) {
            Text(
                text = result.peerName.ifEmpty { result.peerId },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!isSuccess) {
                Text(
                    text = result.error ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
