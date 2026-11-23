
enum class AmbientSoundChannel(val displayName: String, val defaultVolume: Float) {
    RAIN("Gentle Rain", 0.5f),
    BROWN_NOISE("Deep Brown Noise", 0.4f),
    WHITE_NOISE("Pure White Noise", 0.3f),
    OCEAN_WAVES("Ocean Waves", 0.5f),
    CAFE_HUM("Coffee Shop Hum", 0.3f)
}

data class AmbientMixerState(
    val isPlaying: Boolean = false,
    val volumes: Map<AmbientSoundChannel, Float> = AmbientSoundChannel.entries.associateWith { it.defaultVolume },
    val activeChannels: Set<AmbientSoundChannel> = setOf(AmbientSoundChannel.RAIN)
)

object AmbientSoundMixer {

    private val _mixerState = MutableStateFlow(AmbientMixerState())
    val mixerState: StateFlow<AmbientMixerState> = _mixerState.asStateFlow()

    @Volatile
    private var isEngineRunning = false
    private var audioTrack: AudioTrack? = null
    private var audioThread: Thread? = null

    fun setChannelActive(channel: AmbientSoundChannel, active: Boolean) {
        val current = _mixerState.value.activeChannels.toMutableSet()
        if (active) current.add(channel) else current.remove(channel)
        _mixerState.value = _mixerState.value.copy(activeChannels = current)
    }

    fun setChannelVolume(channel: AmbientSoundChannel, volume: Float) {
        val current = _mixerState.value.volumes.toMutableMap()
        current[channel] = volume.coerceIn(0f, 1f)
        _mixerState.value = _mixerState.value.copy(volumes = current)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startMixer() {
        if (isEngineRunning) return
        isEngineRunning = true
        _mixerState.value = _mixerState.value.copy(isPlaying = true)

        audioThread = thread(start = true) {
            val sampleRate = 44100
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(sampleRate)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .build()

            audioTrack?.play()

            val buffer = ShortArray(bufferSize)
            var sampleIndex = 0L
            var lastBrownL = 0.0
            var lastBrownR = 0.0

            while (isEngineRunning) {
                val state = _mixerState.value
                val active = state.activeChannels
                val vols = state.volumes

                val rainVol = if (active.contains(AmbientSoundChannel.RAIN)) vols[AmbientSoundChannel.RAIN] ?: 0f else 0f
                val brownVol = if (active.contains(AmbientSoundChannel.BROWN_NOISE)) vols[AmbientSoundChannel.BROWN_NOISE] ?: 0f else 0f
                val whiteVol = if (active.contains(AmbientSoundChannel.WHITE_NOISE)) vols[AmbientSoundChannel.WHITE_NOISE] ?: 0f else 0f
                val oceanVol = if (active.contains(AmbientSoundChannel.OCEAN_WAVES)) vols[AmbientSoundChannel.OCEAN_WAVES] ?: 0f else 0f
                val cafeVol = if (active.contains(AmbientSoundChannel.CAFE_HUM)) vols[AmbientSoundChannel.CAFE_HUM] ?: 0f else 0f

                for (i in 0 until buffer.size step 2) {
                    val t = sampleIndex.toDouble() / sampleRate
                    val rawWhiteL = (Math.random() * 2.0 - 1.0)
                    val rawWhiteR = (Math.random() * 2.0 - 1.0)

                    
                    val rainSample = rawWhiteL * 0.25 * rainVol

                    
                    lastBrownL = (lastBrownL + (0.02 * rawWhiteL)) / 1.02
                    lastBrownR = (lastBrownR + (0.02 * rawWhiteR)) / 1.02
                    val brownSample = lastBrownL * 3.5 * brownVol

                    
                    val whiteSample = rawWhiteL * 0.15 * whiteVol

                    
                    val swell = (sin(2.0 * Math.PI * 0.08 * t) + 1.0) * 0.5
                    val oceanSample = lastBrownL * 2.5 * swell * oceanVol

                    
                    val cafeSample = (sin(2.0 * Math.PI * 180.0 * t) + sin(2.0 * Math.PI * 340.0 * t)) * 0.08 * cafeVol

                    val mixedL = (rainSample + brownSample + whiteSample + oceanSample + cafeSample).coerceIn(-1.0, 1.0)
                    val mixedR = (rainSample + lastBrownR * 3.5 * brownVol + rawWhiteR * 0.15 * whiteVol + oceanSample + cafeSample).coerceIn(-1.0, 1.0)

                    buffer[i] = (mixedL * 32767).toInt().toShort()
                    buffer[i + 1] = (mixedR * 32767).toInt().toShort()
                    sampleIndex++
                }
                audioTrack?.write(buffer, 0, buffer.size)
            }
        }
    }

    fun stopMixer() {
        isEngineRunning = false
        _mixerState.value = _mixerState.value.copy(isPlaying = false)
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {
        }
        audioTrack = null
        audioThread = null
    }

    fun toggleMixer() {
        if (_mixerState.value.isPlaying) {
            stopMixer()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMixer()
        }
    }
}
