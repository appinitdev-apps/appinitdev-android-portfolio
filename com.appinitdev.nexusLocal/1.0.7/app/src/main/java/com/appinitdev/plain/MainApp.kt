class MainApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        Security.addProvider(BouncyCastleProvider())
        initDataStore(dataStore)
        initDatabase(
            androidx.room.Room.databaseBuilder(this, AppDatabase::class.java, Constants.DATABASE_NAME)
                .addMigrations(Migrations.MIGRATION_5_6)
                .addCallback(object : androidx.room.RoomDatabase.Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        DataInitializer(this@MainApp, db).apply {
                            insertWelcome()
                            insertTags()
                            insertNotes()
                        }
                    }
                })
                .build()
        )
        CrashHandler.install(this)
        SingletonImageLoader.setSafe { context ->
            newImageLoader(context)
        }
        LogCat.addLogAdapter(
            DiskLogAdapter(
                DiskLogFormatStrategy.getInstance(this),
                minPriority = if (BuildConfig.DEBUG) LogCat.VERBOSE else LogCat.WARN,
            ),
        )
        AppEvents.register()
        HttpServerManager.warmUp()
        NetworkMonitor.init(this)
        if (isUPlus()) {
            ZipPathValidator.clearCallback()
        }
        try {
            val manager = getSystemService(TextClassificationManager::class.java)
            manager?.setTextClassifier(TextClassifier.NO_OP)
        } catch (_: Throwable) {
        }
        coIO {
            val preferences = getPreferencesAsync()
            TempData.webEnabled.value = WebPreference.get(preferences)
            TempData.webHttps.value = HttpsPreference.get(preferences)
            TempData.httpPort.value = HttpPortPreference.get(preferences)
            TempData.httpsPort.value = HttpsPortPreference.get(preferences)
            TempData.audioPlayMode.value = AudioPlayModePreference.getValue(preferences)
            AdbTokenPreference.ensureValueAsync(preferences)
            TempData.nearbyDiscoverable = NearbyDiscoverablePreference.getAsync()
            val updateInfo = UpdateInfoPreference.getValueAsync()
            val checkUpdateTime = updateInfo.checkUpdateTime
            val autoCheckUpdate = updateInfo.autoCheckUpdate
            ClientIdPreference.ensureValueAsync(preferences)
            TempData.deviceName.value = DeviceNamePreference.get(preferences).ifEmpty { PhoneHelper.getDeviceName(instance) }
            KeyStorePasswordPreference.ensureValueAsync(preferences)
            UrlTokenPreference.ensureValueAsync(preferences)
            SignatureKeyPreference.ensureKeyPairAsync(preferences)
            MdnsHostnamePreference.ensureValueAsync(preferences)
            DarkThemePreference.setDarkMode(DarkTheme.parse(DarkThemePreference.get(preferences)))
            if (TempData.webEnabled.value && PlugInControlReceiver.isUSBConnected(this@MainApp)) {
                sendEvent(PowerConnectedEvent())
            }
            if (PasswordPreference.get(preferences).isEmpty()) {
                HttpServerManager.resetPasswordAsync()
            }
            HttpServerManager.loadTokenCache()
            ChatCacheManager.loadKeyCacheAsync()
            if (!FidUriExtMigratedPreference.get(preferences)) {
                ChatFidUriMigration.run(this@MainApp)
                FidUriExtMigratedPreference.putAsync(true)
            }
            if (FeedAutoRefreshPreference.get(preferences)) {
                FeedFetchWorker.startRepeatWorkerAsync(instance)
            }
            sendEvent(StartNearbyServiceEvent())
            HttpServerManager.clientTsInterval()
            ImageSearchManager.restoreIfEnabled()
            val thirtyDaysAgo = (kotlin.time.Clock.System.now() - 30.days).toString()
            AppDatabase.instance.videoPlayProgressDao().getRecentProgress(thirtyDaysAgo).forEach {
                TempData.videoPlayProgressMap[it.mediaId] = it.duration
            }
        }
    }
    companion object {
        lateinit var instance: MainApp
        fun getAppVersion(): String {
            return BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")"
        }
        fun getAndroidVersion(): String {
            return Build.VERSION.RELEASE + " (" + Build.VERSION.SDK_INT + ")"
        }
    }
}
