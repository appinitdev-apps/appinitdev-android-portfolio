@Composable
internal fun AudioActionButtons(
    m: DAudio,
    audioVM: AudioViewModel,
    tagsVM: TagsViewModel,
    dragSelectState: DragSelectState,
    context: android.content.Context,
    onDismiss: () -> Unit,
) {
    var showAddToHomeDialog by remember { mutableStateOf(false) }
    ActionButtons {
        if (!audioVM.showSearchBar.value) {
            IconTextSelectButton {
                dragSelectState.enterSelectMode()
                dragSelectState.select(m.id)
                onDismiss()
            }
        }
        if (!audioVM.trash.value) {
            IconTextShareButton {
                ShareHelper.shareUris(context, listOf(AudioMediaStoreHelper.getItemUri(m.id)))
                onDismiss()
            }
            if (!m.path.isUrl()) {
                IconTextOpenWithButton {
                    ShareHelper.openPathWith(context, m.path)
                }
                IconTextAddToHomeButton {
                    showAddToHomeDialog = true
                }
            }
            IconTextRenameButton {
                audioVM.showRenameDialog.value = true
            }
        }
        if (AppFeatureType.MEDIA_TRASH.has()) {
            if (audioVM.trash.value) {
                IconTextRestoreButton {
                    audioVM.restore(context, tagsVM, setOf(m.id))
                    onDismiss()
                }
                IconTextDeleteButton {
                    DialogHelper.confirmToDelete {
                        audioVM.delete(context, tagsVM, setOf(m.id))
                        onDismiss()
                    }
                }
            } else {
                IconTextTrashButton {
                    audioVM.trash(context, tagsVM, setOf(m.id))
                    onDismiss()
                }
            }
        } else {
            IconTextDeleteButton {
                DialogHelper.confirmToDelete {
                    audioVM.delete(context, tagsVM, setOf(m.id))
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
