@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImageFilesSelectModeBottomActions(
    imagesVM: ImagesViewModel,
    tagsVM: TagsViewModel,
    tagsState: List<DTag>,
    dragSelectState: DragSelectState,
) {
    MediaFilesSelectModeBottomActions(
        vm = imagesVM,
        tagsVM = tagsVM,
        tagsState = tagsState,
        dragSelectState = dragSelectState,
        getItemUri = { ImageMediaStoreHelper.getItemUri(it) },
        getCollectableItems = { imagesVM.itemsFlow.collectAsStateValue() },
        isInTrashMode = imagesVM.trash.value
    )
}