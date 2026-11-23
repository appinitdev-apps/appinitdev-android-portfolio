
class PaymentTileService : TileService() {
    
    override fun onTileAdded() {
        super.onTileAdded()
    }

    
    override fun onStartListening() {
        super.onStartListening()
    }

    
    override fun onStopListening() {
        super.onStopListening()
    }

    
    override fun onClick() {
        super.onClick()

        startRootActivity()
    }

    
    override fun onTileRemoved() {
        super.onTileRemoved()
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    private fun startRootActivity() {
        val intent = Intent(applicationContext, RootActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            startActivityAndCollapse(pi)
        } else {
            startActivityAndCollapse(intent)
        }
    }
}
