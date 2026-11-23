
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ViewVideoBottomSheet(
    videosVM: VideosViewModel,
    tagsVM: TagsViewModel,
    tagsMap: Map<String, List<DTagRelation>>,
    tagsState: List<DTag>,
    dragSelectState: DragSelectState,
) {
    val m = videosVM.selectedItem.value ?: return
    val context = LocalContext.current
    val onDismiss = {
        videosVM.selectedItem.value = null
    }
    val viewSize by remember {
        mutableStateOf(m.getRotatedSize())
    }

    if (videosVM.showRenameDialog.value) {
        FileRenameDialog(path = m.path, onDismiss = {
            videosVM.showRenameDialog.value = false
        }, onDoneAsync = {
            videosVM.loadAsync(context, tagsVM)
            onDismiss()
        })
    }

    PModalBottomSheet(onDismissRequest = { onDismiss() }) {
        LazyColumn {
            item {
                VerticalSpace(32.dp)
            }
            item {
                VideoActionButtons(m, videosVM, tagsVM, dragSelectState, onDismiss)
            }
            if (!videosVM.trash.value) {
                item {
                    VerticalSpace(dp = 16.dp)
                    Subtitle(text = stringResource(Res.string.tags))
                    TagSelector(
                        data = m,
                        tagsVM = tagsVM,
                        tagsMap = tagsMap,
                        tagsState = tagsState,
                        onChangedAsync = { videosVM.loadAsync(context, tagsVM) }
                    )
                }
            }
            item {
                VerticalSpace(dp = 16.dp)
                VideoPathCard(m)
            }
            item {
                VerticalSpace(dp = 16.dp)
                PCard {
                    PListItem(title = stringResource(Res.string.file_size), value = m.size.formatBytes())
                    PListItem(title = stringResource(Res.string.type), value = m.path.getMimeType())
                    PListItem(title = stringResource(Res.string.dimensions), value = "${viewSize.width}\u00d7${viewSize.height}")
                    PListItem(title = stringResource(Res.string.created_at), value = m.createdAt.formatDateTime())
                    PListItem(title = stringResource(Res.string.updated_at), value = m.updatedAt.formatDateTime())
                    VideoMetaRows(path = m.path)
                }
            }
            item {
                BottomSpace()
            }
        }
    }
}

