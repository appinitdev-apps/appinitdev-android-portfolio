class FeedEntryViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    val showSelectTagsDialog = mutableStateOf(false)
    val item = mutableStateOf<DFeedEntry?>(null)
    val feed = mutableStateOf<DFeed?>(null)
    val content = mutableStateOf("")
    val fetchingContent = mutableStateOf(false)
}
