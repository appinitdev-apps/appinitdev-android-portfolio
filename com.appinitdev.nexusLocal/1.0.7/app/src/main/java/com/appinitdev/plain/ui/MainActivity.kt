
class MainActivity : AppCompatActivity() {
    internal var pickFileType = PickFileType.IMAGE
    internal var pickFileTag = PickFileTag.SEND_MESSAGE
    internal var exportFileType = ExportFileType.OPML
    internal var pendingLoginEvent by mutableStateOf<ConfirmToAcceptLoginEvent?>(null)
    internal var pendingPairingEvent by mutableStateOf<PairingRequestReceivedEvent?>(null)
    internal var pendingChannelInviteEvent by mutableStateOf<ChannelInviteReceivedEvent?>(null)
    internal val mainVM: MainViewModel by viewModels()
    internal val audioPlaylistVM: AudioPlaylistViewModel by viewModels()
    val pomodoroVM: PomodoroViewModel by viewModels()
    internal val peerVM: PeerViewModel by viewModels()
    internal val channelVM: ChannelViewModel by viewModels()
    internal val chatVM: ChatViewModel by viewModels()
    internal val navControllerState = mutableStateOf<NavHostController?>(null)
    internal var showForwardTargetDialog by mutableStateOf(false)
    internal var pendingFileUris by mutableStateOf<Set<Uri>?>(null)
    internal var pendingForwardText by mutableStateOf<String?>(null)
    internal var pendingCrashReport by mutableStateOf<String?>(null)

    internal val screenCapture = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null && ScreenMirrorService.instance == null) {
            ContextCompat.startForegroundService(
                this, Intent(this, ScreenMirrorService::class.java)
                    .putExtra("code", result.resultCode).putExtra("data", result.data)
            )
        }
    }
    internal val recordAudioForMirror = registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
        try {
            screenCapture.launch(com.appinitdev.nexusLocal.mediaProjectionManager.createScreenCaptureIntent())
        } catch (e: IllegalStateException) {
            LogCat.e("Error launching screen capture: ${e.message}")
        }
    }
    internal val recordAudioForMirrorLate = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            sendScreenMirrorAudioStatus(true)
        } else if (!shouldShowRequestPermissionRationale(android.Manifest.permission.RECORD_AUDIO)
            && !Permission.RECORD_AUDIO.can(this)
        ) {
            showRecordAudioPermissionSettingsGuide()
        } else {
            sendScreenMirrorAudioStatus(false)
        }
    }
    internal val appDetailsSettingsForAudioLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        sendScreenMirrorAudioStatus(Permission.RECORD_AUDIO.can(this))
    }
    internal val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) sendEvent(PickFileResultEvent(pickFileTag, pickFileType, setOf(uri)))
    }
    internal val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            if (uris.isNotEmpty()) {
                sendEvent(PickFileResultEvent(pickFileTag, pickFileType, uris.toSet()))
            }
        }
    internal val pickFileActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uris = result.data?.let { FilePickHelper.getUris(it) } ?: emptySet()
        if (uris.isNotEmpty()) {
            sendEvent(PickFileResultEvent(pickFileTag, pickFileType, uris))
        }
    }
    internal val exportFileActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data
        if (uri != null) {
            sendEvent(ExportFileResultEvent(exportFileType, uri))
        }
    }
    internal val ignoreBatteryOptimizationActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        sendEvent(IgnoreBatteryOptimizationResultEvent())
    }

    private val plugInReceiver = PlugInControlReceiver()
    private val networkStateReceiver = NetworkStateReceiver()

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        sendEvent(WindowFocusChangedEvent(hasFocus))
    }

    @SuppressLint("ClickableViewAccessibility", "DiscouragedPrivateApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen(); super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false); enableEdgeToEdge()
        lifecycleScope.launch(Dispatchers.IO) { Language.initLocaleAsync(this@MainActivity) }
        WindowCompat.getInsetsController(window, window.decorView).systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        instance = WeakReference(this)
        pendingCrashReport = CrashHandler.getPendingReport(this)
        try {
            val f = CursorWindow::class.java.getDeclaredField("sCursorWindowSize"); f.isAccessible = true; f.set(null, 100 * 1024 * 1024)
        } catch (_: Exception) {
        }
        BluetoothPermission.init(this); Permissions.init(this); initEvents()
        val powerFilter = IntentFilter().apply { addAction(Intent.ACTION_POWER_CONNECTED); addAction(Intent.ACTION_POWER_DISCONNECTED) }
        if (isTPlus()) {
            registerReceiver(plugInReceiver, powerFilter, RECEIVER_NOT_EXPORTED); registerReceiver(networkStateReceiver, IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION), RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(plugInReceiver, powerFilter); registerReceiver(networkStateReceiver, IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION))
        }
        setContent {
            SettingsProvider {
                AppTheme(useDarkTheme = DarkTheme.isDarkTheme(LocalDarkTheme.current)) {
                    Main(
                        navControllerState, onLaunched = { handleIntent(intent) },
                        mainVM, audioPlaylistVM, pomodoroVM,
                        chatVM = chatVM, peerVM = peerVM,
                        channelVM = channelVM
                    )
                    if (showForwardTargetDialog) {
                        ForwardTargetDialog(
                            peerVM = peerVM, onDismiss = {
                                showForwardTargetDialog = false
                                pendingFileUris = null
                                pendingForwardText = null
                            },
                            onTargetSelected = { target ->
                                navControllerState.value?.navigate(Routing.Chat(target.encodedToId))
                                pendingFileUris?.let { uris ->
                                    coIO {
                                        delay(500)
                                        sendEvent(PickFileResultEvent(PickFileTag.SEND_MESSAGE, PickFileType.FILE, uris))
                                    }
                                }
                                pendingForwardText?.let { text ->
                                    coIO {
                                        delay(500)
                                        chatVM.sendTextMessage(text, this@MainActivity)
                                    }
                                }
                            })
                    }
                    pendingCrashReport?.let { report ->
                        CrashReportDialog(crashReport = report, onDismiss = { pendingCrashReport = null })
                    }
                    pendingLoginEvent?.let { event ->
                        val clientIp = HttpServerManager.clientIpCache[event.clientId] ?: ""
                        LoginRequestPage(
                            clientIp = clientIp,
                            request = event.request,
                            onDeny = {
                                pendingLoginEvent = null
                                lifecycleScope.launch(Dispatchers.IO) {
                                    event.session.close(CloseReason(CloseReason.Codes.TRY_AGAIN_LATER, "rejected"))
                                }
                            },
                            onAllow = {
                                pendingLoginEvent = null
                                lifecycleScope.launch(Dispatchers.IO) {
                                    HttpServerManager.respondTokenAsync(event, clientIp)
                                }
                            },
                        )
                    }
                    pendingPairingEvent?.let { event ->
                        PairingRequestPage(
                            event = event,
                            onDeny = {
                                pendingPairingEvent = null
                                coIO {
                                    NearbyPairManager.respondToPairing(event.request, false)
                                }
                            },
                            onAllow = {
                                pendingPairingEvent = null
                                coIO {
                                    NearbyPairManager.respondToPairing(event.request, true)
                                }
                            },
                        )
                    }
                    pendingChannelInviteEvent?.let { event ->
                        ChannelInvitePage(
                            event = event,
                            onDecline = {
                                pendingChannelInviteEvent = null
                                channelVM.declineChannelInvite(this@MainActivity, event.channelId)
                            },
                            onAccept = {
                                pendingChannelInviteEvent = null
                                channelVM.acceptChannelInvite(event.channelId)
                            },
                        )
                    }
                }
            }
        }
        AudioPlayer.ensurePlayer(this)
        coIO {
            try {
                if (WebPreference.getAsync()) {
                    mainVM.enableHttpServer(this@MainActivity, true)
                    doWhenReadyAsync()
                }
            } catch (ex: Exception) {
                LogCat.e(ex.toString())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Permissions.release()
        unregisterReceiver(plugInReceiver)
        unregisterReceiver(networkStateReceiver)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        PlainAccessibilityService.invalidateScreenSizeCache()
        lifecycleScope.launch(Dispatchers.IO) {
            Language.initLocaleAsync(this@MainActivity)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent); handleIntent(intent)
    }

    fun openNew() {
        try {
            // 1. Verificamos si la app ya está visible para el usuario
            // Nota: Si no tienes "AppHelper.foregrounded()", puedes usar "instance.get() != null"
            // o verificar si la activity está en un estado válido del ciclo de vida.
            if (foregrounded()) {
                // Si ya está al frente, simplemente la reordenamos de forma nativa
                val intent = Intent(this, MainActivity::class.java).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    )
                }
                startActivity(intent)
            } else {
                // 2. Si está en SEGUNDO PLANO, enviamos una notificación interactiva
                val context = this
                val channelId = "app_foreground_channel" // Asegúrate de tener este canal creado en tu app

                val intent = Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    // Opcional: puedes pasar un extra si quieres que al abrir desde la notificación haga algo extra
                    putExtra("opened_from_notification", true)
                }

                val pendingIntent = android.app.PendingIntent.getActivity(
                    context,
                    1002, // ID único para este PendingIntent
                    intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )

                val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

                // Creamos la notificación
                val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
                    .setContentTitle(LocaleHelper.getString(Res.string.app_name)) // Puedes cambiarlo por recursos de Strings si tienes
                    .setContentText(LocaleHelper.getString(Res.string.permission_required))
                    .setSmallIcon(android.R.drawable.ic_dialog_info) // Cambia por tu icono real (ej. R.drawable.ic_logo)
                    .setAutoCancel(true)
                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .build()

                notificationManager.notify(1002, notification)
                LogCat.d("App en background: Se envió notificación para abrir MainActivity de forma segura.")
            }
        } catch (e: Exception) {
            LogCat.e("Error bringing MainActivity to foreground: ${e.message}")
        }
    }

    companion object {
        lateinit var instance: WeakReference<MainActivity>
    }
}
