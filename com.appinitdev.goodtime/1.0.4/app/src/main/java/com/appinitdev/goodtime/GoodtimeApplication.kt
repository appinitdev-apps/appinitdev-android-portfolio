class GoodtimeApplication :
    Application(),
    KoinComponent,
    Configuration.Provider {
    private val applicationScope = MainScope()
    override fun onCreate() {
        super.onCreate()
        if (ACRA.isACRASenderServiceProcess()) return
        startKoin {
            modules(
                module {
                    single<Context> { this@GoodtimeApplication }
                    single<ActivityResultLauncherManager> {
                        ActivityResultLauncherManager(
                            get(),
                            coroutineScope = get<CoroutineScope>(named(IO_SCOPE)),
                        )
                    }
                    single<BackupPrompter> {
                        AndroidBackupPrompter(get())
                    }
                    single {
                        ChecklistManager(get())
                    }
                    single {
                        com.apps.adrcotfas.goodtime.stats.FocusGardenManager(get())
                    }
                    single {
                        com.apps.adrcotfas.goodtime.bl.media.MediaControlManager(get())
                    }
                    single {
                        com.apps.adrcotfas.goodtime.bl.sensors.FlipToFocusManager(get(), get())
                    }
                    single {
                        InterruptionReasonTracker(get<Context>())
                    }
                    single {
                        FocusRoomManager()
                    }
                    single {
                        GoogleDriveBackupManager(get<Context>())
                    }
                    single<NotificationArchManager> {
                        NotificationArchManager(
                            get<Context>(),
                            MainActivity::class.java,
                        )
                    }
                    single<EventListener>(named(EventListener.TIMER_SERVICE_HANDLER)) {
                        TimerServiceStarter(get())
                    }
                    single<EventListener>(named(EventListener.ALARM_MANAGER_HANDLER)) {
                        AlarmManagerHandler(
                            get<Context>(),
                            get<TimeProvider>(),
                            getWith("AlarmManagerHandler"),
                        )
                    }
                    single<ReminderHelper> {
                        ReminderHelper(
                            get(),
                            get(),
                            getWith("ReminderHelper"),
                        )
                    }
                    viewModel<SoundsViewModel> {
                        SoundsViewModel(
                            settingsRepository = get(),
                        )
                    }
                    single {
                        SoundPlayer(
                            context = get(),
                            ioScope = get<CoroutineScope>(named(IO_SCOPE)),
                            playerScope = get<CoroutineScope>(named(WORKER_SCOPE)),
                            settingsRepo = get(),
                            logger = getWith("SoundPlayer"),
                        )
                    }
                    single {
                        VibrationPlayer(
                            context = get(),
                            playerScope = get<CoroutineScope>(named(WORKER_SCOPE)),
                            ioScope = get<CoroutineScope>(named(IO_SCOPE)),
                            settingsRepo = get(),
                        )
                    }
                    single {
                        TorchManager(
                            context = get(),
                            ioScope = get<CoroutineScope>(named(IO_SCOPE)),
                            playerScope = get<CoroutineScope>(named(WORKER_SCOPE)),
                            settingsRepo = get(),
                            logger = getWith("TorchManager"),
                        )
                    }
                    single<EventListener>(named(EventListener.SOUND_AND_VIBRATION_PLAYER)) {
                        SoundVibrationAndTorchPlayer(
                            soundPlayer = get(),
                            vibrationPlayer = get(),
                            torchManager = get(),
                        )
                    }
                    single<EventListener>(named(EventListener.SESSION_RESET_HANDLER)) {
                        SessionResetHandler(get(), getWith("SessionResetHandler"))
                    }
                    single<EventListener>(named(EventListener.DND_MODE_MANAGER)) {
                        DndModeManager(
                            notificationManager = get<NotificationArchManager>(),
                            settingsRepository = get<SettingsRepository>(),
                            coroutineScope = get<CoroutineScope>(named(IO_SCOPE)),
                        )
                    }
                    single(createdAtStart = true) {
                        AutoBackupManager(
                            context = get(),
                            settingsRepository = get<SettingsRepository>(),
                            logger = getWith("AutoBackupManager"),
                        )
                    }
                    worker {
                        AutoBackupWorker(
                            get(),
                            get(),
                            get(),
                            getWith("AutoBackupWorker"),
                            get<String>(named(DB_PATH_KEY)),
                            get(),
                        )
                    }
                },
                flavorModule,
                coroutineScopeModule,
                platformModule,
                coreModule,
                localDataModule,
                timerManagerModule,
                viewModelModule,
                mainModule,
            )
            workManagerFactory()
        }
        val reminderHelper = get<ReminderHelper>()
        applicationScope.launch {
            reminderHelper.init()
        }
        val billing = get<BillingAbstract>()
        billing.init()
    }
    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(context)
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            throwable.printStackTrace()  
        }
        initAcra {
            alsoReportToAndroidFramework = true
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON
            notification {
                title = getString(R.string.main_crash_notification_title)
                text = getString(R.string.main_crash_notification_desc)
                channelName = getString(R.string.main_crash_channel_name)
                resSendButtonIcon = null
                resDiscardButtonIcon = null
            }
            mailSender {
                mailTo = getString(R.string.contact_address)
                subject = getString(R.string.crash_report_title)
                reportFileName = "crash.txt"
            }
        }
    }
    override val workManagerConfiguration: Configuration
        get() =
            if (BuildConfig.DEBUG) {
                Configuration
                    .Builder()
                    .setMinimumLoggingLevel(android.util.Log.DEBUG)
                    .build()
            } else {
                Configuration
                    .Builder()
                    .setMinimumLoggingLevel(android.util.Log.ERROR)
                    .build()
            }
}
