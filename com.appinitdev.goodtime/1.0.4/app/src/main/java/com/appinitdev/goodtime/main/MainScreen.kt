
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    navController: NavController,
    onSurfaceClick: () -> Unit,
    hideBottomBar: Boolean,
    viewModel: TimerViewModel,
    mainViewModel: MainViewModel,
    onUpdateClicked: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(TimerMainUiState())
    val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle(MainUiState())

    val updateAvailable = mainUiState.isUpdateAvailable
    val wasNotificationPermissionDenied = mainUiState.wasNotificationPermissionDenied

    var chosenColor by remember { mutableStateOf(Color.Gray) } 
    var showDialog by remember { mutableStateOf(false) }

    if (uiState.isLoading) return
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    InitTimerStyle(viewModel)

    LifecycleResumeEffect(Unit) {
        viewModel.refreshStartOfToday()
        onPauseOrDispose {
            
        }
    }

    val timerUiState by viewModel.timerUiState.collectAsStateWithLifecycle(TimerUiState())

    val timerStyle = uiState.timerStyle
    val label = timerUiState.label

    val configuration = LocalConfiguration.current
    val haptic = LocalHapticFeedback.current

    val dialControlState =
        rememberCustomDialControlState(
            config = DialConfig(),
            onLeft = viewModel::skip,
            onTop = viewModel::addOneMinute,
            onRight = viewModel::skip,
            onBottom = viewModel::resetTimer,
        )

    dialControlState.updateEnabledOptions(timerUiState)
    val gestureModifier =
        dialControlState.let {
            Modifier
                .pointerInput(it) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        it.onDown(position = down.position)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        var change =
                            awaitTouchSlopOrCancellation(pointerId = down.id) { change, _ ->
                                change.consume()
                            }
                        while (change != null && change.pressed) {
                            change =
                                awaitDragOrCancellation(change.id)?.also { inputChange ->
                                    if (inputChange.pressed && timerUiState.isActive) {
                                        dialControlState.onDrag(dragAmount = inputChange.positionChange())
                                    }
                                }
                        }
                        it.onRelease()
                    }
                }
        }

    val yOffset = remember { Animatable(0f) }
    ScreensaverMode(
        screensaverMode = uiState.screensaverMode,
        isActive = timerUiState.isActive,
        screenWidth = configuration.screenWidth,
        yOffset = yOffset,
    )


    val isDarkTheme = uiState.darkThemePreference.isDarkTheme(isSystemInDarkTheme())
    val backgroundColor by animateColorAsState(
        if (isDarkTheme &&
            uiState.trueBlackMode &&
            timerUiState.isActive
        ) {
            Color.Black
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "main background color",
    )

    val flashScreenBackgroundColor by
    if (timerUiState.isFinished && uiState.flashScreen) {
        val infiniteTransition = rememberInfiniteTransition(label = "flash")
        infiniteTransition.animateColor(
            initialValue = backgroundColor,
            targetValue = MaterialTheme.colorScheme.onSurface,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(200, easing = FastOutLinearInEasing, delayMillis = 2000),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "flashColor",
        )
    } else {
        rememberUpdatedState(backgroundColor)
    }

    val permissionState = getPermissionsState()
    val actionBadgeItemCount = permissionState.count() + if (updateAvailable) 1 else 0
    val interactionSource = remember { MutableInteractionSource() }

    var showNavigationSheet by rememberSaveable { mutableStateOf(false) }
    var showSelectLabelDialog by rememberSaveable { mutableStateOf(false) }

    val showTutorial = uiState.showTutorial


    val imagePath by BackgroundPrefs.getImagePath(context).collectAsState(initial = null)
    val savedColor by BackgroundPrefs.getColor(context).collectAsState(initial = Color.Gray)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                BackgroundPrefs.saveImage(context, uri)
            }
        }
    }


    AnimatedVisibility(
        timerUiState.isReady,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
            Surface(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                        ) {
                            onSurfaceClick()
                        },
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(flashScreenBackgroundColor)
                            .windowInsetsPadding(WindowInsets.safeDrawing),
                    contentAlignment = Alignment.Center,
                ) {

                    if (imagePath != null) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = ImageRequest.Builder(context)
                                    .data(File(imagePath!!))
                                    .diskCachePolicy(CachePolicy.DISABLED) 
                                    .memoryCachePolicy(CachePolicy.DISABLED) 
                                    .build()
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(0.6f),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        savedColor?.let {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(it.copy(alpha = 0.6f)) 
                            )
                        }

                    }

                    if (!hideBottomBar)
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd) 
                                .padding(top = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (savedColor == null)
                                IconButton(onClick = {
                                    launcher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.AddPhotoAlternate,
                                        contentDescription = "Agregar fondo",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            if (imagePath == null)
                                IconButton(onClick = {
                                    showDialog = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Palette,
                                        contentDescription = "Agregar color",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            Spacer(modifier = Modifier.width(3.dp))
                            if (imagePath != null || savedColor != null) {
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        BackgroundPrefs.deleteImage(context)
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Eliminar fondo",

                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }


                    val tutorialModifier =
                        if (showTutorial) Modifier.blur(radius = 4.dp) else Modifier
                    val modifier =
                        Modifier.offset {
                            if (configuration.isPortrait) {
                                IntOffset(
                                    0,
                                    yOffset.value.roundToInt(),
                                )
                            } else {
                                IntOffset(yOffset.value.roundToInt(), 0)
                            }
                        }

                    val alphaModifier =
                        Modifier.graphicsLayer {
                            alpha = if (dialControlState.isDragging) 0.38f else 1f
                        }
                    MainTimerView(
                        modifier = alphaModifier.then(modifier),
                        state = dialControlState,
                        gestureModifier = gestureModifier.then(tutorialModifier),
                        timerUiState = timerUiState,
                        timerStyle = timerStyle,
                        domainLabel = label,
                        onStart = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (permissionState.shouldAskForAlarmPermission) {
                                coroutineScope.launch {
                                    showAlarmPermissionSnackbar(context)
                                }
                            } else {
                                viewModel.startTimer()
                            }
                        },
                        onToggle = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (permissionState.shouldAskForAlarmPermission) {
                                coroutineScope.launch {
                                    showAlarmPermissionSnackbar(context)
                                }
                            } else {
                                viewModel.toggleTimer()
                            }
                        },
                        onLongClick = { navController.navigate(SettingsDest) },
                    )
                    DialControl(
                        modifier = modifier,
                        state = dialControlState,
                        dialContent = { region ->
                            DialControlButton(
                                enabled = !dialControlState.isDisabled(region),
                                selected = region == dialControlState.selectedOption,
                                region = region,
                            )
                        },
                    )
                    if (showTutorial) {
                        TutorialScreen(
                            onClose = { viewModel.setShowTutorial(false) },
                        )
                    } else {
                        BottomAppBar(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            hide = hideBottomBar,
                            onShowSheet = { showNavigationSheet = true },
                            onLabelClick = { showSelectLabelDialog = true },
                            labelData = label.getLabelData(),
                            sessionCountToday = uiState.sessionCountToday,
                            badgeItemCount = actionBadgeItemCount,
                            navController = navController,
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {},
            title = { Text("Selecciona un color") },
            text = {
                ColorPickerDialog(
                    onColorSelected = { color ->
                        chosenColor = color   
                        coroutineScope.launch {
                            BackgroundPrefs.setColor(context, chosenColor)
                        }
                        showDialog = false    
                    }
                )
            }
        )
    }

    if (showNavigationSheet) {
        MainNavigationSheet(
            onHideSheet = { showNavigationSheet = false },
            navController = navController,
            onUpdateClicked = onUpdateClicked,
            actionBadgeCount = actionBadgeItemCount,
            showPro = BuildConfig.IS_FDROID || !uiState.isPro,
            isUpdateAvailable = updateAvailable,
            wasNotificationPermissionDenied = wasNotificationPermissionDenied,
            onNotificationPermissionGranted = { granted: Boolean ->
                mainViewModel.setNotificationPermissionGranted(granted)
            },
        )
    }

    var showFinishedSessionSheet by remember(timerUiState.isFinished) {
        mutableStateOf(timerUiState.isFinished)
    }

    if (showFinishedSessionSheet) {
        FinishedSessionSheet(
            timerUiState = timerUiState,
            onHideSheet = { showFinishedSessionSheet = false },
            onNext = { updateWorkTime ->
                viewModel.next(updateWorkTime)
                
                if (context.installIsOlderThan10Days() && !timerUiState.isBreak) {
                    viewModel.setShouldAskForReview()
                }
            },
            onReset = { updateWorkTime ->
                if (updateWorkTime) {
                    viewModel.resetTimer(updateWorkTime = true)
                } else {
                    viewModel.resetTimer(false, actionType = FinishActionType.MANUAL_DO_NOTHING)
                }
            },
            onUpdateNotes = viewModel::updateNotesForLastCompletedSession,
        )
    }

    if (showSelectLabelDialog) {
        SelectActiveLabelDialog(
            initialSelectedLabel = timerUiState.label.label.name,
            onDismiss = { showSelectLabelDialog = false },
            onConfirm = { selectedLabels ->
                if (selectedLabels.isNotEmpty()) {
                    val first = selectedLabels.first()
                    if (first != label.label.name) {
                        viewModel.setActiveLabel(first)
                    }
                }
                showSelectLabelDialog = false
            },
            onNavigateToLabels = {
                navController.navigate(LabelsDest)
                showSelectLabelDialog = false
            },
            onNavigateToActiveLabel = {
                navController.navigate(AddEditLabelDest(name = timerUiState.label.getLabelName()))
                showSelectLabelDialog = false
            },
            onNavigateToTimerDurations = {
                navController.navigate(TimerDurationsDest)
                showSelectLabelDialog = false
            },
            onClearLabel = {
                viewModel.setActiveLabel(Label.DEFAULT_LABEL_NAME)
                showSelectLabelDialog = false
            },
        )
    }
}

private suspend fun showAlarmPermissionSnackbar(context: Context) {
    SnackbarController.sendEvent(
        event =
            SnackbarEvent(
                message = context.getString(R.string.settings_allow_alarms),
                action =
                    SnackbarAction(
                        name = context.getString(R.string.settings_allow),
                        action = {
                            context.askForAlarmPermission()
                        },
                    ),
            ),
    )
}
