@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewAudioBottomSheet(
    audioVM: AudioViewModel,
    tagsVM: TagsViewModel,
    tagsMapState: Map<String, List<DTagRelation>>,
    tagsState: List<DTag>,
    dragSelectState: DragSelectState,
    castVM: CastViewModel? = null,
) {
    val m = audioVM.selectedItem.value ?: return
    val context = LocalContext.current
    val onDismiss = {
        audioVM.selectedItem.value = null
    }
    if (audioVM.showRenameDialog.value) {
        FileRenameDialog(path = m.path, onDismiss = {
            audioVM.showRenameDialog.value = false
        }, onDoneAsync = {
            audioVM.loadAsync(context, tagsVM)
            onDismiss()
        })
    }
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
                AudioActionButtons(
                    m = m,
                    audioVM = audioVM,
                    tagsVM = tagsVM,
                    dragSelectState = dragSelectState,
                    context = context,
                    onDismiss = onDismiss,
                )
            }
            if (!audioVM.trash.value) {
                item {
                    VerticalSpace(dp = 16.dp)
                    Subtitle(text = stringResource(Res.string.tags))
                    TagSelector(
                        data = m,
                        tagsVM = tagsVM,
                        tagsMap = tagsMapState,
                        tagsState = tagsState,
                        onChangedAsync = {
                            audioVM.loadAsync(context, tagsVM)
                        }
                    )
                }
            }
            item {
                VerticalSpace(dp = 16.dp)
                PCard {
                    PListItem(title = m.path, action = {
                        CopyIconButton(text = m.path, clipLabel = stringResource(Res.string.file_path))
                    })
                }
            }
            item {
                VerticalSpace(dp = 16.dp)
                PCard {
                    PListItem(title = stringResource(Res.string.file_size), value = m.size.formatBytes())
                    PListItem(title = stringResource(Res.string.type), value = m.path.getMimeType())
                    PListItem(title = stringResource(Res.string.duration), value = m.duration.formatDuration())
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
