enum class BinauralWaveMode(
    val title: String,
    val baseFreq: Double,
    val beatFreq: Double,
    val description: String
) {
    BETA_FOCUS("Beta Waves (Focus)", 200.0, 15.0, "15 Hz difference - Boosts alertness and concentration"),
    ALPHA_RELAX("Alpha Waves (Relax)", 200.0, 10.0, "10 Hz difference - Calms mind during break periods"),
    THETA_CREATIVITY("Theta Waves (Creativity)", 150.0, 6.0, "6 Hz difference - Stimulates creative thinking"),
    DELTA_REST("Delta Waves (Deep Rest)", 100.0, 2.5, "2.5 Hz difference - Promotes deep rest")
}
object BinauralBeatsGenerator {
    @Volatile
    private var isPlaying = false
    private var audioTrack: AudioTrack? = null
    private var playThread: Thread? = null
    @RequiresApi(Build.VERSION_CODES.O)
    fun startBinauralBeats(mode: BinauralWaveMode) {
        if (isPlaying) stopBinauralBeats()
        isPlaying = true
        playThread = thread(start = true) {
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
            val leftFreq = mode.baseFreq
            val rightFreq = mode.baseFreq + mode.beatFreq
            val buffer = ShortArray(bufferSize)
            var sampleIndex = 0L
            while (isPlaying) {
                for (i in 0 until buffer.size step 2) {
                    val t = sampleIndex.toDouble() / sampleRate
                    val leftVal = Math.sin(2.0 * Math.PI * leftFreq * t) * 0.3
                    val rightVal = Math.sin(2.0 * Math.PI * rightFreq * t) * 0.3
                    buffer[i] = (leftVal * 32767).toInt().toShort()     
                    buffer[i + 1] = (rightVal * 32767).toInt().toShort() 
                    sampleIndex++
                }
                audioTrack?.write(buffer, 0, buffer.size)
            }
        }
    }
    fun stopBinauralBeats() {
        isPlaying = false
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {
        }
        audioTrack = null
        playThread = null
    }
    fun isPlaying(): Boolean = isPlaying
}
