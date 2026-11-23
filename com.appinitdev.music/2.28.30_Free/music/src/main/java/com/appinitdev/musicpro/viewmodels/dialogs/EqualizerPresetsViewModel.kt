
 













class EqualizerPresetsViewModel(application: Application) : WrappedViewModel(application) {

    private val db: EqualizerDatabase by lazy {
        EqualizerDatabase.getInstance(getApplication())
    }

     



    val presets: StateFlow<List<EqualizerPreset>> = db.equalizerPresetDao()
        .getAllPresetsFlow()
        .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
        )

    init {
        
        
        viewModelScope.launch(Dispatchers.IO) {
            val builtInCount = db.equalizerPresetDao().getBuiltInPresetCount()
            if (builtInCount == 0) {
                db.equalizerPresetDao().insertPresets(EqualizerDatabase.builtInPresets())
            }
        }
    }

     







    fun savePreset(name: String, gains: FloatArray, preampDb: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val preset = EqualizerPreset.fromGains(
                    name = name.trim(),
                    gains = gains,
                    preampDb = preampDb,
                    isBuiltIn = false
            )
            db.equalizerPresetDao().insertPreset(preset)
        }
    }

     






    fun deletePreset(preset: EqualizerPreset) {
        if (preset.isBuiltIn) return     
        viewModelScope.launch(Dispatchers.IO) {
            db.equalizerPresetDao().deletePreset(preset)
        }
    }
}

