
/**
 * ViewModel for the [com.appinitdev.musicpro.dialogs.player.MilkdropPresets] dialog.
 *
 * Delegates all asset discovery to [PresetManager], which recursively scans every
 * subdirectory under [PresetManager.PRESETS_ROOT] and caches the result so that
 * this ViewModel and [MilkdropViewModel][com.appinitdev.musicpro.viewmodels.panels.MilkdropViewModel]
 * share a single in-memory preset list without rescanning the asset filesystem.
 *
 * @author Hamza417
 */
class MilkdropPresetsViewModel(application: Application) : AndroidViewModel(application) {

    private val _presets = MutableStateFlow<List<MilkdropPreset>>(emptyList())

    /**
     * Sorted flat list of all `.milk` presets found across every subdirectory of
     * [PresetManager.PRESETS_ROOT].  Emits an empty list until the initial scan
     * completes.
     */
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
