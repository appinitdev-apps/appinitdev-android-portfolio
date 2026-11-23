
class MainActivity : FragmentActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private val delayMillis: Long = 10 * 60 * 1000 
    private val viewModel: ChatViewModel by viewModels()
    lateinit var billingManager: BillingManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private var statusRunnable: Runnable? = null
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var preferencesManager: PreferencesManager
    private var adsRemoved = mutableStateOf(false)
    private var isAuthenticated = mutableStateOf(false)


    val scheduler by lazy {
        AndroidAlarmSch(applicationContext)
    }
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            viewModel = viewModel,
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }
    private val showAdRunnable = object : Runnable {
        override fun run() {
            if (!BuildConfig.DEBUG && viewModel.state.value.userData?.isVip == false) {
                InterstitialAdManager.showAd(this@MainActivity)
            }
            handler.postDelayed(this, delayMillis)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) 
    }

    override fun onPause() {
        super.onPause()


    }

    override fun onStart() {
        super.onStart()
        if (viewModel.state.value.chatId != "")
            viewModel.readAllMessagesInChat(viewModel.state.value.chatId)

        viewModel.ejecutarUnaVezPorDia(this) {
            viewModel.actualizarTodosUsuariosOffline()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            Firebase.auth.currentUser?.uid?.let { userId ->
                viewModel.getUserData(userId)
                
                
                viewModel.state.value.userData?.suspendedUntil?.let { suspendedUntil ->
                    if (suspendedUntil.toDate().after(Date())) {
                        val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(suspendedUntil.toDate())
                        Toast.makeText(this, getString(R.string.cuenta_suspendida, formattedDate), Toast.LENGTH_LONG).show()
                        lifecycleScope.launch {
                            googleAuthUiClient.signOut()
                        }
                    }
                }

                if (viewModel.chats.isEmpty()) {
                    viewModel.showChats(userId)
                }
            }
            if (viewModel.state.value.chatId != "")
                viewModel.readAllMessagesInChat(viewModel.state.value.chatId)
        } catch (e: Exception) {
            Log.e("MainActivity", "onResume error", e)
        }
    }

    override fun onStop() {
        super.onStop()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
        
        
        
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        setupCrashHandler()
        super.onCreate(savedInstanceState)
        billingManager = BillingManager(this, viewModel)

        
        val prefs = getSharedPreferences("privacy_prefs", Context.MODE_PRIVATE)
        val isLockEnabled = prefs.getBoolean("biometric_lock", false)
        
        if (isLockEnabled && !isAuthenticated.value) {
            BiometricHelper.showBiometricPrompt(
                activity = this,
                title = getString(R.string.bloqueo_app),
                subtitle = getString(R.string.autenticate_para_entrar),
                onSuccess = { isAuthenticated.value = true },
                onError = { finish() }
            )
        } else {
            isAuthenticated.value = true
        }

        
        if (!BuildConfig.DEBUG) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        
        if (!BuildConfig.DEBUG) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        MobileAds.initialize(this) { }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
         




         










        preferencesManager = PreferencesManager(this)

        if (!BuildConfig.DEBUG && preferencesManager.isAdsRemoved() == false && viewModel.state.value.userData?.isVip == false) {
            InterstitialAdManager.loadAd(this)
            handler.postDelayed({ InterstitialAdManager.showAd(this) }, 3 * 60 * 1000)
            handler.postDelayed(showAdRunnable, delayMillis)
        }


        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver(viewModel))

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        enableEdgeToEdge()
        try {
            programarNotificacionDiaria(this)
        }catch (e: Exception){}
        observeNetwork()

        setContent {
            if (isAuthenticated.value) {
                ConvoixTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                    val state by viewModel.state.collectAsState()
                    val navController = rememberNavController()

                    val context = LocalContext.current

                    
                    LaunchedEffect(intent) {
                        intent?.getStringExtra("chatId")?.let { chatId ->
                            
                            viewModel.chats.find { it.chatId == chatId }?.let { chat ->
                                val chatUser = if (chat.user1?.userId == Firebase.auth.currentUser?.uid) chat.user2 else chat.user1
                                if (chatUser != null) {
                                    viewModel.getTp(chatId)
                                    viewModel.readAllMessagesInChat(chatId)
                                    viewModel.setchatUser(chatUser, chatId)
                                    navController.navigate("chat")
                                }
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "start"
                    ) {
                        composable("start") {
                            LaunchedEffect(key1 = Unit) {
                                val userData = googleAuthUiClient.getSignedInUser()
                                if (userData != null) {
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        viewModel.updateStatus(true, userData.userId)
                                        viewModel.popStory()
                                        viewModel.getFCMToken(userData.userId)
                                        viewModel.getUserData(userData.userId)
                                        viewModel.showChats(userData.userId)
                                    }
                                    navController.navigate("chats")
                                } else {
                                    navController.navigate("signIn")
                                }
                            }
                        }
                        composable("signIn") {
                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        lifecycleScope.launch {
                                            val signInResult = googleAuthUiClient.signInWithIntent(
                                                intent = result.data ?: return@launch
                                            )
                                            viewModel.onSignInResult(signInResult)
                                        }
                                    }
                                }
                            )
                            LaunchedEffect(key1 = state.isSignedIn) {
                                if (state.isSignedIn) {
                                    Toast.makeText(
                                        applicationContext,
                                        getString(R.string.inici_sesi_n_exitosamente),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val userData = googleAuthUiClient.getSignedInUser()
                                    userData?.run {
                                        viewModel.getFCMToken(userData.userId)
                                        
                                        viewModel.getUserData(userData.userId)
                                        viewModel.showChats(userData.userId)
                                        viewModel.popStory()
                                        viewModel.getFCMToken(userData.userId)
                                        navController.navigate("chats")
                                        viewModel.updateStatus(true)
                                    }
                                }
                            }

                            Scaffold(
                                contentWindowInsets = WindowInsets.ime, 
                                content = {
                                    SignInScreen(
                                        state = state,
                                        onSignUpClick = { email, pw, user, age, gender, profileUri, isVendedora, instagram, facebook, telegram ->
                                            lifecycleScope.launch {
                                                
                                                val isLocalImage = profileUri.startsWith("content://") || profileUri.startsWith("file://")

                                                if (isLocalImage) {
                                                    
                                                    val uri = android.net.Uri.parse(profileUri)

                                                    
                                                    viewModel.uploadMedia(uri = uri, folder = "photos") { downloadedUrl ->
                                                        if (downloadedUrl.isNotEmpty()) {
                                                            
                                                            lifecycleScope.launch {
                                                                val result = googleAuthUiClient.createUserWithEmailPassword(
                                                                    email, pw, user, age, gender, downloadedUrl
                                                                )

                                                                
                                                                if (result.data != null) {
                                                                    try {
                                                                        val userDataWithImage = result.data.copy(
                                                                            ppurl = downloadedUrl,
                                                                            pref = result.data.pref.copy(verificada = isVendedora),
                                                                            instagram = instagram,
                                                                            facebook = facebook,
                                                                            telegram = telegram
                                                                        )
                                                                        viewModel.getFCMToken(userDataWithImage.userId)
                                                                        viewModel.addUserDataToFirestore(userDataWithImage, age, gender)
                                                                        viewModel.getUserData(userDataWithImage.userId)
                                                                        viewModel.showChats(userDataWithImage.userId)
                                                                        navController.navigate("chats") {
                                                                            popUpTo("signIn") { inclusive = true }
                                                                        }
                                                                    } catch (e: Exception) {
                                                                        Log.e("Error", "Error al procesar datos: ${e.message}")
                                                                    }
                                                                } else {
                                                                    Toast.makeText(applicationContext, getString(R.string.error_formato, result.errmsg ?: ""), Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                        } else {
                                                            Toast.makeText(applicationContext, getString(R.string.error_subir_imagen), Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                } else {
                                                    
                                                    val result = googleAuthUiClient.createUserWithEmailPassword(
                                                        email, pw, user, age, gender, profileUri
                                                    )

                                                    if (result.data != null) {
                                                        try {
                                                            val userDataWithPref = result.data.copy(
                                                                pref = result.data.pref.copy(verificada = isVendedora),
                                                                instagram = instagram,
                                                                facebook = facebook,
                                                                telegram = telegram
                                                            )
                                                            viewModel.getFCMToken(userDataWithPref.userId)
                                                            viewModel.addUserDataToFirestore(userDataWithPref, age, gender)
                                                            viewModel.getUserData(userDataWithPref.userId)
                                                            viewModel.showChats(userDataWithPref.userId)
                                                            navController.navigate("chats") {
                                                                popUpTo("signIn") { inclusive = true }
                                                            }
                                                        } catch (e: Exception) {
                                                            Log.e("Error", "Error: ${e.message}")
                                                        }
                                                    } else {
                                                        Toast.makeText(applicationContext, getString(R.string.error_formato, result.errmsg ?: ""), Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        },
                                        onSignInClick = { email, pw ->
                                            lifecycleScope.launch {
                                                try {
                                                    val result =
                                                        googleAuthUiClient.signInWithEmailPassword(
                                                            email,
                                                            pw
                                                        )
                                                    if (result.data != null) {
                                                        
                                                        viewModel.getFCMToken(result.data.userId)
                                                        viewModel.getUserData(result.data.userId)
                                                        viewModel.showChats(result.data.userId)
                                                        navController.navigate("chats")
                                                    } else {
                                                        Toast.makeText(
                                                            applicationContext,
                                                            getString(R.string.error_verifique_su_correo_y_contrase_a),
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                } catch (e: Exception) {
                                                }
                                            }

                                        },
                                        navController
                                    )
                                })
                        }
                        composable("forgot_password") {
                            ForgotPasswordScreen(
                                onPasswordReset = { email ->
                                    lifecycleScope.launch {
                                        try {
                                            googleAuthUiClient.sendPasswordResetEmail(email)
                                        } catch (e: Exception) {
                                        }
                                    }
                                },
                                navController = navController
                            )
                        }
                        composable("chats") {
                            ChatScreen(
                                navController,
                                viewModel,
                                state,
                                showSingleChat = { usr, id ->
                                    viewModel.getTp(id)
                                    viewModel.readAllMessagesInChat(id)
                                    viewModel.setchatUser(usr, id)
                                    navController.navigate("chat")

                                })

                        }
                        composable("chat", enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(200)
                            )
                        }, exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(200)
                            )
                        }) {
                            val chatUser = state.User2
                            if (chatUser != null) {
                                Chat(navController,
                                    viewModel,
                                    messages = viewModel.messages,
                                    userData = chatUser,
                                    chatId = state.chatId,
                                    state = state,
                                    onBack = {
                                        navController.popBackStack()
                                        viewModel.dePopMsg()
                                        viewModel.depopTp()
                                        viewModel.dltchatUser()
                                    },
                                    schedule = { time, content, chatId, senderId ->
                                        val alarmItem = AlarmItem(
                                            chatId = chatId,
                                            content = content,
                                            time = time,
                                            senderId = senderId
                                        )
                                        alarmItem.let(scheduler::schedule)
                                    })
                            } else {
                                LaunchedEffect(Unit) {
                                    navController.navigate("chats") {
                                        popUpTo("chat") { inclusive = true }
                                    }
                                }
                            }
                        }
                        composable("otherprofile") {
                            state.User2?.let { user ->
                                OtherProfile(user, navController, viewModel)
                            } ?: LaunchedEffect(Unit) {
                                navController.popBackStack()
                            }
                        }
                        composable("profile") {

                            LaunchedEffect(Unit) {
                                val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                                currentUserId?.let { id ->
                                    viewModel.getUserData(id)
                                }
                            }

                            Log.d("DEBUG_PROFILE", "User recibido en Dialog: ${state.userData}")

                            ProfileScreen(viewModel = viewModel, state = state, onSignOut = {
                                lifecycleScope.launch {
                                    googleAuthUiClient.signOut()
                                    Toast.makeText(
                                        applicationContext,
                                        getString(R.string.sesion_cerrada),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.navigate("signIn")
                                }
                            }, navController)
                        }

                        composable("gallery") {
                            ImageGallery(navController, viewModel)
                        }
                        composable("settings", enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(200)
                            )
                        }, exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(200)
                            )
                        }) {
                            Settings(navController, state, viewModel)
                        }
                        composable("cus", enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(200)
                            )
                        }, exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(200)
                            )
                        }) {
                            Customization(viewModel, state)
                        }
                        composable("blck", enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(200)
                            )
                        }, exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(200)
                            )
                        }) {
                            state.userData?.blockedUsers?.let { blockedUsers ->
                                viewModel.getBlockedUsers(blockedUsers)
                            }
                            BlockedUsers(viewModel, state, navController)
                        }
                        composable("forward", enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(200)
                            )
                        }, exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(200)
                            )
                        }) {
                            ForwardMsg(navController, viewModel, state)
                        }
                        composable("scheduled", enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(200)
                            )
                        }, exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(200)
                            )
                        }) {
                            ScheduledMsgs(navController, viewModel, state, scheduler)
                        }
                        composable("admin") {
                            AdminScreen(navController, viewModel)
                        }
                        composable("store") {
                            com.appinitdev.chatok.screens.StoreScreen(navController, viewModel, state)
                        }
                    }
                    NetworkErrorDialog(showDialog = false) 
                    }
                }
            }
        }
    }

    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("CRASH", "App crasheó: ${throwable.message}", throwable)

             










            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun observeNetwork() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        val initiallyOffline = capabilities == null || !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        viewModel.updateNetworkStatus(initiallyOffline)

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                handler.post {
                    viewModel.updateNetworkStatus(false)
                }
            }

            override fun onLost(network: Network) {
                handler.post {
                    viewModel.updateNetworkStatus(true)
                }
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
}

@Composable
fun SolicitarPermisoNotificaciones() {
    val context = LocalContext.current

    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            
        } else {
            
        }
    }


    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permiso = Manifest.permission.POST_NOTIFICATIONS

            if (ContextCompat.checkSelfPermission(
                    context,
                    permiso
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(permiso)
            }
        }
    }
}


