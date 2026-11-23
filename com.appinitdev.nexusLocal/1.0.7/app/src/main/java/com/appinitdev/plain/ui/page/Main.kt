@Composable
fun Main(
    navControllerState: MutableState<NavHostController?>,
    onLaunched: () -> Unit,
    mainVM: MainViewModel,
    audioPlaylistVM: AudioPlaylistViewModel,
    pomodoroVM: PomodoroViewModel,
    chatVM: ChatViewModel,
    peerVM: PeerViewModel,
    channelVM: ChannelViewModel,
    notesVM: NotesViewModel = viewModel(key = ""),
    feedTagsVM: TagsViewModel = viewModel(key = ""),
    noteTagsVM: TagsViewModel = viewModel(key = ""),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    navControllerState.value = navController
    val useDarkTheme = DarkTheme.isDarkTheme(LocalDarkTheme.current)
    val view = LocalView.current
    var confirmDialogEvent by remember { mutableStateOf<ConfirmDialogEvent?>(null) }
    var loadingDialogEvent by remember { mutableStateOf<LoadingDialogEvent?>(null) }
    var toastState by remember { mutableStateOf<ToastEvent?>(null) }
    val activity = context as? Activity
    LaunchedEffect(loadingDialogEvent) {
        if (loadingDialogEvent != null) activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    LaunchedEffect(Unit) {
        onLaunched()
        scope.launch(Dispatchers.IO) { pomodoroVM.loadAsync(context) }
    }
    MainEventCollector(
        scope, context, chatVM, audioPlaylistVM, pomodoroVM, peerVM,
        onConfirmDialog = { confirmDialogEvent = it },
        onLoadingDialog = { loadingDialogEvent = if (it.show) it else null },
        onToast = { toastState = it },
        clearToast = { toastState = null },
    )
    DisposableEffect(useDarkTheme) {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !useDarkTheme
            isAppearanceLightNavigationBars = !useDarkTheme
        }
        if (isQPlus() && context.isGestureInteractionMode()) {
            window.isNavigationBarContrastEnforced = false
        }
        onDispose { }
    }
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.backgroundNormal)) {
        MainNavGraph(navController, mainVM, audioPlaylistVM, chatVM, peerVM, channelVM, notesVM, feedTagsVM, noteTagsVM, pomodoroVM)
        MainDialogs(loadingDialogEvent, confirmDialogEvent, { confirmDialogEvent = null }, toastState, { toastState = null })
    }
}
