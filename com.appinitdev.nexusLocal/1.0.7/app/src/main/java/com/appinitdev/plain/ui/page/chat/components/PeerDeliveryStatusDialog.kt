@Composable
fun PeerDeliveryStatusDialog(
    statusData: DMessageStatusData,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    val failed = statusData.failedResults.firstOrNull()
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.delivery_status),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = failed?.error ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        },
        confirmButton = {
            Button(
                enabled = failed != null,
                onClick = {
                    onRetry()
                    onDismiss()
                },
            ) {
                Text(text = stringResource(Res.string.try_again))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(Res.string.close))
            }
        },
    )
}
