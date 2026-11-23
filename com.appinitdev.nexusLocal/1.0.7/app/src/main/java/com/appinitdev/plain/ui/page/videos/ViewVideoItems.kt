@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun VideoActionButtons(
    m: DVideo,
    videosVM: VideosViewModel,
    tagsVM: TagsViewModel,
    dragSelectState: DragSelectState,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var showAddToHomeDialog by remember { mutableStateOf(false) }
    ActionButtons {
        if (!videosVM.showSearchBar.value) {
            IconTextSelectButton {
                dragSelectState.enterSelectMode()
                dragSelectState.select(m.id)
                onDismiss()
            }
        }
        IconTextShareButton {
            ShareHelper.shareUris(context, listOf(VideoMediaStoreHelper.getItemUri(m.id)))
            onDismiss()
        }
        if (!m.path.isUrl()) {
            IconTextOpenWithButton {
                ShareHelper.openPathWith(context, m.path)
            }
        }
        if (!m.path.isUrl() && !videosVM.trash.value) {
            IconTextAddToHomeButton {
                showAddToHomeDialog = true
            }
        }
        IconTextRenameButton {
            videosVM.showRenameDialog.value = true
        }
        if (AppFeatureType.MEDIA_TRASH.has()) {
            if (videosVM.trash.value) {
                IconTextRestoreButton {
                    videosVM.restore(context, tagsVM, setOf(m.id))
                    onDismiss()
                }
                IconTextDeleteButton {
                    DialogHelper.confirmToDelete {
                        videosVM.delete(context, tagsVM, setOf(m.id))
                        onDismiss()
                    }
                }
            } else {
                IconTextTrashButton {
                    videosVM.trash(context, tagsVM, setOf(m.id))
                    onDismiss()
                }
            }
        } else {
            IconTextDeleteButton {
                DialogHelper.confirmToDelete {
                    videosVM.delete(context, tagsVM, setOf(m.id))
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
internal fun VideoPathCard(m: DVideo) {
    PCard {
        PListItem(title = m.path, action = {
            CopyIconButton(text = m.path, clipLabel = stringResource(Res.string.file_path))
        })
    }
}
