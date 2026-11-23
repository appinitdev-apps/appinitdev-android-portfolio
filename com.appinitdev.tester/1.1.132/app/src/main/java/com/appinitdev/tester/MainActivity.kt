
class MainActivity : ComponentActivity() {

    // Inicializamos el ViewModel aquí para que viva durante todo el ciclo de la Activity
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)

        // Configuración de Firestore para soporte Offline (Base de Datos Local)
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder()
                    .setSizeBytes(100 * 1024 * 1024) // 100 MB de caché local
                    .build())
                .build()
            FirebaseFirestore.getInstance().firestoreSettings = settings
        } catch (e: Exception) {
            // Ya inicializado o error
        }

        try {
            solicitarPermisoNotificaciones(this)
            requestIgnoreBatteryOptimizations(this)
        } catch (e: Exception) {
            // Manejar error si es necesario
        } finally {
            // 2. Invocar el nuevo sistema de WorkManager
            try {
                programarNotificacionDiaria(this)
            } catch (e: Exception) {
                // Manejar error
            }

            // 3. (Opcional) Puedes eliminar o comentar la alarma vieja para evitar duplicados
            // programarAlarmaDiaria(this)
        }
        MobileAds.initialize(this) {}

        val appIdFromNotification = intent.getStringExtra("appId")

        setContent {
            TestersTheme {
                // Usamos Scaffold para manejar correctamente los espacios (Edge-to-Edge)
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Llamamos a tu componente de navegación ya existente
                    AppNavigation(
                        viewModel = authViewModel,
                        // El modificador aplica el padding del Scaffold para evitar que el UI
                        // quede tapado por las barras del sistema.
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

