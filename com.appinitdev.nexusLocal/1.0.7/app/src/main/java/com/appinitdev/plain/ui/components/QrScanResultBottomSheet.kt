
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QrScanResultBottomSheet(
    context: android.content.Context,
    value: String,
    onDismiss: () -> Unit,
) {
    PModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
    ) {
        PBottomSheetTopAppBar(title = stringResource(Res.string.scan_result)) {
            CopyIconButton(text = value, clipLabel = stringResource(Res.string.scan_result))
        }
        TopSpace()
        ScanResult(context, text = value)
        BottomSpace()
    }
}