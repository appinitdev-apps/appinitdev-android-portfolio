
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFilePage(
    navController: NavHostController,
    path: String,
    title: String,
    mediaId: String = "",
    type: String = TextFileType.DEFAULT.name,
    textFileVM: TextFileViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val darkTheme = LocalDarkTheme.current
    val isDarkTheme = DarkTheme.isDarkTheme(darkTheme)

    var isSaving by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isSaving) 360f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "save_rotation"
    )

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            textFileVM.loadConfigAsync(context)
            textFileVM.loadFileAsync(context, path, mediaId)
            textFileVM.isDataLoading.value = false
        }
    }

    if (textFileVM.showMoreActions.value) {
        ViewTextFileBottomSheet(textFileVM, path, textFileVM.file.value, onDeleted = {
            scope.launch {
                navController.navigateUp()
            }
        })
    }

    BackHandler(enabled = !textFileVM.readOnly.value) {
        textFileVM.exitEditMode()
    }

    PScaffold(
        topBar = {
            PTopAppBar(
                title = title.ifEmpty { path.getFilenameFromPath() },
                navController = navController,
                navigationIcon = {
                    if (textFileVM.readOnly.value) {
                        NavigationBackIcon { navController.navigateUp() }
                    } else {
                        NavigationCloseIcon {
                            textFileVM.exitEditMode()
                        }
                    }
                },
                actions = {
                    TextFilePageActions(
                        textFileVM = textFileVM,
                        type = type,
                        path = path,
                        isSaving = isSaving,
                        rotation = rotation,
                        scope = scope,
                        context = context,
                        onSavingChanged = { isSaving = it },
                    )
                },
            )
        },
        content = { paddingValues ->
            // Use Box so the WebView renders in the background while the spinner
            // overlays it — avoids a layout-shift freeze when the spinner hides.
            Box(modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .fillMaxSize()) {
                if (!textFileVM.isDataLoading.value) {
                    AceEditor(
                        textFileVM, scope,
                        EditorData(
                            language = path.pathToAceMode(),
                            wrapContent = textFileVM.wrapContent.value,
                            isDarkTheme = isDarkTheme,
                            readOnly = textFileVM.readOnly.value,
                            gotoEnd = type == TextFileType.APP_LOG.name,
                            content = textFileVM.content.value
                        )
                    )
                }
                if (textFileVM.isDataLoading.value || !textFileVM.isEditorReady.value) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        },
    )
}

