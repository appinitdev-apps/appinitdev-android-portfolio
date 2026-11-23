@SuppressLint("ResourceType")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun <T : IData> MediaFilesSelectModeBottomActions(
    vm: BaseMediaViewModel<T>,
    tagsVM: TagsViewModel,
    tagsState: List<DTag>,
    dragSelectState: DragSelectState,
    getItemUri: (String) -> android.net.Uri,
    getCollectableItems: @Composable () -> List<T>,
    isInTrashMode: Boolean
) {
    val context = LocalContext.current
    var showSelectTagsDialog by remember {
        mutableStateOf(false)
    }
    var removeFromTags by remember {
        mutableStateOf(false)
    }
    if (showSelectTagsDialog) {
        val selectedIds = dragSelectState.selectedIds
        val selectedItems = getCollectableItems().filter { selectedIds.contains(it.id) }
        BatchSelectTagsDialog(tagsVM, tagsState, selectedItems, removeFromTags) {
            showSelectTagsDialog = false
            dragSelectState.exitSelectMode()
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
            IconTextSmallButtonShare {
                ShareHelper.shareUris(context, dragSelectState.selectedIds.map { getItemUri(it) })
            }
            if (AppFeatureType.MEDIA_TRASH.has()) {
                if (isInTrashMode) {
                    IconTextSmallButtonRestore {
                        @Suppress("UNCHECKED_CAST")
                        when (vm) {
                            is ImagesViewModel -> vm.restore(context, tagsVM, dragSelectState.selectedIds.toSet())
                            is VideosViewModel -> vm.restore(context, tagsVM, dragSelectState.selectedIds.toSet())
                            is AudioViewModel -> vm.restore(context, tagsVM, dragSelectState.selectedIds.toSet())
                        }
                        dragSelectState.exitSelectMode()
                    }
                    IconTextSmallButtonDelete {
                        DialogHelper.confirmToDelete {
                            @Suppress("UNCHECKED_CAST")
                            when (vm) {
                                is ImagesViewModel -> vm.delete(context, tagsVM, dragSelectState.selectedIds.toSet())
                                is VideosViewModel -> vm.delete(context, tagsVM, dragSelectState.selectedIds.toSet())
                                is AudioViewModel -> vm.delete(context, tagsVM, dragSelectState.selectedIds.toSet())
                            }
                            dragSelectState.exitSelectMode()
                        }
                    }
                } else {
                    IconTextSmallButtonTrash {
                        @Suppress("UNCHECKED_CAST")
                        when (vm) {
                            is ImagesViewModel -> vm.trash(context, tagsVM, dragSelectState.selectedIds.toSet())
                            is VideosViewModel -> vm.trash(context, tagsVM, dragSelectState.selectedIds.toSet())
                            is AudioViewModel -> vm.trash(context, tagsVM, dragSelectState.selectedIds.toSet())
                        }
                        dragSelectState.exitSelectMode()
                    }
                }
            } else {
                IconTextSmallButtonDelete {
                    DialogHelper.confirmToDelete {
                        @Suppress("UNCHECKED_CAST")
                        when (vm) {
                            is ImagesViewModel -> vm.delete(context, tagsVM, dragSelectState.selectedIds.toSet())
                            is VideosViewModel -> vm.delete(context, tagsVM, dragSelectState.selectedIds.toSet())
                            is AudioViewModel -> vm.delete(context, tagsVM, dragSelectState.selectedIds.toSet())
                        }
                        dragSelectState.exitSelectMode()
                    }
                }
            }
        }
    }
} 