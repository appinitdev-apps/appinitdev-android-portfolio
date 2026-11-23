
object SocialAndHaptics {

    fun triggerMoveHaptic(haptic: HapticFeedback?, context: Context? = null) {
        val isHapticEnabled = context?.let { loadAppSettings(it).isHapticEnabled } ?: true
        if (isHapticEnabled) {
            haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        SoundEffectsManager.playMoveSound()
    }

    fun triggerVictoryHaptic(haptic: HapticFeedback?, context: Context? = null) {
        val isHapticEnabled = context?.let { loadAppSettings(it).isHapticEnabled } ?: true
        if (isHapticEnabled) {
            haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        SoundEffectsManager.playVictorySound()
    }

    fun shareVictoryText(context: Context, gameModeOrTitle: String, moves: Int, timeSeconds: Int) {
        val minutes = timeSeconds / 60
        val seconds = timeSeconds % 60
        val timeFormatted = "%02d:%02d".format(minutes, seconds)

        val shareText = """
            🧩 ¡Acabo de completar un Rompecabezas en Puzzle App!
            
            🎮 Modo: $gameModeOrTitle
            ⏱️ Tiempo: $timeFormatted
            🎯 Movimientos: $moves
            
            ¿Puedes superarme? ¡Descarga la app y ponte a prueba! 🚀
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        context.startActivity(Intent.createChooser(intent, "Compartir victoria"))
    }
}
