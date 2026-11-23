@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatEditTextPage(
    navController: NavHostController,
    id: String,
    content: String,
    chatVM: ChatViewModel,
) {
    val scope = rememberCoroutineScope()
    var inputValue by remember { mutableStateOf(content) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    PScaffold(
        topBar = {
            PTopAppBar(
                navController = navController,
                title = stringResource(Res.string.edit_text),
                actions = {
                    PIconButton(
                        icon = Res.drawable.save,
                        contentDescription = stringResource(Res.string.save),
                        tint = MaterialTheme.colorScheme.onSurface,
                    ) {
                        if (inputValue.isNotEmpty()) {
                            scope.launch {
                                val originalChat = ChatDbHelper.getChatItem(id) ?: return@launch
                                ChatMessageEditor.updateTextAsync(context, originalChat, inputValue)
                                chatVM.update(originalChat)
                                focusManager.clearFocus()
                                navController.navigateUp()
                            }
                        }
                    }
                })
        },
        content = { paddingValues ->
            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                modifier =
                Modifier
                    .padding(start = 16.dp, end = 16.dp, top = paddingValues.calculateTopPadding())
                    .imePadding()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default,
                shape = RoundedCornerShape(8.dp),
            )
        },
    )
}
