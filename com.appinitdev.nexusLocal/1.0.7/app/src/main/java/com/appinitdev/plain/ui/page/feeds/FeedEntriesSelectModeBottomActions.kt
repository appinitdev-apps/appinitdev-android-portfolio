
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeedEntriesSelectModeBottomActions(
    feedEntriesVM: FeedEntriesViewModel,
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
        BatchSelectTagsDialog(tagsVM, tagsState, feedEntriesVM.getSelectedItems(), removeFromTags) {
            showSelectTagsDialog = false
            feedEntriesVM.exitSelectMode()
        }
    }

    PBottomAppBar {
        BottomActionButtons {
            IconTextSmallButtonLabel {
                showSelectTagsDialog = true
                removeFromTags = false
            }
            IconTextSmallButtonLabelOff {
                showSelectTagsDialog = true
                removeFromTags = true
            }
            IconTextSmallButtonDelete {
                feedEntriesVM.delete(tagsVM, feedEntriesVM.selectedIds.toSet())
                feedEntriesVM.exitSelectMode()
            }
        }
    }
}