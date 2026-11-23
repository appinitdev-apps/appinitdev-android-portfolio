class MilkdropPresetsViewModel(application: Application) : AndroidViewModel(application) {
    private val _presets = MutableStateFlow<List<MilkdropPreset>>(emptyList())
    val presets: StateFlow<List<MilkdropPreset>> = _presets.asStateFlow()
    init {
        loadPresets()
    }
    private fun loadPresets() {
        viewModelScope.launch(Dispatchers.IO) {
            _presets.value = PresetManager.listAll(getApplication<Application>().assets)
        }
    }
}
