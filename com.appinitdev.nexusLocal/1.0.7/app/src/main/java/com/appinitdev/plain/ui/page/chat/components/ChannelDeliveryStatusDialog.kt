
@Composable
fun ChannelDeliveryStatusDialog(
    statusData: DMessageStatusData,
    onResend: (peerIds: List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    val selectedIds = remember {
        mutableStateListOf<String>().apply {
            addAll(statusData.failedResults.map { it.peerId })
        }
    }

    AlertDialog(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = stringResource(Res.string.delivery_status),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = stringResource(
                        Res.string.delivery_status_summary,
                        statusData.deliveredCount,
                        statusData.total,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(statusData.deliveredResults, key = { "d_${it.peerId}" }) { result ->
                    ChannelDeliveryResultRow(result = result, isSelected = false, onToggle = null)
                }
                items(statusData.failedResults, key = { "f_${it.peerId}" }) { result ->
                    ChannelDeliveryResultRow(
                        result = result,
                        isSelected = selectedIds.contains(result.peerId),
                        onToggle = {
                            if (selectedIds.contains(result.peerId)) {
                                selectedIds.remove(result.peerId)
                            } else {
                                selectedIds.add(result.peerId)
                            }
                        },
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = selectedIds.isNotEmpty(),
                onClick = {
                    onResend(selectedIds.toList())
                    onDismiss()
                },
            ) {
                Text(text = stringResource(Res.string.resend_selected))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.close))
            }
        },
    )
}
