@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ViewNoteBottomSheet(
    notesVM: NotesViewModel,
    tagsVM: TagsViewModel,
    tagsMap: Map<String, List<DTagRelation>>,
    tagsState: List<DTag>,
) {
    val m = notesVM.selectedItem.value ?: return
    val onDismiss = {
        notesVM.selectedItem.value = null
    }
    val scope = rememberCoroutineScope()
    PModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
    ) {
        LazyColumn {
            item {
                VerticalSpace(32.dp)
            }
            item {
                ActionButtons {
                    if (!notesVM.showSearchBar.value) {
                        IconTextSelectButton {
                            notesVM.enterSelectMode()
                            notesVM.select(m.id)
                            onDismiss()
                        }
                    }
                    if (notesVM.trash.value) {
                        IconTextRestoreButton {
                            notesVM.restore(tagsVM, setOf(m.id))
                            onDismiss()
                        }
                        IconTextDeleteButton {
                            notesVM.delete(tagsVM, setOf(m.id))
                            onDismiss()
                        }
                    } else {
                        IconTextTrashButton {
                            notesVM.trash(tagsVM, setOf(m.id))
                            onDismiss()
                        }
                    }
                }
            }
            if (!notesVM.trash.value) {
                item {
                    VerticalSpace(dp = 16.dp)
                    Subtitle(text = stringResource(Res.string.tags))
                    TagSelector(
                        data = m,
                        tagsVM = tagsVM,
                        tagsMap = tagsMap,
                        tagsState = tagsState,
                        onChangedAsync = {
                            notesVM.loadAsync(tagsVM)
                        }
                    )
                }
            }
            item {
                VerticalSpace(dp = 16.dp)
                PCard {
                    PListItem(title = stringResource(Res.string.created_at), value = m.createdAt.formatDateTime())
                    PListItem(title = stringResource(Res.string.updated_at), value = m.updatedAt.formatDateTime())
                }
            }
            item {
                BottomSpace()
            }
        }
    }
}
