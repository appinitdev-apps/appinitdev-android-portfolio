
object NfcFocusHandler {

    fun handleNfcIntent(intent: Intent?, timerManager: TimerManager): Boolean {
        if (intent == null) return false
        val action = intent.action
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == action ||
            NfcAdapter.ACTION_TAG_DISCOVERED == action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == action
        ) {
            timerManager.start()
            return true
        }
        return false
    }
}
