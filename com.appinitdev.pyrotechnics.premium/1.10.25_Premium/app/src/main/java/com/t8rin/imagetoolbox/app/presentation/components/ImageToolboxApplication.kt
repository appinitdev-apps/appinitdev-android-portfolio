 


@HiltAndroidApp
class ImageToolboxApplication : ComposeApplication() {

    @Inject
    lateinit var keepAliveService: KeepAliveService

    @Inject
    lateinit var appScope: AppScope

    @Inject
    lateinit var httpClient: HttpClient

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    private var isSetupCompleted: Boolean = false

    override fun onCreate() {
        super.onCreate()
        runSetup()
    }

    override fun runSetup() {
        if (isSetupCompleted) return

        if (isMain()) {
            setupFlags()
            initAppContext()
            initOpenCV()
            initNeuralTool()
            initColorNames()
            initQrScanner()
            attachLogWriter()
            applyGlobalExceptionHandler()
            registerSecurityProviders()
            initPdfBox()
            injectBaseComponent()
            initCollages()

            isSetupCompleted = true
        }
    }

}