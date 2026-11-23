class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder()
                    .setSizeBytes(100 * 1024 * 1024) 
                    .build())
                .build()
            FirebaseFirestore.getInstance().firestoreSettings = settings
        } catch (e: Exception) {
        }
        try {
            solicitarPermisoNotificaciones(this)
            requestIgnoreBatteryOptimizations(this)
        } catch (e: Exception) {
        } finally {
            try {
                programarNotificacionDiaria(this)
            } catch (e: Exception) {
            }
        }
        MobileAds.initialize(this) {}
        val appIdFromNotification = intent.getStringExtra("appId")
        setContent {
            TestersTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        viewModel = authViewModel,
                        modifier = Modifier.padding(innerPadding),
                        startAppId = appIdFromNotification
                    )
                }
            }
        }
    }
    private fun requestIgnoreBatteryOptimizations(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = context.packageName
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent().apply {
                    action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    data = Uri.parse("package:$packageName")
                }
                context.startActivity(intent)
            }
        }
    }
}
