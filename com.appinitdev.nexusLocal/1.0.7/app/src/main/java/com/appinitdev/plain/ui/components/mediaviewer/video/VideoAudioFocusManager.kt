class VideoAudioFocusManager(private val audioManager: AudioManager) {
    private var focusRequest: AudioFocusRequest? = null
    fun requestFocus(player: ExoPlayer) {
        focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            .setOnAudioFocusChangeListener { change ->
                when (change) {
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> player.pause()
                    AudioManager.AUDIOFOCUS_GAIN -> player.play()
                    AudioManager.AUDIOFOCUS_LOSS -> player.stop()
                }
            }
            .build()
        focusRequest = req
        audioManager.requestAudioFocus(req)
    }
    fun abandonFocus() {
        focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        focusRequest = null
    }
}
