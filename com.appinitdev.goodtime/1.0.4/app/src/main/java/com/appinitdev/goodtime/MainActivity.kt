 

















class MainActivity : GoodtimeMainActivity() {
    private val notificationManager: NotificationArchManager by inject()
    private val timerViewModel: TimerViewModel by viewModel<TimerViewModel>()
    private var fullScreenJob: Job? = null
    private var timerStateJob: Job? = null

    override fun onResume() {
        super.onResume()
        timerViewModel.onBringToForeground()
        timerStateJob =
            lifecycleScope.launch {
                timerViewModel.listenForeground()
            }
    }

    override fun onPause() {
        timerViewModel.onSendToBackground()
        timerStateJob?.cancel()
        timerStateJob = null
        super.onPause()
    }

    @SuppressLint("UnrememberedGetBackStackEntry", "UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        log.d { "onCreate" }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        var themeSettings by mutableStateOf(
            ThemeSettings(
                darkTheme = resources.configuration.isSystemInDarkTheme,
                isDynamicTheme = false,
            ),
        )

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    isSystemInDarkTheme(),
                    viewModel.uiState,
                ) { systemDark, uiState ->
                    ThemeSettings(
                        darkTheme = uiState.darkThemePreference.isDarkTheme(systemDark) && !uiState.showOnboarding,
                        isDynamicTheme = uiState.isDynamicColor,
                    )
                }.onEach { themeSettings = it }
                    .map { it.darkTheme }
                    .distinctUntilChanged()
                    .collect { darkTheme ->
                        enableEdgeToEdge(
                            statusBarStyle =
                                SystemBarStyle.auto(
                                    lightScrim = android.graphics.Color.TRANSPARENT,
                                    darkScrim = android.graphics.Color.TRANSPARENT,
                                ) { darkTheme },
                            navigationBarStyle =
                                SystemBarStyle.auto(
                                    lightScrim = lightScrim,
                                    darkScrim = darkScrim,
                                ) { darkTheme },
                        )
                    }
            }
        }

        splashScreen.setKeepOnScreenCondition { viewModel.uiState.value.loading }

        setContent {
            val coroutineScope = rememberCoroutineScope()

            val mainUiState by viewModel.uiState.collectAsStateWithLifecycle()

            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val isDarkTheme = themeSettings.darkTheme
            val showWhenLocked = uiState.showWhenLocked
            val isActive = uiState.isActive
            val keepScreenOn = uiState.keepScreenOn
            val isFinished = uiState.isFinished
            var isMainScreen by rememberSaveable { mutableStateOf(true) }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(showWhenLocked)
            }

            val fullscreenMode = isMainScreen && uiState.fullscreenMode
            LaunchedEffect(fullscreenMode) {
                fullscreenMode.let {
                    toggleFullscreen(it)
                    if (!it) fullScreenJob?.cancel()
                }
            }
            var hideBottomBar by remember(fullscreenMode) {
                mutableStateOf(fullscreenMode)
            }
            val onSurfaceClick = {
                if (fullscreenMode) {
                    fullScreenJob?.cancel()
                    fullScreenJob =
                        coroutineScope.launch {
                            toggleFullscreen(false)
                            hideBottomBar = false
                            executeDelayed(3000) {
                                toggleFullscreen(true)
                                hideBottomBar = true
                            }
                        }
                }
            }

            toggleKeepScreenOn(isActive && keepScreenOn)
            val startDestination =
                remember(mainUiState.showOnboarding) {
                    if (mainUiState.showOnboarding) {
                        OnboardingDest
                    } else {
                        MainDest
                    }
                }

            ApplicationTheme(darkTheme = isDarkTheme, dynamicColor = themeSettings.isDynamicTheme) {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }

                navController.addOnDestinationChangedListener { _, destination, _ ->
                    isMainScreen = destination.route == MainDest.route
                }

                LaunchedEffect(isFinished) {
                    if (isFinished) {
                        navController.currentDestination?.route?.let {
                            val shouldNavigate = it != MainDest.route
                            if (shouldNavigate) {
                                navController.navigate(MainDest) {
                                    popUpTo(MainDest) {
                                        inclusive = true
                                    }
                                }
                            }
                        }
                    }
                }

                ObserveAsEvents(
                    flow = SnackbarController.events,
                    snackbarHostState,
                ) { event ->
                    coroutineScope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()

                        val result =
                            snackbarHostState.showSnackbar(
                                message = event.message,
                                actionLabel = event.action?.name,
                                duration = SnackbarDuration.Long,
                            )

                        if (result == SnackbarResult.ActionPerformed) {
                            event.action?.action?.invoke()
                        }
                    }
                }
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState,
                        )
                    },
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                    ) {
                        composable<OnboardingDest> { OnboardingScreen() }
                        composable<MainDest> {
                            MainScreen(
                                onSurfaceClick = onSurfaceClick,
                                hideBottomBar = hideBottomBar,
                                navController = navController,
                                viewModel = timerViewModel,
                                mainViewModel = viewModel,
                                onUpdateClicked = { this@MainActivity.triggerAppUpdate() },
                            )
                        }
                        composable<LabelsDest> {
                            LabelsScreen(
                                onNavigateToLabel = navController::navigate,
                                onNavigateToArchivedLabels = {
                                    navController.navigate(ArchivedLabelsDest)
                                },
                                onNavigateToPro = { navController.navigate(ProDest) },
                                onNavigateBack = navController::popBackStack2,
                            )
                        }
                        composable<AddEditLabelDest> {
                            val addEditLabelDest = it.toRoute<AddEditLabelDest>()
                            AddEditLabelScreen(
                                labelName = addEditLabelDest.name,
                                onNavigateToDefault = { navController.navigate(TimerDurationsDest) },
                                onNavigateBack = navController::popBackStack2,
                            )
                        }
                        composable<ArchivedLabelsDest> {
                            ArchivedLabelsScreen(
                                onNavigateBack = navController::popBackStack2,
                            )
                        }
                        composable<StatsDest> {
                            StatisticsScreen(
                                onNavigateBack = navController::popBackStack2,
                            )
                        }
                        composable<SettingsDest> {
                            SettingsScreen(
                                onNavigateToUserInterface = { navController.navigate(UserInterfaceDest) },
                                onNavigateToNotifications = {
                                    navController.navigate(
                                        NotificationSettingsDest,
                                    )
                                },
                                onNavigateToDefaultLabel = {
                                    navController.navigate(TimerDurationsDest)
                                },
                                onNavigateBack = navController::popBackStack2,
                            )
                        }
                        composable<TimerDurationsDest> {
                            TimerProfileScreen(
                                onNavigateBack = navController::popBackStack2,
                            )
                        }
                        composable<UserInterfaceDest> {
                            UserInterfaceScreen(
                                onNavigateToPro = { navController.navigate(ProDest) },
                                onNavigateBack = navController::popBackStack2,
                            )
                        }
                        composable<NotificationSettingsDest> {
                            NotificationsScreen(
                                onNavigateBack = navController::popBackStack2,
                            )
                        }

                        composable<BackupDest> {
                            BackupScreen(
                                onNavigateToPro = { navController.navigate(ProDest) },
                                onNavigateBack = navController::popBackStack2,
                            )
                        }


                        composable<AcknowledgementsDest> {
                            AcknowledgementsScreen(navController::popBackStack2)
                        }
                        composable<ProDest> {
                            ProScreen(onNavigateBack = navController::popBackStack2)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        log.d { "onDestroy" }
        notificationManager.clearFinishedNotification()
        super.onDestroy()
    }

    private fun toggleKeepScreenOn(enabled: Boolean) {
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun toggleFullscreen(enabled: Boolean) {
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)

        if (enabled) {
            windowInsetsController.apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    private suspend fun executeDelayed(
        delay: Long,
        block: () -> Unit,
    ) {
        coroutineScope {
            delay(delay)
            block()
        }
    }
}

fun NavController.popBackStack2(): Boolean {
    if (this.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        return this.popBackStack()
    }
    return false
}

private val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)

data class ThemeSettings(
    val darkTheme: Boolean,
    val isDynamicTheme: Boolean,
)
