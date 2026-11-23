@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeedsSelectModeBottomActions(
    feedsVM: FeedsViewModel,
) {
    PBottomAppBar {
        BottomActionButtons {
            IconTextSmallButtonDelete {
                feedsVM.delete(feedsVM.selectedIds.toSet())
                feedsVM.exitSelectMode()
            }
        }
    }
}