@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoGridItem(
    modifier: Modifier = Modifier,
    videosVM: VideosViewModel,
    castVM: CastViewModel,
    m: DVideo,
    showSize: Boolean,
    previewerState: MediaPreviewerState,
    dragSelectState: DragSelectState,
    widthPx: Int,
    sort: FileSortBy
) {
    val isSelected by remember { derivedStateOf { dragSelectState.isSelected(m.id) } }
    val inSelectionMode = dragSelectState.selectMode
    val selected = isSelected || videosVM.selectedItem.value?.id == m.id
    val itemState = rememberTransformItemState()
    Box(
        modifier = modifier
            .combinedClickable(
                onClick = {
                    if (castVM.castMode.value) {
                        castVM.cast(m.path)
                    } else if (inSelectionMode) {
                        dragSelectState.addSelected(m.id)
                    } else {
                        coMain {
                            withIO { MediaPreviewData.setDataAsync(itemState, videosVM.itemsFlow.value, m) }
                            previewerState.openTransform(
                                index = MediaPreviewData.items.indexOfFirst { it.id == m.id },
                                itemState = itemState,
                            )
                        }
                    }
                },
                onLongClick = {
                    if (inSelectionMode) {
                        return@combinedClickable
                    }
                    videosVM.selectedItem.value = m
                },
            )
            .then(
                if (!inSelectionMode) {
                    Modifier
                } else {
                    Modifier.toggleable(
                        value = selected,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onValueChange = { toggled ->
                            if (toggled) {
                                dragSelectState.addSelected(m.id)
                            } else {
                                dragSelectState.removeSelected(m.id)
                            }
                        }
                    )
                },
            ),
    ) {
        TransformImageView(
            modifier = Modifier.fillMaxSize().align(Alignment.Center).aspectRatio(1f),
            path = m.path,
            fileName = m.path.getFilenameFromPath(),
            key = m.id,
            uri = VideoMediaStoreHelper.getItemUri(m.id),
            itemState = itemState,
            previewerState = previewerState,
            widthPx = widthPx
        )
        if (selected) {
            SelectedOverlay()
        } else if (castVM.castMode.value) {
            CastModeOverlay()
        }
        if (inSelectionMode) {
            Box(modifier = Modifier.align(Alignment.TopStart)) {
                SelectionCheckbox(selected = selected, id = m.id, dragSelectState = dragSelectState)
            }
        }
        if (showSize) {
            Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                SizeLabel(
                    text = if (setOf(FileSortBy.SIZE_ASC, FileSortBy.SIZE_DESC).contains(sort))
                        m.size.formatBytes() else m.duration.formatDuration()
                )
            }
        }
    }
}