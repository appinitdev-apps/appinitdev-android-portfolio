@Composable
fun WebAddressBarQrDialog(
    url: String,
    onClose: () -> Unit,
) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onClose,
        confirmButton = {
            Button(onClick = onClose) {
                Text(stringResource(Res.string.close))
            }
        },
        title = {},
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(Res.string.scan_qrcode_to_access_web),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Image(
                    bitmap = QrCodeGenerateHelper.generate(url, 300, 300).asImageBitmap(),
                    contentDescription = stringResource(Res.string.qrcode),
                    modifier = Modifier.size(300.dp),
                )
            }
        },
    )
}
