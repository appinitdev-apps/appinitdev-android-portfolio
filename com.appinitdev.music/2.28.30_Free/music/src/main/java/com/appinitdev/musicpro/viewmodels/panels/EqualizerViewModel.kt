
 


















class EqualizerViewModel(application: Application) : WrappedViewModel(application) {

    private val _initialState = MutableStateFlow<EqualizerInitialState?>(null)

     




    val initialState: StateFlow<EqualizerInitialState?> = _initialState.asStateFlow()

     
    private val db: EqualizerDatabase by lazy {
        EqualizerDatabase.getInstance(getApplication())
    }

     




    val allPresets: StateFlow<List<EqualizerPreset>> = db.equalizerPresetDao()
        .getAllPresetsFlow()
        .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
        )

     





    val currentPresetName: StateFlow<String> = combine(
            EqualizerManager.bandGainsFlow,
            EqualizerManager.preampFlow,
            allPresets
    ) { gains, preamp, presets ->
        findMatchingPreset(gains, preamp, presets)?.name ?: CUSTOM_PRESET_NAME
    }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CUSTOM_PRESET_NAME
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _initialState.value = EqualizerInitialState(
                    isEqEnabled = EqualizerPreferences.isEqEnabled(),
                    bandGains = EqualizerPreferences.getAllBandGains(),
                    preampDb = EqualizerPreferences.getPreampDb(),
                    bassDb = EqualizerPreferences.getBassDb(),
                    trebleDb = EqualizerPreferences.getTrebleDb(),
                    balance = EqualizerPreferences.getBalance(),
                    stereoWidth = EqualizerPreferences.getStereoWidth(),
                    tapeSaturationDrive = EqualizerPreferences.getTapeSaturationDrive(),
                    reverbMix = EqualizerPreferences.getReverbMix(),
                    reverbDecay = EqualizerPreferences.getReverbDecay(),
                    reverbSize = EqualizerPreferences.getReverbSize(),
                    reverbDamp = EqualizerPreferences.getReverbDamp(),
                    pitch = EqualizerPreferences.getPitch(),
                    playbackSpeed = EqualizerPreferences.getPlaybackSpeed(),
                    replayGainDb = EqualizerPreferences.getReplayGainDb(),
                    pitchSpeedLocked = EqualizerPreferences.isPitchSpeedLocked()
            )
        }
    }

     












    fun savePreset(
            name: String,
            gains: FloatArray,
            preampDb: Float,
            onDuplicate: () -> Unit,
            onSuccess: () -> Unit
    ) {
        val trimmedName = name.trim()
        viewModelScope.launch(Dispatchers.IO) {
            val existing = db.equalizerPresetDao().getPresetByName(trimmedName)
            if (existing != null) {
                withContext(Dispatchers.Main) { onDuplicate() }
                return@launch
            }
            val preset = EqualizerPreset.fromGains(
                    name = trimmedName,
                    gains = gains,
                    preampDb = preampDb,
                    isBuiltIn = false
            )
            db.equalizerPresetDao().insertPreset(preset)
            withContext(Dispatchers.Main) { onSuccess() }
        }
    }

     







    private fun findMatchingPreset(
            gains: FloatArray,
            preamp: Float,
            presets: List<EqualizerPreset>
    ): EqualizerPreset? {
        return presets.firstOrNull { preset ->
            val presetGains = preset.getBandGains()
            val gainsMatch = gains.size == presetGains.size &&
                    gains.indices.all { i -> abs(gains[i] - presetGains[i]) < GAIN_TOLERANCE }
            val preampMatch = abs(preamp - preset.preampDb) < GAIN_TOLERANCE
            gainsMatch && preampMatch
        }
    }

     





















    data class EqualizerInitialState(
            val isEqEnabled: Boolean,
            val bandGains: FloatArray,
            val preampDb: Float,
            val bassDb: Float,
            val trebleDb: Float,
            val balance: Float,
            val stereoWidth: Float,
            val tapeSaturationDrive: Float,
            val reverbMix: Float,
            val reverbDecay: Float,
            val reverbSize: Float,
            val reverbDamp: Float,
            val pitch: Float,
            val playbackSpeed: Float,
            val replayGainDb: Float = 0f,
            val pitchSpeedLocked: Boolean = false
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is EqualizerInitialState) return false
            return isEqEnabled == other.isEqEnabled
                    && bandGains.contentEquals(other.bandGains)
                    && preampDb == other.preampDb
                    && bassDb == other.bassDb
                    && trebleDb == other.trebleDb
                    && balance == other.balance
                    && stereoWidth == other.stereoWidth
                    && tapeSaturationDrive == other.tapeSaturationDrive
                    && reverbMix == other.reverbMix
                    && reverbDecay == other.reverbDecay
                    && reverbSize == other.reverbSize
                    && reverbDamp == other.reverbDamp
                    && pitch == other.pitch
                    && playbackSpeed == other.playbackSpeed
                    && replayGainDb == other.replayGainDb
                    && pitchSpeedLocked == other.pitchSpeedLocked
        }

        override fun hashCode(): Int {
            var result = isEqEnabled.hashCode()
            result = 31 * result + bandGains.contentHashCode()
            result = 31 * result + preampDb.hashCode()
            result = 31 * result + bassDb.hashCode()
            result = 31 * result + trebleDb.hashCode()
            result = 31 * result + balance.hashCode()
            result = 31 * result + stereoWidth.hashCode()
            result = 31 * result + tapeSaturationDrive.hashCode()
            result = 31 * result + reverbMix.hashCode()
            result = 31 * result + reverbDecay.hashCode()
            result = 31 * result + reverbSize.hashCode()
            result = 31 * result + reverbDamp.hashCode()
            result = 31 * result + pitch.hashCode()
            result = 31 * result + playbackSpeed.hashCode()
            result = 31 * result + replayGainDb.hashCode()
            result = 31 * result + pitchSpeedLocked.hashCode()
            return result
        }
    }

    companion object {
         



        const val CUSTOM_PRESET_NAME = "Custom"

         




        private const val GAIN_TOLERANCE = 0.005f
    }
}
