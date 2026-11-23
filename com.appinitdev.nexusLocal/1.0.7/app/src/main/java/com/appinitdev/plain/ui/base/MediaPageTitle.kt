@Composable
internal fun getMediaPageTitle(
    mediaType: DataType,
    castVM: CastViewModel,
    bucket: DMediaBucket?,
    dragSelectState: DragSelectState,
    tag: MutableState<com.appinitdev.nexusLocal.db.DTag?>,
    trash: MutableState<Boolean>
): String {
    val resourceId = when (mediaType) {
        DataType.IMAGE -> Res.string.images
        DataType.VIDEO -> Res.string.videos
        DataType.AUDIO -> Res.string.audios
        DataType.DOC -> Res.string.docs
        else -> Res.string.files
    }
    val mediaName = bucket?.name ?: stringResource(resourceId)
    return if (castVM.castMode.value) {
        stringResource(Res.string.cast_mode) + " - " + CastPlayer.currentDevice?.description?.device?.friendlyName
    } else if (dragSelectState.selectMode) {
        LocaleHelper.getStringF(Res.string.x_selected, "count", dragSelectState.selectedIds.size)
    } else {
        mediaName
    }
}
