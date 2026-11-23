
@Composable
fun StayOnlineModeOverlay(onExit: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    // true = pure black screen; false = text visible
    var sleeping by remember { mutableStateOf(false) }
    var sleepJob by remember { mutableStateOf<Job?>(null) }

    fun scheduleSleep(delayMs: Long) {
        sleepJob?.cancel()
        sleepJob = scope.launch {
            delay(delayMs)
            sleeping = true
        }
    }

    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Initial display: show text for 3s then sleep
    LaunchedEffect(Unit) { scheduleSleep(3_000) }

    val textAlpha by animateFloatAsState(
        targetValue = if (sleeping) 0f else 1f,
        animationSpec = tween(durationMillis = 1500),
        label = "textAlpha",
    )

    Dialog(
        onDismissRequest = onExit,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        ),
    ) {
        val dialogView = LocalView.current
        SideEffect {
            val dialogWindow = (dialogView.parent as? DialogWindowProvider)?.window
            if (dialogWindow != null) {
                dialogWindow.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                )
                WindowCompat.setDecorFitsSystemWindows(dialogWindow, false)
                WindowInsetsControllerCompat(dialogWindow, dialogWindow.decorView).apply {
                    hide(WindowInsetsCompat.Type.systemBars())
                    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    if (sleeping) {
                        sleeping = false
                        scheduleSleep(5_000)
                    } else {
                        onExit()
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            if (!sleeping) {
                Column(
                    modifier = Modifier
                        .alpha(textAlpha)
                        .padding(horizontal = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.stay_online_mode),
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 56.sp,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(Res.string.stay_online_mode_oled_saving),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 20.sp,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(Res.string.stay_online_mode_tap_to_exit),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 20.sp,
                    )
                }
            }
        }
    }
}
