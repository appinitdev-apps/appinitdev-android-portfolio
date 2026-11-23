enum class SoundType {
    NONE,
    WHITE_NOISE,
    BROWN_NOISE,
    PINK_NOISE
}
object AmbientSoundPlayer {
    @Volatile
    private var isPlaying = false
    private var audioTrack: AudioTrack? = null
    private var playThread: Thread? = null
    @RequiresApi(Build.VERSION_CODES.O)
    fun startSound(soundType: SoundType) {
        if (soundType == SoundType.NONE) {
            stopSound()
            return
        }
        stopSound()
        isPlaying = true
        playThread = thread(start = true) {
            val sampleRate = 44100
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
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
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .build()
            audioTrack?.play()
            val buffer = ShortArray(bufferSize)
            val random = Random()
            var lastBrownSample = 0.0
            while (isPlaying) {
                for (i in buffer.indices) {
                    val sample: Double = when (soundType) {
                        SoundType.WHITE_NOISE -> {
                            random.nextDouble() * 2.0 - 1.0
                        }
                        SoundType.BROWN_NOISE -> {
                            val white = random.nextDouble() * 2.0 - 1.0
                            lastBrownSample = (lastBrownSample + (0.02 * white)) / 1.02
                            lastBrownSample * 3.5
                        }
                        SoundType.PINK_NOISE -> {
                            val white = random.nextDouble() * 2.0 - 1.0
                            lastBrownSample = (lastBrownSample + (0.05 * white)) / 1.05
                            lastBrownSample * 2.5
                        }
                        else -> 0.0
                    }
                    val clamped = sample.coerceIn(-1.0, 1.0)
                    buffer[i] = (clamped * 32767).toInt().toShort()
                }
                audioTrack?.write(buffer, 0, buffer.size)
            }
        }
    }
    fun stopSound() {
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
