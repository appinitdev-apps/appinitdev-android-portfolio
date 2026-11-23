@Composable
internal fun FileInfoActionButtons(
    file: DFile,
    filesVM: FilesViewModel,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    showRenameDialog: MutableState<Boolean>,
    context: android.content.Context,
    scope: CoroutineScope,
    onDismiss: () -> Unit,
    onShowPasteBar: (Boolean) -> Unit,
) {
    ActionButtons {
        if (!filesVM.showSearchBar.value) {
            IconTextSelectButton {
                filesVM.enterSelectMode()
                filesVM.select(file.path)
                onDismiss()
            }
        }
        IconTextCutButton {
            performCutFiles(filesVM, listOf(file), onShowPasteBar) { onDismiss() }
        }
        IconTextCopyButton {
            performCopyFiles(filesVM, listOf(file), onShowPasteBar) { onDismiss() }
        }
        if (file.isDir) {
            IconTextFavoriteButton(isFavorite = isFavorite) {
                onFavoriteToggle()
            }
        }
        IconTextShareButton {
            ShareHelper.sharePaths(context, setOf(file.path))
            onDismiss()
        }
        if (!file.isDir) {
            IconTextOpenWithButton {
                ShareHelper.openPathWith(context, file.path)
            }
        }
        if (!file.isDir) {
            IconTextZipButton {
                performZipFiles(scope, context, filesVM, listOf(file)) { onDismiss() }
            }
        }
        IconTextRenameButton {
            showRenameDialog.value = true
        }
        IconTextDeleteButton {
            DialogHelper.confirmToDelete {
                filesVM.deleteFiles(setOf(file.path))
                onDismiss()
            }
        }
    }
}
