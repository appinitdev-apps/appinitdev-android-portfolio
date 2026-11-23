object TickingSoundPlayer {
    @Volatile
    private var isPlaying = false
    private var audioTrack: AudioTrack? = null
    private var playThread: Thread? = null
    @RequiresApi(Build.VERSION_CODES.O)
    fun startTicking(bpm: Int = 60) {
        if (isPlaying) stopTicking()
        isPlaying = true
        playThread = thread(start = true) {
            val sampleRate = 44100
            val intervalMs = (60.0 / bpm * 1000).toInt()
            val tickLengthSamples = (sampleRate * 0.015).toInt() 
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(sampleRate * 2)
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
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
            val tickBuffer = ShortArray(tickLengthSamples)
            for (i in tickBuffer.indices) {
                val decay = 1.0 - (i.toDouble() / tickLengthSamples)
                val freq = 1200.0 
                val sample = Math.sin(2.0 * Math.PI * freq * i / sampleRate) * decay * 0.4
                tickBuffer[i] = (sample * 32767).toInt().toShort()
            }
            while (isPlaying) {
                audioTrack?.write(tickBuffer, 0, tickBuffer.size)
                try {
                    Thread.sleep(intervalMs.toLong())
                } catch (_: InterruptedException) {
                    break
                }
            }
        }
    }
    fun stopTicking() {
        isPlaying = false
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {
        }
        audioTrack = null
        playThread = null
    }
    fun isTicking(): Boolean = isPlaying
}
