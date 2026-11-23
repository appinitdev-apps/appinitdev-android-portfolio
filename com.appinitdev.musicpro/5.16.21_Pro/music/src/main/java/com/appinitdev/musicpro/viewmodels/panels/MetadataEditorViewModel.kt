
 












@HiltViewModel(assistedFactory = MetadataEditorViewModel.Factory::class)
class MetadataEditorViewModel @AssistedInject constructor(
        application: Application,
        @Assisted val audio: Audio
) : WrappedViewModel(application) {

    private val _isSaving = MutableStateFlow(false)

     
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveResult = MutableSharedFlow<SaveResult>(extraBufferCapacity = 1)

     
    val saveResult: SharedFlow<SaveResult> = _saveResult.asSharedFlow()

     











    fun saveMetadata(fields: MetadataWriter.Fields, updatedAudio: Audio, artworkFile: java.io.File? = null) {
        if (_isSaving.value) return
        viewModelScope.launch(Dispatchers.IO) {
            _isSaving.emit(true)
            try {
                val path = updatedAudio.uri
                    ?: throw IllegalStateException("Audio path must not be null.")

                
                
                val fieldsWithArt = if (artworkFile != null) fields.copy(artworkFile = artworkFile) else fields

                MetadataWriter.write(path.toUri(), fieldsWithArt, contentResolver)

                updatedAudio.dateModified = System.currentTimeMillis() / 1000L

                AudioDatabase.getInstance(getApplication())
                    .audioDao()
                    ?.update(updatedAudio)

                MediaScannerConnection.scanFile(
                        getApplication(),
                        arrayOf(path),
                        null
                ) { _, _ ->
                    Log.d(TAG, "MediaStore scan complete: $path")
                }

                _saveResult.emit(SaveResult.Success)

                
                
                MediaPlaybackManager.replaceAndNotifyCurrentAudio(updatedAudio)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save metadata: ${e.message}", e)
                _saveResult.emit(SaveResult.Error(e.message ?: "Unknown error"))
            } finally {
                _isSaving.emit(false)
            }
        }
    }

     
    sealed class SaveResult {
         
        data object Success : SaveResult()

         
        data class Error(val message: String) : SaveResult()
    }

     
    @AssistedFactory
    interface Factory {
         
        fun create(audio: Audio): MetadataEditorViewModel
    }

    companion object {
        private const val TAG = "MetadataEditorViewModel"
    }
}

