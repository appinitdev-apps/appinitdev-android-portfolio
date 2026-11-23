
 









@HiltViewModel
class WaveformViewModel @Inject constructor(
        application: Application
) : WrappedViewModel(application) {

    private val waveformData: MutableLiveData<FloatArray> = MutableLiveData()
    private var currentUri: String? = null
    private var loadJob: Job? = null
    private var amplitudaInstance: Amplituda? = null

     



    fun getWaveformData(): LiveData<FloatArray> = waveformData

    fun loadWaveform(audio: Audio) {
        if (amplitudaInstance == null) {
            amplitudaInstance = Amplituda(getApplication())
        }

        
        if (audio.uri == currentUri && waveformData.value?.isNotEmpty() == true) return

        
        currentUri = audio.uri

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                if (currentUri != audio.uri) return@launch

                postFlatData(audio) 

                val parcelFileDescriptor = getApplication<Application>()
                    .contentResolver.openFileDescriptor(audio.uri.toUri(), "r")
                    ?: throw IllegalArgumentException("Unable to open URI: ${audio.uri}")

                try {

                    val rawAmplitudes = amplitudaInstance!!
                        .processAudio(
                                parcelFileDescriptor,
                                Compress.withParams(Compress.AVERAGE, BARS_PER_SECOND),
                                Cache.withParams(Cache.REUSE, audio.hash.toString())
                        )
                        .get() 
                        .amplitudesAsList()

                    parcelFileDescriptor.close()

                    if (currentUri != audio.uri) return@launch

                    if (rawAmplitudes.isEmpty()) {
                        waveformData.postValue(getRandomGhostData(audio.duration)) 
                        return@launch
                    }

                    

                    val expectedBars = ((audio.duration / 1000f) * BARS_PER_SECOND).toInt().coerceAtLeast(1)

                    val chunkedAmplitudes = FloatArray(expectedBars)
                    val chunkSize = rawAmplitudes.size.toFloat() / expectedBars

                    for (i in 0 until expectedBars) {
                        val start = (i * chunkSize).toInt()
                        val end = ((i + 1) * chunkSize).toInt().coerceAtMost(rawAmplitudes.size)

                        var peakInChunk = 0
                        for (j in start until end) {
                            if (rawAmplitudes[j] > peakInChunk) {
                                peakInChunk = rawAmplitudes[j]
                            }
                        }
                        chunkedAmplitudes[i] = peakInChunk.toFloat()
                    }

                    

                    val maxPeak = chunkedAmplitudes.maxOrNull() ?: 1f
                    val sampled = if (maxPeak > 0f) {
                        FloatArray(expectedBars) { i ->
                            chunkedAmplitudes[i] / maxPeak
                        }
                    } else {
                        getRandomGhostData(audio.duration) 
                    }

                    
                    waveformData.postValue(sampled)

                } catch (e: Exception) {
                    Log.e(TAG, "Amplituda extraction failed on IO thread for ${audio.title}", e)
                    if (currentUri == audio.uri) {
                        postFlatData(audio) 
                    }
                } finally {
                    try {
                        parcelFileDescriptor.close()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to close ParcelFileDescriptor for ${audio.uri}", e)
                    }
                }
            }.getOrElse {
                Log.e(TAG, "Unexpected error in loadWaveform for ${audio.title}", it)
                if (currentUri == audio.uri) {
                    postFlatData(audio) 
                }
            }
        }
    }

     



    fun resetWaveform() {
        loadJob?.cancel()
        currentUri = null
        waveformData.postValue(FloatArray(0))
    }

    private fun postFlatData(audio: Audio) {
        waveformData.postValue(getGhostData(audio.duration))
    }

    private fun getGhostData(durationMs: Long): FloatArray {
        val seconds = (durationMs / 1000).toInt().coerceAtLeast(1)
        return FloatArray(seconds) { 0.05f }
    }

    private fun getRandomGhostData(durationMs: Long): FloatArray {
        
        val expectedBars = ((durationMs / 1000f) * BARS_PER_SECOND).toInt().coerceAtLeast(1)

        return FloatArray(expectedBars) { i ->
            
            val progress = i.toFloat() / expectedBars

            
            
            val envelope = sin(progress * PI).toFloat()

            
            
            val noise = (nextFloat() * 0.8f) + 0.2f

            
            
            (envelope * noise) * 0.4f
        }
    }

    override fun onCleared() {
        super.onCleared()
        amplitudaInstance?.release()
    }

    companion object {
        private const val TAG = "WaveformViewModel"

        private const val BARS_PER_SECOND = 1
    }
}

