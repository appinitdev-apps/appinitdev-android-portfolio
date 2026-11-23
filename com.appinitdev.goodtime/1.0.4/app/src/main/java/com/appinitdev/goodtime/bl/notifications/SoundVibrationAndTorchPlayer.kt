 

















class SoundVibrationAndTorchPlayer(
    private val soundPlayer: SoundPlayer,
    private val vibrationPlayer: VibrationPlayer,
    private val torchManager: TorchManager,
) : EventListener {
    override fun onEvent(event: Event) {
        when (event) {
            is Event.Start -> {
                if (!event.autoStarted) {
                    soundPlayer.stop()
                    vibrationPlayer.stop()
                    torchManager.stop()
                }
            }

            is Event.Finished -> {
                soundPlayer.play(event.type)
                vibrationPlayer.start()
                torchManager.start()
            }

            Event.Reset -> {
                soundPlayer.stop()
                vibrationPlayer.stop()
                torchManager.stop()
            }

            else -> {
                
            }
        }
    }
}
