class ImagesViewModel : BaseMediaViewModel<DImage>() {
    override val dataType = DataType.IMAGE
    val scrollStateMap = mutableStateMapOf<Int, LazyGridState>()
    val useAiSearch = mutableStateOf(false)
    suspend fun loadWithAiSearchAsync(context: Context, tagsVM: TagsViewModel) {
        val query = queryText.value.trim()
        val combined = com.appinitdev.nexusLocal.features.media.ImageSearchHelper.searchCombinedAsync(
            context = context,
            queryText = query,
            extraQuery = getQuery(),
            limit = limit.intValue,
            offset = 0,
            sortBy = sortBy.value
        )
        useAiSearch.value = query.isNotEmpty() && com.appinitdev.nexusLocal.ai.ImageSearchManager.isModelReady()
        offset.intValue = 0
        _itemsFlow.value = combined.toMutableStateList()
        tagsVM.loadAsync(_itemsFlow.value.map { it.id }.toSet())
        total.intValue = combined.size
        totalTrash.intValue = 0
        noMore.value = true
        showLoading.value = false
        if (combined.isEmpty()) {
            useAiSearch.value = false
            loadAsync(context, tagsVM)
        }
    }
    fun delete(context: Context, tagsVM: TagsViewModel, ids: Set<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            DialogHelper.showLoading()
            TagHelper.deleteTagRelationByKeys(ids, dataType)
            ImageMediaStoreHelper.deleteRecordsAndFilesByIdsAsync(context, ids, trash.value)
            ImageIndexManager.enqueueRemove(ids)
            loadAsync(context, tagsVM)
            DialogHelper.hideLoading()
        }
    }
}