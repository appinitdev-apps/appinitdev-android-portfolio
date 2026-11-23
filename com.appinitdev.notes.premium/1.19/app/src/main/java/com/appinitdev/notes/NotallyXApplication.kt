
class NotallyXApplication : Application(), Application.ActivityLifecycleCallbacks {

    private lateinit var biometricLockObserver: Observer<BiometricLock>
    private lateinit var preferences: NotallyXPreferences
    private var unlockReceiver: UnlockReceiver? = null

    val locked = NotNullLiveData(true)

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        if (isTestRunner()) return
        com.appinitdev.notes.utils.clean.CleanTrashWorker.scheduleAutoTrashClean(this)
        preferences = NotallyXPreferences.Companion.getInstance(this)
        if (preferences.useDynamicColors.value) {
            if (DynamicColors.isDynamicColorAvailable()) {
                DynamicColors.applyToActivitiesIfAvailable(this)
            }
        } else {
            setTheme(R.style.AppTheme)
        }
        preferences.theme.observeForeverWithPrevious { (oldTheme, theme) ->
            when (theme) {
                Theme.DARK ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

                Theme.LIGHT ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

                Theme.FOLLOW_SYSTEM ->
                    AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    )
            }
            if (oldTheme != null) {
                WidgetProvider.Companion.updateWidgets(this, locked = locked.value)
            }
        }

        preferences.backupsFolder.observeForeverWithPrevious { (backupFolderBefore, backupFolder) ->
            checkUpdatePeriodicBackup(
                backupFolderBefore,
                backupFolder,
                preferences.periodicBackups.value.periodInDays.toLong(),
            )
        }
        preferences.periodicBackups.observeForever { value ->
            val backupFolder = preferences.backupsFolder.value
            checkUpdatePeriodicBackup(backupFolder, backupFolder, value.periodInDays.toLong())
        }

        val filter = IntentFilter().apply { addAction(Intent.ACTION_SCREEN_OFF) }
        biometricLockObserver = Observer { biometricLock ->
            if (biometricLock == BiometricLock.ENABLED) {
                unlockReceiver = UnlockReceiver(this)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    registerReceiver(unlockReceiver, filter, RECEIVER_NOT_EXPORTED)
                } else {
                    registerReceiver(unlockReceiver, filter)
                }
            } else {
                unlockReceiver?.let { unregisterReceiver(it) }
                if (locked.value) {
                    locked.postValue(false)
                }
            }
        }
        preferences.biometricLock.observeForever(biometricLockObserver)

        locked.observeForever { isLocked -> WidgetProvider.Companion.updateWidgets(this, locked = isLocked) }

        preferences.backupPassword.observeForeverWithPrevious {
            (previousBackupPassword, backupPassword) ->
            if (preferences.backupOnSave.value) {
                val backupPath = preferences.backupsFolder.value
                if (backupPath != NotallyXPreferences.Companion.EMPTY_PATH) {
                    if (
                        !modifiedNoteBackupExists(backupPath) ||
                            (previousBackupPassword != null &&
                                previousBackupPassword != backupPassword)
                    ) {
                        deleteModifiedNoteBackup(backupPath)
                        MainScope().launch {
                            withContext(Dispatchers.IO) {
                                autoBackupOnSave(
                                    backupPath,
                                    savedNote = null,
                                    password = backupPassword,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkUpdatePeriodicBackup(
        backupFolderBefore: String?,
        backupFolder: String,
        periodInDays: Long,
    ) {
        val workManager = getWorkManagerSafe() ?: return
        workManager.getWorkInfosForUniqueWorkLiveData(AUTO_BACKUP_WORK_NAME).observeOnce { workInfos
            ->
            if (backupFolder == NotallyXPreferences.Companion.EMPTY_PATH || periodInDays < 1) {
                if (workInfos?.containsNonCancelled() == true) {
                    workManager.cancelAutoBackup()
                }
            } else if (
                workInfos.isNullOrEmpty() ||
                    workInfos.all { it.state == WorkInfo.State.CANCELLED } ||
                    (backupFolderBefore != null && backupFolderBefore != backupFolder)
            ) {
                workManager.scheduleAutoBackup(this, periodInDays)
            } else if (
                workInfos.first().periodicityInfo?.isEqualTo(periodInDays, TimeUnit.DAYS) == false
            ) {
                workManager.updateAutoBackup(workInfos, periodInDays)
            }
        }
    }

    private fun getWorkManagerSafe(): WorkManager? {
        return try {
            WorkManager.getInstance(this)
        } catch (e: Exception) {
            // TODO: Happens when ErrorActivity is launched
            null
        }
    }

    companion object {
        private fun isTestRunner(): Boolean {
            return Build.FINGERPRINT.equals("robolectric", ignoreCase = true)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activity.setEnabledSecureFlag(preferences.secureFlag.value)
    }

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}
}