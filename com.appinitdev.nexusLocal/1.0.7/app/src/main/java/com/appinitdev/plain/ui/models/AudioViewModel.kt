
class AudioViewModel : BaseMediaViewModel<DAudio>() {
    override val dataType = DataType.AUDIO
    val scrollStateMap = mutableStateMapOf<Int, LazyListState>()

    fun delete(context: Context, tagsVM: TagsViewModel, ids: Set<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            DialogHelper.showLoading()
            TagHelper.deleteTagRelationByKeys(ids, dataType)
            AudioMediaStoreHelper.deleteRecordsAndFilesByIdsAsync(context, ids, trash.value)
            val pathes = itemsFlow.value.filter { ids.contains(it.id) }.map { it.path }.toSet()
            AudioPlaylistPreference.deleteAsync(pathes)
            loadAsync(context, tagsVM)
            DialogHelper.hideLoading()
        }
    }
}
