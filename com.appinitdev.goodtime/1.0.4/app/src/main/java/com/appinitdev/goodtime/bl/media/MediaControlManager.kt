
class MediaControlManager(context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun playPause() {
        sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
    }

    fun skipNext() {
        sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT)
    }

    fun skipPrevious() {
        sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
    }

    fun pausePlayback() {
        if (audioManager.isMusicActive) {
            sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PAUSE)
        }
    }

    private fun sendMediaKeyEvent(keyCode: Int) {
        val eventDown = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val eventUp = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        audioManager.dispatchMediaKeyEvent(eventDown)
        audioManager.dispatchMediaKeyEvent(eventUp)
    }
}
