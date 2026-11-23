
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AudioFilesSelectModeBottomActions(
    audioVM: AudioViewModel,
    audioPlaylistVM: AudioPlaylistViewModel,
    tagsVM: TagsViewModel,
    tagsState: List<DTag>,
    dragSelectState: DragSelectState,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showSelectTagsDialog by remember { mutableStateOf(false) }
    var removeFromTags by remember { mutableStateOf(false) }

    if (showSelectTagsDialog) {
        val selectedIds = dragSelectState.selectedIds
        val selectedItems = audioVM.itemsFlow.collectAsState().value.filter { selectedIds.contains(it.id) }
        BatchSelectTagsDialog(tagsVM, tagsState, selectedItems, removeFromTags) {
            showSelectTagsDialog = false
            dragSelectState.exitSelectMode()
        }
    }

    PBottomAppBar {
        BottomActionButtons {
            if (!audioVM.trash.value) {
                IconTextSmallButtonLabel {
                    showSelectTagsDialog = true
                    removeFromTags = false
                }
                IconTextSmallButtonLabelOff {
                    showSelectTagsDialog = true
                    removeFromTags = true
                }
                IconTextSmallButtonPlaylistAdd {
                    scope.launch {
                        val selectedIds = dragSelectState.selectedIds
                        val selectedItems = audioVM.itemsFlow.value.filter { selectedIds.contains(it.id) }
                        withIO {
                            audioPlaylistVM.addAsync(context, selectedItems)
                        }
                        dragSelectState.exitSelectMode()
                        DialogHelper.showMessage(Res.string.added_to_playlist)
                    }
                }
                IconTextSmallButtonShare {
                    ShareHelper.shareUris(context, dragSelectState.selectedIds.map { AudioMediaStoreHelper.getItemUri(it) })
                }
            }
            if (AppFeatureType.MEDIA_TRASH.has()) {
                if (audioVM.trash.value) {
                    IconTextSmallButtonRestore {
                        audioVM.restore(context, tagsVM, dragSelectState.selectedIds.toSet())
                        dragSelectState.exitSelectMode()
                    }
                    IconTextSmallButtonDelete {
                        DialogHelper.confirmToDelete {
                            audioVM.delete(context, tagsVM, dragSelectState.selectedIds.toSet())
                            dragSelectState.exitSelectMode()
                        }
                    }
                } else {
                    IconTextSmallButtonTrash {
                        audioVM.trash(context, tagsVM, dragSelectState.selectedIds.toSet())
                        dragSelectState.exitSelectMode()
                    }
                }
            } else {
                IconTextSmallButtonDelete {
                    DialogHelper.confirmToDelete {
                        audioVM.delete(context, tagsVM, dragSelectState.selectedIds.toSet())
                        dragSelectState.exitSelectMode()
                    }
                }
            }
        }
    }
} 