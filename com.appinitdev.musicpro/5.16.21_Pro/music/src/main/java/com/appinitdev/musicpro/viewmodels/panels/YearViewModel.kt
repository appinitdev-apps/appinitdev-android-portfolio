
@HiltViewModel
class YearViewModel @Inject constructor(
        application: Application,
        private val audioRepository: AudioRepository
) : WrappedViewModel(application) {

    private val _years = MutableStateFlow<List<YearGroup>>(emptyList())
    val years: StateFlow<List<YearGroup>> = _years.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            audioRepository.getAllYearsWithAggregation()
                .map { yearList -> yearList.sorted() }
                .distinctUntilChanged()
                .catch { exception ->
                    Log.e(TAG, "Error loading years", exception)
                    emit(emptyList())
                }
                .flowOn(Dispatchers.IO)
                .collect { sortedYears ->
                    _years.value = sortedYears
                    Log.d(TAG, "loadData: ${sortedYears.size} year groups loaded")
                }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            YearPreferences.YEAR_SORT_STYLE,
            YearPreferences.SORT_ORDER -> {
                Log.d(TAG, "onSharedPreferenceChanged: Sorting changed, reloading years")
                loadData()
            }
            LibraryPreferences.MINIMUM_AUDIO_SIZE,
            LibraryPreferences.MINIMUM_AUDIO_LENGTH -> {
                loadData()
            }
        }
    }

    companion object {
        private const val TAG = "YearViewModel"
    }
}

