
 


object NotesSyncManager {
    private const val TAG = "NotesSyncManager"
    private const val SYNC_DELAY_MS = 2000L 

    private val handler = Handler(Looper.getMainLooper())
    private var pendingSyncRunnable: Runnable? = null
    private var isSyncing = false

    private fun notifyStatusOnMain(onSyncStatusChange: ((SyncStatus) -> Unit)?, status: SyncStatus) {
        if (onSyncStatusChange == null) return
        handler.post {
            try {
                onSyncStatusChange.invoke(status)
            } catch (e: Exception) {
                Log.w(TAG, "Exception while invoking status callback", e)
            }
        }
    }

     


    fun canSyncNotes(context: Context): Boolean {
        val isSignedIn = false
        if (!isSignedIn) return false

        val proPref = ProVersion(context)
        val proPlusPreference = ProPlusVersion(context)
        val hasPro = proPref.getValue() == 100
        val hasProPlus = proPlusPreference.getValue() == 100

        return hasPro || hasProPlus
    }

     



    fun requestSync(context: Context, onSyncStatusChange: ((SyncStatus) -> Unit)? = null) {
        
        pendingSyncRunnable?.let { handler.removeCallbacks(it) }

        
        if (!canSyncNotes(context)) {
            notifyStatusOnMain(onSyncStatusChange, SyncStatus.NOT_ELIGIBLE)
            return
        }

        
        if (isSyncing) {
            notifyStatusOnMain(onSyncStatusChange, SyncStatus.SYNCING)
            return
        }

        
        pendingSyncRunnable = Runnable {
            performSync(context, onSyncStatusChange)
        }
        handler.postDelayed(pendingSyncRunnable!!, SYNC_DELAY_MS)
    }

     


    private fun performSync(context: Context, onSyncStatusChange: ((SyncStatus) -> Unit)?) {
        isSyncing = true
        notifyStatusOnMain(onSyncStatusChange, SyncStatus.SYNCING)
        val uid = AuthManager.getUid()
        if (uid == null) {
            Log.w(TAG, "User UID is null, cannot sync")
            isSyncing = false
            notifyStatusOnMain(onSyncStatusChange, SyncStatus.NOT_ELIGIBLE)
            return
        }

        
        val notesPref = NotesPreference(context)
        val notes = notesPref.getValue()

        

    }

     


    fun cancelPendingSync() {
        pendingSyncRunnable?.let { handler.removeCallbacks(it) }
        pendingSyncRunnable = null
    }

    enum class SyncStatus {
        NOT_ELIGIBLE,  
        SYNCING,       
        SYNCED,        
        ERROR          
    }
}