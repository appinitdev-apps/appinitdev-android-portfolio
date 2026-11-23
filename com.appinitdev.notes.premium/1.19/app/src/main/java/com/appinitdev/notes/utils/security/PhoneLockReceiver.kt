
class UnlockReceiver(private val application: NotallyXApplication) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            application.locked.value = true
        }
    }
}
