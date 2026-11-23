@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatSelectModeBottomActions(
    chatVM: ChatViewModel,
) {
    val context = LocalContext.current
    PBottomAppBar {
        BottomActionButtons {
            IconTextSmallButtonDelete {
                chatVM.delete(context, chatVM.selectedIds.toSet())
                chatVM.exitSelectMode()
            }
        }
    }
}