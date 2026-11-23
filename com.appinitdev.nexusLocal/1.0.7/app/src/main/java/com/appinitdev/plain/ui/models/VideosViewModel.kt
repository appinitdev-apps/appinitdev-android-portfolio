
@OptIn(SavedStateHandleSaveableApi::class)
class VideosViewModel : BaseMediaViewModel<DVideo>() {
    override val dataType = DataType.VIDEO
    val scrollStateMap = mutableStateMapOf<Int, LazyGridState>()

    fun delete(context: Context, tagsVM: TagsViewModel, ids: Set<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            DialogHelper.showLoading()
            TagHelper.deleteTagRelationByKeys(ids, dataType)
            VideoMediaStoreHelper.deleteRecordsAndFilesByIdsAsync(context, ids, trash.value)
            loadAsync(context, tagsVM)
            DialogHelper.hideLoading()
        }
    }
}
