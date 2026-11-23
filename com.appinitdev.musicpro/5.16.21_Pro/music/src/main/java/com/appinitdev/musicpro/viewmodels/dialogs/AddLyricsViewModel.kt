
 


private val LRC_TIMESTAMP_REGEX = Regex("""\[\d{1,2}:\d{2}\.\d{2,3}]""")

@HiltViewModel(assistedFactory = AddLyricsViewModel.Factory::class)
class AddLyricsViewModel @AssistedInject constructor(
        application: Application,
        @Assisted val audio: Audio,
        private val lrcRepository: LrcRepository
) : AndroidViewModel(application) {

     



    private val _saveResult = MutableSharedFlow<SaveResult>(extraBufferCapacity = 1)
    val saveResult: SharedFlow<SaveResult> = _saveResult.asSharedFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

     


    fun hasTimestamps(text: String): Boolean {
        return LRC_TIMESTAMP_REGEX.containsMatchIn(text)
    }

     



    fun saveLyrics(text: String) {
        if (text.isBlank()) {
            viewModelScope.launch {
                _saveResult.emit(SaveResult.Error("Lyrics text is empty."))
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isSaving.emit(true)
            try {
                val audioPath = audio.uri ?: run {
                    _saveResult.emit(SaveResult.Error("Audio path is null."))
                    return@launch
                }

                val result = if (hasTimestamps(text)) {
                    
                    Log.d(TAG, "Timestamps detected, saving as .lrc")
                    lrcRepository.saveLrcToFile(text, audioPath)
                } else {
                    
                    Log.d(TAG, "No timestamps detected, saving as .txt")
                    lrcRepository.saveTxtToFile(text, audioPath)
                }

                result.fold(
                        onSuccess = { file ->
                            _saveResult.emit(SaveResult.Success(file.path ?: "", hasTimestamps(text)))
                        },
                        onFailure = { e ->
                            _saveResult.emit(SaveResult.Error(e.message ?: "Unknown error"))
                        }
                )
            } finally {
                _isSaving.emit(false)
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(audio: Audio): AddLyricsViewModel
    }

    sealed class SaveResult {
        data class Success(val filePath: String, val isLrc: Boolean) : SaveResult()
        data class Error(val message: String) : SaveResult()
    }

    companion object {
        private const val TAG = "AddLyricsViewModel"
    }
}

