
@Composable
fun MainDialogs(
    loadingEvent: LoadingDialogEvent?,
    confirmEvent: ConfirmDialogEvent?,
    onDismissConfirm: () -> Unit,
    toastState: ToastEvent?,
    onDismissToast: () -> Unit,
) {
    loadingEvent?.let {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        ) {
            Surface(
                modifier = Modifier.widthIn(min = 150.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )
                    if (it.message.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = it.message,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    confirmEvent?.let {
        AlertDialog(
            onDismissRequest = onDismissConfirm,
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            title = {
                if (it.title.isNotEmpty()) {
                    Text(
                        text = it.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = it.message,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { it.confirmButton.second(); onDismissConfirm() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = it.confirmButton.first,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                if (it.dismissButton != null) {
                    TextButton(onClick = { it.dismissButton.second(); onDismissConfirm() }) {
                        Text(
                            text = it.dismissButton.first,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
        )
    }
    toastState?.let { event ->
        PToast(message = event.message, type = event.type, onDismiss = onDismissToast)
    }
}
