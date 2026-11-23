
object SoundEffectsManager {

    private var toneGenerator: ToneGenerator? = null
    var isSoundEnabled: Boolean = true

    fun init(context: Context) {
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            }
        } catch (e: Exception) {
            Log.e("SoundEffectsManager", "Error initializing ToneGenerator: ${e.message}")
        }
    }

    fun playMoveSound() {
        if (!isSoundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
        } catch (e: Exception) {
            Log.e("SoundEffectsManager", "Error playing move sound: ${e.message}")
        }
    }

    fun playShuffleSound() {
        if (!isSoundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 80)
        } catch (e: Exception) {
            Log.e("SoundEffectsManager", "Error playing shuffle sound: ${e.message}")
        }
    }

    fun playVictorySound() {
        if (!isSoundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400)
        } catch (e: Exception) {
            Log.e("SoundEffectsManager", "Error playing victory sound: ${e.message}")
        }
    }

    fun playGameOverSound() {
        if (!isSoundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_LOW_PBX_L, 300)
        } catch (e: Exception) {
            Log.e("SoundEffectsManager", "Error playing game over sound: ${e.message}")
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            Log.e("SoundEffectsManager", "Error releasing ToneGenerator: ${e.message}")
        }
    }
}
