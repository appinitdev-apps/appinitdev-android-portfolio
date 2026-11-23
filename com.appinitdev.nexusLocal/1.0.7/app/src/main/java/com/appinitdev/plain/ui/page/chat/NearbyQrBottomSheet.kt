
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyQrBottomSheet(
    qrData: DQrPairData?,
    onDismiss: () -> Unit,
) {
    PModalBottomSheet(onDismissRequest = onDismiss) {
        PBottomSheetTopAppBar(title = stringResource(Res.string.pair_via_qr_title))
        TopSpace()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (qrData != null) {
                val qrContent = qrData.toQrContent()
                val bitmap = remember(qrContent) {
                    QrCodeGenerateHelper.generate(qrContent, 700, 700)
                }
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = stringResource(Res.string.qrcode),
                    modifier = Modifier.size(280.dp),
                )
                VerticalSpace(dp = 20.dp)
                Text(
                    text = stringResource(Res.string.pair_via_qr_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        BottomSpace()
    }
}
