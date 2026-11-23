@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ViewMediaActionButtons(
    m: PreviewItem,
    qrScanResult: String,
    onShowQrScanResult: () -> Unit,
    onShowRenameDialog: () -> Unit,
    deleteAction: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    ActionButtons {
        if (qrScanResult.isNotEmpty()) {
            IconTextScanQrCodeButton { onShowQrScanResult() }
        }
        if (m.data is DImage || m.data is DVideo) {
            IconTextShareButton {
                if (m.data is DImage) {
                    ShareHelper.shareUris(context, listOf(ImageMediaStoreHelper.getItemUri(m.id)))
                } else {
                    ShareHelper.shareUris(context, listOf(VideoMediaStoreHelper.getItemUri(m.id)))
                }
                onDismiss()
            }
        }
        IconTextRenameButton { onShowRenameDialog() }
        IconTextDeleteButton {
            DialogHelper.confirmToDelete {
                deleteAction()
                onDismiss()
            }
        }
    }
}
@Composable
internal fun ViewMediaPathCard(m: PreviewItem) {
    PCard {
        PListItem(title = m.path, action = {
            CopyIconButton(text = m.path, clipLabel = stringResource(Res.string.file_path))
        })
    }
}
