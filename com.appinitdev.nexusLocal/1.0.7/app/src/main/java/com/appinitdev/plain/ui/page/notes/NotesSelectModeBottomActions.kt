@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotesSelectModeBottomActions(
    notesVM: NotesViewModel,
    tagsVM: TagsViewModel,
    tagsState: List<DTag>,
) {
    var showSelectTagsDialog by remember {
        mutableStateOf(false)
    }
    var removeFromTags by remember {
        mutableStateOf(false)
    }
    if (showSelectTagsDialog) {
        BatchSelectTagsDialog(tagsVM, tagsState, notesVM.getSelectedItems(), removeFromTags) {
            showSelectTagsDialog = false
            notesVM.exitSelectMode()
        }
    }
    PBottomAppBar {
        BottomActionButtons {
            if (notesVM.trash.value) {
                IconTextSmallButtonRestore {
                    notesVM.restore(tagsVM, notesVM.selectedIds.toSet())
                    notesVM.exitSelectMode()
                }
                IconTextSmallButtonDelete {
                    notesVM.delete(tagsVM, notesVM.selectedIds.toSet())
                    notesVM.exitSelectMode()
                }
            } else {
                IconTextSmallButtonLabel {
                    showSelectTagsDialog = true
                    removeFromTags = false
                }
                IconTextSmallButtonLabelOff {
                    showSelectTagsDialog = true
                    removeFromTags = true
                }
                IconTrashButton {
                    notesVM.trash(tagsVM, notesVM.selectedIds.toSet())
                    notesVM.exitSelectMode()
                }
            }
        }
    }
}