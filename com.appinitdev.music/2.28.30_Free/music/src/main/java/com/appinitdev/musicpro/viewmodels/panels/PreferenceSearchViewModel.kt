
 








class PreferenceSearchViewModel(application: Application) : WrappedViewModel(application) {

    private val _searchQuery = MutableStateFlow("")

     




    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

     




    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}


