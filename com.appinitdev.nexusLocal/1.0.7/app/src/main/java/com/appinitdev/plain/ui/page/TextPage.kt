@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextPage(
    navController: NavHostController,
    title: String,
    content: String,
    language: String,
    textFileVM: TextFileViewModel = viewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val darkTheme = LocalDarkTheme.current
    val isDarkTheme = DarkTheme.isDarkTheme(darkTheme)
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            textFileVM.loadConfigAsync(context)
            textFileVM.isDataLoading.value = false
        }
    }
    if (textFileVM.showMoreActions.value) {
        ViewTextContentBottomSheet(textFileVM, content)
    }
    PScaffold(
        topBar = {
            PTopAppBar(navController = navController, title = title, actions = {
                ActionButtonMore {
                    textFileVM.showMoreActions.value = true
                }
            })
        },
        content = { paddingValues ->
            Column(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
                if (textFileVM.isDataLoading.value) {
                    NoDataColumn(loading = true)
                    return@PScaffold
                }
                if (!textFileVM.isEditorReady.value) {
                    NoDataColumn(loading = true)
                }
                AceEditor(
                    textFileVM, scope, EditorData(
                        language,
                        textFileVM.wrapContent.value,
                        isDarkTheme = isDarkTheme,
                        readOnly = textFileVM.readOnly.value,
                        gotoEnd = false,
                        content = content
                    )
                )
            }
        },
    )
}
