class MilkdropViewModel(application: Application) : AndroidViewModel(application) {
    private val _presetContent = MutableStateFlow<String?>(null)
    val presetContent: StateFlow<String?> = _presetContent.asStateFlow()
    private val _presets = MutableStateFlow<List<MilkdropPreset>>(emptyList())
    val presets: StateFlow<List<MilkdropPreset>> = _presets.asStateFlow()
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()
    private val _isShuffleEnabled = MutableStateFlow(MilkdropPreferences.isShuffleEnabled())
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()
    private var shuffleJob: Job? = null
    init {
        loadPresetsAndCurrentPreset()
        if (MilkdropPreferences.isShuffleEnabled()) {
            startShuffle()
        }
    }
    fun loadPresetAtIndex(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            loadPresetAtIndexInternal(index)
        }
    }
    fun selectPreset(path: String) {
        if (_isShuffleEnabled.value) {
            _isShuffleEnabled.value = false
            MilkdropPreferences.setShuffleEnabled(false)
            stopShuffle()
        }
        viewModelScope.launch(Dispatchers.IO) {
            val list = _presets.value
            val index = list.indexOfFirst { it.path == path }.coerceAtLeast(0)
            loadPresetAtIndexInternal(index)
        }
    }
    fun toggleShuffle() {
        val enabled = !_isShuffleEnabled.value
        _isShuffleEnabled.value = enabled
        MilkdropPreferences.setShuffleEnabled(enabled)
        if (enabled) {
            startShuffle()
        } else {
            stopShuffle()
        }
    }
    fun reloadFromPreferences() {
        loadPresetsAndCurrentPreset()
    }
    private fun loadPresetsAndCurrentPreset() {
        viewModelScope.launch(Dispatchers.IO) {
            val assets = getApplication<Application>().assets
            val list = PresetManager.listAll(assets)
            _presets.value = list
            val path = MilkdropPreferences.getLastPreset()
                .takeIf { it.isNotBlank() }
                ?: PresetManager.firstPresetPath(assets)?.also {
                    MilkdropPreferences.setLastPreset(it)
                }
                ?: return@launch
            val index = list.indexOfFirst { it.path == path }.coerceAtLeast(0)
            _currentIndex.value = index
            _presetContent.value = PresetManager.loadContent(assets, path)
        }
    }
    private fun loadPresetAtIndexInternal(index: Int) {
        val list = _presets.value
        if (index < 0 || index >= list.size) return
        val preset = list[index]
        _currentIndex.value = index
        MilkdropPreferences.setLastPreset(preset.path)
        _presetContent.value = PresetManager.loadContent(
                getApplication<Application>().assets, preset.path
        )
    }
    private fun startShuffle() {
        shuffleJob?.cancel()
        shuffleJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(SHUFFLE_INTERVAL_MS)
                val list = _presets.value
                if (list.isEmpty()) continue
                val randomIndex = list.indices.random()
                loadPresetAtIndexInternal(randomIndex)
            }
        }
    }
    private fun stopShuffle() {
        shuffleJob?.cancel()
        shuffleJob = null
    }
    override fun onCleared() {
        stopShuffle()
        super.onCleared()
    }
    fun refreshCurrentPreset() {
        viewModelScope.launch(Dispatchers.IO) {
            loadPresetAtIndexInternal(_currentIndex.value)
        }
    }
    companion object {
        private const val SHUFFLE_INTERVAL_MS = 15_000L
    }
}
