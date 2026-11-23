class MathViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MathRepository(application)
    private val billingManager = BillingManager(application)
    val isPremium: StateFlow<Boolean> = billingManager.isPremium
    fun launchBillingFlow(activity: Activity) {
        billingManager.launchBillingFlow(activity)
    }
    fun setPremium(premium: Boolean) {
        billingManager.setPremium(premium)
    }
    private val _currentLanguage = MutableStateFlow(repository.getSelectedLanguage())
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()
    private val _themeMode = MutableStateFlow(repository.getThemeMode())
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()
    private val _selectedDomain = MutableStateFlow(MathDomain.ALL)
    val selectedDomain: StateFlow<MathDomain> = _selectedDomain.asStateFlow()
    private val _categories = MutableStateFlow<List<MathCategory>>(emptyList())
    val categories: StateFlow<List<MathCategory>> = _categories.asStateFlow()
    private val _filteredCategories = MutableStateFlow<List<MathCategory>>(emptyList())
    val filteredCategories: StateFlow<List<MathCategory>> = _filteredCategories.asStateFlow()
    private val _selectedCategory = MutableStateFlow<MathCategory?>(null)
    val selectedCategory: StateFlow<MathCategory?> = _selectedCategory.asStateFlow()
    private val _categoryTopics = MutableStateFlow<List<MathTopic>>(emptyList())
    val categoryTopics: StateFlow<List<MathTopic>> = _categoryTopics.asStateFlow()
    private val _selectedTopic = MutableStateFlow<MathTopic?>(null)
    val selectedTopic: StateFlow<MathTopic?> = _selectedTopic.asStateFlow()
    private val _topicMarkdownContent = MutableStateFlow("")
    val topicMarkdownContent: StateFlow<String> = _topicMarkdownContent.asStateFlow()
    private val _selectedDetailTab = MutableStateFlow(DetailTab.ALL)
    val selectedDetailTab: StateFlow<DetailTab> = _selectedDetailTab.asStateFlow()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _advancedSearchResults = MutableStateFlow<List<SearchResultMatch>>(emptyList())
    val advancedSearchResults: StateFlow<List<SearchResultMatch>> = _advancedSearchResults.asStateFlow()
    private val _searchFilterOnlyExamples = MutableStateFlow(false)
    val searchFilterOnlyExamples: StateFlow<Boolean> = _searchFilterOnlyExamples.asStateFlow()
    private val _favoriteTopics = MutableStateFlow<List<MathTopic>>(emptyList())
    val favoriteTopics: StateFlow<List<MathTopic>> = _favoriteTopics.asStateFlow()
    private val _recentTopics = MutableStateFlow<List<MathTopic>>(emptyList())
    val recentTopics: StateFlow<List<MathTopic>> = _recentTopics.asStateFlow()
    private val _pinnedTopics = MutableStateFlow<List<MathTopic>>(emptyList())
    val pinnedTopics: StateFlow<List<MathTopic>> = _pinnedTopics.asStateFlow()
    private val screenHistory = mutableListOf<Screen>()
    init {
        loadInitialData()
    }
    private fun loadInitialData() {
        viewModelScope.launch {
            val cats = repository.getCategories()
            _categories.value = cats
            filterCategoriesByDomain(_selectedDomain.value)
            updateFavorites()
            updateRecents()
            updatePinned()
        }
    }
    private fun updateRecents() {
        _recentTopics.value = repository.getRecentTopics()
    }
    private fun updatePinned() {
        _pinnedTopics.value = repository.getPinnedTopics()
    }
    fun togglePinnedTopic(topic: MathTopic) {
        viewModelScope.launch {
            repository.togglePinnedTopic(topic.id)
            updatePinned()
        }
    }
    fun isTopicPinned(topicId: String): Boolean {
        return repository.isTopicPinned(topicId)
    }
    fun switchLanguage(language: String) {
        if (_currentLanguage.value != language) {
            repository.setSelectedLanguage(language)
            _currentLanguage.value = language
            loadInitialData()
            _selectedCategory.value?.let { cat ->
                val topics = repository.getTopicsForCategory(cat.id)
                _categoryTopics.value = topics
            }
            _selectedTopic.value?.let { topic ->
                val updatedTopic = repository.getAllTopics().find { it.id == topic.id } ?: topic
                selectTopic(updatedTopic)
            }
        }
    }
    fun selectDomain(domain: MathDomain) {
        _selectedDomain.value = domain
        filterCategoriesByDomain(domain)
    }
    private fun filterCategoriesByDomain(domain: MathDomain) {
        val allCats = _categories.value
        _filteredCategories.value = if (domain == MathDomain.ALL) {
            allCats
        } else {
            allCats.filter { it.domain == domain }
        }
    }
    fun selectCategory(category: MathCategory) {
        _selectedCategory.value = category
        viewModelScope.launch {
            val topics = repository.getTopicsForCategory(category.id)
            _categoryTopics.value = topics
            navigateTo(Screen.CATEGORY_DETAIL)
        }
    }
    fun selectTopic(topic: MathTopic) {
        _selectedTopic.value = topic
        _selectedDetailTab.value = DetailTab.ALL
        repository.addRecentTopic(topic.id)
        updateRecents()
        viewModelScope.launch {
            val content = repository.getTopicMarkdownContent(topic.filePath)
            _topicMarkdownContent.value = content
            navigateTo(Screen.TOPIC_DETAIL)
        }
    }
    fun setDetailTab(tab: DetailTab) {
        _selectedDetailTab.value = tab
    }
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _advancedSearchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            val results = repository.advancedSearch(query, _searchFilterOnlyExamples.value)
            _advancedSearchResults.value = results
        }
    }
    fun toggleSearchExamplesFilter() {
        _searchFilterOnlyExamples.value = !_searchFilterOnlyExamples.value
        updateSearchQuery(_searchQuery.value)
    }
    fun toggleFavorite(topic: MathTopic) {
        viewModelScope.launch {
            val newFavState = repository.toggleFavorite(topic.id)
            updateFavorites()
            _categoryTopics.value = _categoryTopics.value.map {
                if (it.id == topic.id) it.copy(isFavorite = newFavState) else it
            }
            _advancedSearchResults.value = _advancedSearchResults.value.map { match ->
                if (match.topic.id == topic.id) match.copy(topic = match.topic.copy(isFavorite = newFavState)) else match
            }
            if (_selectedTopic.value?.id == topic.id) {
                _selectedTopic.value = _selectedTopic.value?.copy(isFavorite = newFavState)
            }
        }
    }
    private fun updateFavorites() {
        val allTopics = repository.getAllTopics()
        _favoriteTopics.value = allTopics.filter { it.isFavorite }
    }
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }
    fun navigateBack(): Boolean {
        if (screenHistory.isNotEmpty()) {
            val prev = screenHistory.removeAt(screenHistory.size - 1)
            _currentScreen.value = prev
            return true
        }
        if (_currentScreen.value != Screen.HOME) {
            _currentScreen.value = Screen.HOME
            return true
        }
        return false
    }
    fun nextTopic() {
        val current = _selectedTopic.value ?: return
        val list = _categoryTopics.value
        val currentIndex = list.indexOfFirst { it.id == current.id }
        if (currentIndex != -1 && currentIndex < list.size - 1) {
            selectTopic(list[currentIndex + 1])
        }
    }
    fun previousTopic() {
        val current = _selectedTopic.value ?: return
        val list = _categoryTopics.value
        val currentIndex = list.indexOfFirst { it.id == current.id }
        if (currentIndex > 0) {
            selectTopic(list[currentIndex - 1])
        }
    }
    fun getTopicNote(topicId: String): String {
        return repository.getNoteForTopic(topicId)
    }
    fun saveTopicNote(topicId: String, note: String) {
        repository.saveNoteForTopic(topicId, note)
    }
    fun changeThemeMode(mode: String) {
        repository.setThemeMode(mode)
        _themeMode.value = mode
    }
}
