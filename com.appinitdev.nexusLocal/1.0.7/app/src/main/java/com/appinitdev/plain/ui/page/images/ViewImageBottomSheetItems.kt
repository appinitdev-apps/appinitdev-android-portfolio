
@Composable
internal fun ViewImageActionButtons(
    imagesVM: ImagesViewModel,
    tagsVM: TagsViewModel,
    m: com.appinitdev.nexusLocal.data.DImage,
    dragSelectState: DragSelectState,
    qrScanResult: String,
    onShowQrScanResult: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var showAddToHomeDialog by remember { mutableStateOf(false) }
    ActionButtons {
        if (!imagesVM.showSearchBar.value) {
            IconTextSelectButton {
                dragSelectState.enterSelectMode()
                dragSelectState.select(m.id)
                onDismiss()
            }
        }
        if (qrScanResult.isNotEmpty()) {
            IconTextScanQrCodeButton {
                onShowQrScanResult()
            }
        }
        IconTextShareButton {
            ShareHelper.shareUris(context, listOf(ImageMediaStoreHelper.getItemUri(m.id)))
            onDismiss()
        }
        if (!m.path.isUrl()) {
            IconTextOpenWithButton {
                ShareHelper.openPathWith(context, m.path)
            }
        }
        if (!m.path.isUrl() && !imagesVM.trash.value) {
            IconTextAddToHomeButton {
                showAddToHomeDialog = true
            }
        }
        IconTextRenameButton {
            imagesVM.showRenameDialog.value = true
        }
        if (AppFeatureType.MEDIA_TRASH.has()) {
            if (imagesVM.trash.value) {
                IconTextRestoreButton {
                    imagesVM.restore(context, tagsVM, setOf(m.id))
                    onDismiss()
                }
                IconTextDeleteButton {
                    DialogHelper.confirmToDelete {
                        imagesVM.delete(context, tagsVM, setOf(m.id))
                        onDismiss()
                    }
                }
            } else {
                IconTextTrashButton {
                    imagesVM.trash(context, tagsVM, setOf(m.id))
                    onDismiss()
                }
            }
        } else {
            IconTextDeleteButton {
                DialogHelper.confirmToDelete {
                    imagesVM.delete(context, tagsVM, setOf(m.id))
                    onDismiss()
                }
            }
        }
    }
    if (showAddToHomeDialog) {
        AddToHomeDialog(path = m.path, iconRes = R.mipmap.ic_launcher, onDismiss = {
            showAddToHomeDialog = false
            onDismiss()
        })
    }
}

@Composable
internal fun ViewImagePathCard(
    m: com.appinitdev.nexusLocal.data.DImage,
) {
    PCard {
        PListItem(title = m.path, action = {
            CopyIconButton(text = m.path, clipLabel = stringResource(Res.string.file_path))
        })
    }
}
