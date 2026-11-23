@RequiresApi(Build.VERSION_CODES.N)
class GoodtimeTileService : TileService(), KoinComponent {
    private val timerManager: TimerManager by inject()
    private val timeProvider: TimeProvider by inject()
    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }
    override fun onClick() {
        super.onClick()
        val data = timerManager.timerData.value
        if (data.state == TimerState.RESET) {
            timerManager.start()
        } else {
            timerManager.toggle()
        }
        updateTileState()
    }
    private fun updateTileState() {
        val tile = qsTile ?: return
        val data = timerManager.timerData.value
        if (data.state == TimerState.RUNNING) {
            val remainingMs = data.getBaseTime(timeProvider)
            val remainingSec = (remainingMs / 1000).coerceAtLeast(0)
            tile.state = Tile.STATE_ACTIVE
            tile.label = "Focus: ${formatTime(remainingSec)}"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "Running"
            }
        } else if (data.state == TimerState.PAUSED) {
            tile.state = Tile.STATE_INACTIVE
            tile.label = "Focus: Paused"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "Tap to Resume"
            }
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = "Goodtime Focus"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "Tap to Start"
            }
        }
        tile.updateTile()
    }
    private fun formatTime(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }
}
