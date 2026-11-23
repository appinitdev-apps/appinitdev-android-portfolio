
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VideoFilesSelectModeBottomActions(
    videosVM: VideosViewModel,
    tagsVM: TagsViewModel,
    tagsState: List<DTag>,
    dragSelectState: DragSelectState,
) {
    MediaFilesSelectModeBottomActions(
        vm = videosVM,
        tagsVM = tagsVM,
        tagsState = tagsState,
        dragSelectState = dragSelectState,
        getItemUri = { VideoMediaStoreHelper.getItemUri(it) },
        getCollectableItems = { videosVM.itemsFlow.collectAsStateValue() },
        isInTrashMode = videosVM.trash.value
    )
}