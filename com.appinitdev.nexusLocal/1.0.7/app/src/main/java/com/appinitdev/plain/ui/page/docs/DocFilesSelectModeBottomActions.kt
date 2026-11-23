@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DocFilesSelectModeBottomActions(
    docsVM: DocsViewModel,
    tagsVM: TagsViewModel,
    tagsState: List<DTag>,
    dragSelectState: DragSelectState,
) {
    val context = LocalContext.current
    var showSelectTagsDialog by remember { mutableStateOf(false) }
    var removeFromTags by remember { mutableStateOf(false) }
    if (showSelectTagsDialog) {
        val selectedIds = dragSelectState.selectedIds
        val selectedItems = docsVM.itemsFlow.collectAsState().value.filter { selectedIds.contains(it.id) }
        BatchSelectTagsDialog(tagsVM, tagsState, selectedItems, removeFromTags) {
            showSelectTagsDialog = false
            dragSelectState.exitSelectMode()
        }
    }
    PBottomAppBar {
        BottomActionButtons {
            if (!docsVM.trash.value) {
                IconTextSmallButtonLabel {
                    showSelectTagsDialog = true
                    removeFromTags = false
                }
                IconTextSmallButtonLabelOff {
                    showSelectTagsDialog = true
                    removeFromTags = true
                }
                IconTextSmallButtonShare {
                    val selectedIds = dragSelectState.selectedIds
                    val paths = docsVM.itemsFlow.value.filter { selectedIds.contains(it.id) }.map { it.path }.toSet()
                    ShareHelper.sharePaths(context, paths)
                }
            }
            if (AppFeatureType.MEDIA_TRASH.has()) {
                if (docsVM.trash.value) {
                    IconTextSmallButtonRestore {
                        docsVM.restore(context, tagsVM, dragSelectState.selectedIds.toSet())
                        dragSelectState.exitSelectMode()
                    }
                    IconTextSmallButtonDelete {
                        DialogHelper.confirmToDelete {
                            docsVM.delete(context, tagsVM, dragSelectState.selectedIds.toSet())
                            dragSelectState.exitSelectMode()
                        }
                    }
                } else {
                    IconTextSmallButtonTrash {
                        docsVM.trash(context, tagsVM, dragSelectState.selectedIds.toSet())
                        dragSelectState.exitSelectMode()
                    }
                }
            } else {
                IconTextSmallButtonDelete {
                    DialogHelper.confirmToDelete {
                        docsVM.delete(context, tagsVM, dragSelectState.selectedIds.toSet())
                        dragSelectState.exitSelectMode()
                    }
                }
            }
        }
    }
}
