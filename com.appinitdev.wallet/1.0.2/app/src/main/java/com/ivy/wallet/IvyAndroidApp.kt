@HiltAndroidApp
class IvyAndroidApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    override fun onCreate() {
        super.onCreate()
        appContext = this
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }
}
